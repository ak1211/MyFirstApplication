// Copyright (c) 2021 Akihiro Yamamoto.
// Licensed under the MIT License <https://spdx.org/licenses/MIT.html>
// See LICENSE file in the project root for full license information.
//
package com.example.infraredremote

import arrow.core.*
import arrow.core.computations.either
import com.example.infraredremote.device.PanasonicHvac.decodePanasonicHvac
import com.example.infraredremote.device.ToshibaTv.decodeToshibaTv
import kotlinx.coroutines.runBlocking

//
fun <A> span(xs: List<A>, condition: (A) -> Boolean): Pair<List<A>, List<A>> =
    Pair(xs.takeWhile(condition), xs.dropWhile(condition))

//
fun <A> toListList(n: Int, xs: List<A>): List<List<A>> {
    return when {
        xs.none() -> listOf()
        else -> {
            val (hd, tl) = xs.take(n) to xs.drop(n)
            listOf(hd) + toListList(n, tl)
        }
    }
}

//
fun <A> toListNonEmptyList(n: Int, xs: List<A>): List<NonEmptyList<A>> {
    return toListList(n, xs)
        .traverseOption { NonEmptyList.fromList(it) }
        .toList()
        .flatten()
}

//
@JvmInline
value class Microseconds(val micros: Int) {
    init {
        require(micros >= 0) {}
    }

    operator fun plus(y: Microseconds): Microseconds {
        return Microseconds(micros + y.micros)
    }

    operator fun minus(y: Microseconds): Microseconds {
        return Microseconds(micros - y.micros)
    }
}

//
val Int.micros get() = Microseconds(this)

//
data class MarkAndSpace(val mark: Microseconds, val space: Microseconds)

//
fun fromIntList(input: List<Int>): List<MarkAndSpace> {
    return input
        .chunked(2)
        .filter { it.size == 2 }
        .map { (mark, space) -> MarkAndSpace(mark.micros, space.micros) }
}

//
fun toIntList(input: List<MarkAndSpace>): List<Int> {
    return input
        .flatMap { markAndSpace -> listOf(markAndSpace.mark.micros, markAndSpace.space.micros) }
}

//
fun from38khzOnOffPairHexString(input: String): Either<String, List<MarkAndSpace>> {
    //
    fun toInt(str: String): Either<String, Int> {
        //
        fun go(cs: CharArray): Either<String, Int> {
            val s = String(cs)
            return Option.fromNullable(s.toIntOrNull(16))
                .toEither { "unexpected char '$s'" }
        }
        //
        if (str.length != 4) {
            return Either.Left("unexpected end of input")
        }
        val lower = go(charArrayOf(str[0], str[1]))
        val higher = go(charArrayOf(str[2], str[3]))

        return lower.flatMap { lo ->
            higher.flatMap { hi ->
                Either.Right(hi * 256 + lo)
            }
        }
    }

    //
    fun toMarkAndSpace(mss: List<Int>): MarkAndSpace {
        val period = 26 // (1 / 38kHz) microseconds
        return MarkAndSpace(
            Microseconds(period * mss[0]), Microseconds(period * mss[1])
        )
    }
    //
    return input
        .chunked(4)
        .traverseEither(::toInt)
        .map { it.chunked(2).map(::toMarkAndSpace) }
}

//
fun digestFromMarkAndSpaces(input: List<MarkAndSpace>): Either<String, String> {
    return decodeToIrCodeFrames(input)
        .map { frames ->
            val protocolDigest = frames.first().toDigestString()
            decodePhase4(frames)
                .map {
                    when {
                        it is IrRemoteUnknown -> protocolDigest
                        else -> it.toDigestString()
                    }
                }
                .first()
        }
}

//
fun digestFromMarkAndSpaces2(input: List<MarkAndSpace>): Result<String> {
    return digestFromMarkAndSpaces(input)
        .fold(
            ifLeft = { Result.failure(Exception(it)) },
            ifRight = { Result.success(it) }
        )
}

//
sealed class InfraredLeader {
    object Aeha : InfraredLeader()
    object Nec : InfraredLeader()
    object Sirc : InfraredLeader()
    data class Unknown(val markandspace: MarkAndSpace) : InfraredLeader()

    companion object {
        // upper lower tolerance 0.3ms = 300us
        private fun typical(x: Microseconds) = withTolerance(300.micros, x)

        // H-level width, typical 3.4ms = 3400us
        // L-level width, typical 1.7ms = 1700us
        val leaderAeha = MarkAndSpace(3400.micros, 1700.micros)

        // H-level width, typical 9.0ms = 9000us
        // L-level width, typical 4.5ms = 4500us
        val leaderNec = MarkAndSpace(9000.micros, 4500.micros)

        // H-level width, typical 2.4ms = 2400us
        // L-level width, typical 0.6ms = 600us
        val leaderSirc = MarkAndSpace(2400.micros, 600.micros)

        //
        fun fromMarkAndSpace(input: MarkAndSpace): InfraredLeader {
            fun cmp(x: MarkAndSpace, y: MarkAndSpace): Boolean {
                return x.mark.micros in typical(y.mark) && x.space.micros in typical(y.space)
            }
            return when {
                cmp(input, leaderAeha) -> Aeha
                cmp(input, leaderNec) -> Nec
                cmp(input, leaderSirc) -> Sirc
                else -> Unknown(input)
            }
        }
    }
}

//
fun withTolerance(tolerance: Microseconds, x: Microseconds): IntRange =
    (-tolerance.micros + x.micros)..(x.micros + tolerance.micros)

//
@JvmInline
value class Bit(val bit: Int) {
    init {
        require(bit == 0 || bit == 1) {}
    }

    override fun toString(): String = if (bit == 0) "0" else "1"
}

//
typealias BitStream = NonEmptyList<Bit>

//
fun fromBinaryString(input: String): Either<String, BitStream> {
    fun toBit(idx: Int, c: Char): Either<String, Bit> {
        return when (c) {
            '0' -> Either.Right(Bit(0))
            '1' -> Either.Right(Bit(1))
            else -> {
                val pos = idx + 1
                Either.Left("unexpected char '$c' at $pos")
            }
        }
    }
    //
    val xs: List<Either<String, Bit>> = input.toCharArray().mapIndexed(::toBit)
    val ys: Either<String, List<Bit>> = xs.traverseEither { it }
    return ys.map { NonEmptyList.fromListUnsafe(it) }
}

//
fun fromMsbFirstHexadecimalString(input: String): Either<String, BitStream> {
    fun toBits(idx: Int, c: Char): Either<String, List<Bit>> {
        return when (c) {
            '0' -> Either.Right(listOf(Bit(0), Bit(0), Bit(0), Bit(0)))
            '1' -> Either.Right(listOf(Bit(0), Bit(0), Bit(0), Bit(1)))
            '2' -> Either.Right(listOf(Bit(0), Bit(0), Bit(1), Bit(0)))
            '3' -> Either.Right(listOf(Bit(0), Bit(0), Bit(1), Bit(1)))
            '4' -> Either.Right(listOf(Bit(0), Bit(1), Bit(0), Bit(0)))
            '5' -> Either.Right(listOf(Bit(0), Bit(1), Bit(0), Bit(1)))
            '6' -> Either.Right(listOf(Bit(0), Bit(1), Bit(1), Bit(0)))
            '7' -> Either.Right(listOf(Bit(0), Bit(1), Bit(1), Bit(1)))
            '8' -> Either.Right(listOf(Bit(1), Bit(0), Bit(0), Bit(0)))
            '9' -> Either.Right(listOf(Bit(1), Bit(0), Bit(0), Bit(1)))
            'a', 'A' -> Either.Right(listOf(Bit(1), Bit(0), Bit(1), Bit(0)))
            'b', 'B' -> Either.Right(listOf(Bit(1), Bit(0), Bit(1), Bit(1)))
            'c', 'C' -> Either.Right(listOf(Bit(1), Bit(1), Bit(0), Bit(0)))
            'd', 'D' -> Either.Right(listOf(Bit(1), Bit(1), Bit(0), Bit(1)))
            'e', 'E' -> Either.Right(listOf(Bit(1), Bit(1), Bit(1), Bit(0)))
            'f', 'F' -> Either.Right(listOf(Bit(1), Bit(1), Bit(1), Bit(1)))
            else -> {
                val pos = idx + 1
                Either.Left("unexpected char '$c' at $pos")
            }
        }
    }
    //
    val xs: List<Either<String, List<Bit>>> = input.toCharArray().mapIndexed(::toBits)
    val ys: Either<String, List<List<Bit>>> = xs.traverseEither { it }
    val zs: Either<String, List<Bit>> = ys.map { it.flatten() }
    return zs.map { NonEmptyList.fromListUnsafe(it) }
}

//
fun fromMsbFirstBitStream(input: BitStream): Int = input.fold(0, { acc, v -> acc * 2 + v.bit })

//
fun fromLsbFirstBitStream(input: BitStream): Int =
    input.reversed().fold(0, { acc, v -> acc * 2 + v.bit })

//
fun toLsbFirstBitStream(bitwidth: Int, input: Int): Option<BitStream> {
    var count = 0
    val seq = generateSequence { if (input shr (count++) and 1 != 0) Bit(1) else Bit(0) }
    return NonEmptyList.fromList(seq.take(bitwidth).toList())
}

//
fun demodulate(leader: InfraredLeader, xs: List<MarkAndSpace>): Option<List<Bit>> {
    // pulse distance modulation is NEC, AEHA
    fun pulseDistanceModulation(x: MarkAndSpace): Bit {
        return when {
            2 * x.mark.micros <= x.space.micros -> Bit(1)
            else -> Bit(0)
        }
    }

    // pulse width modulation is SIRC
    fun sircModulation(x: MarkAndSpace): Bit {
        // upper lower tolerance 0.1ms = 100us
        fun typical(x: Microseconds) = withTolerance(100.micros, x)
        return when (x.mark.micros in typical(1200.micros)) {
            true -> Bit(1)
            false -> Bit(0)
        }
    }
    //
    return when (leader) {
        is InfraredLeader.Aeha -> Some(xs.map(::pulseDistanceModulation))
        is InfraredLeader.Nec -> Some(xs.map(::pulseDistanceModulation))
        is InfraredLeader.Sirc -> Some(xs.map(::sircModulation))
        is InfraredLeader.Unknown -> None
    }
}

//
fun modulate(
    leader: InfraredLeader,
    bitstream: BitStream
): Either<String, NonEmptyList<MarkAndSpace>> {
    fun aehaModulation(bit: Bit): MarkAndSpace {
        val t = 425.micros
        return when (bit) {
            Bit(0) -> MarkAndSpace(t, t)
            else -> MarkAndSpace(t, t + t + t)
        }
    }

    fun necModulation(bit: Bit): MarkAndSpace {
        val t = 562.micros
        return when (bit) {
            Bit(0) -> MarkAndSpace(t, t)
            else -> MarkAndSpace(t, t + t + t)
        }
    }

    fun sircModulation(bit: Bit): MarkAndSpace {
        val t = 600.micros
        return when (bit) {
            Bit(0) -> MarkAndSpace(t, t)
            else -> MarkAndSpace(t, t + t)
        }
    }

    //
    fun go(
        leader: MarkAndSpace,
        f: (Bit) -> MarkAndSpace
    ): NonEmptyList<MarkAndSpace> {
        return nonEmptyListOf(leader) + bitstream.map { x -> f(x) }
    }

    //
    return when (leader) {
        is InfraredLeader.Aeha -> Either.Right(go(InfraredLeader.leaderAeha, ::aehaModulation))
        is InfraredLeader.Nec -> Either.Right(go(InfraredLeader.leaderNec, ::necModulation))
        is InfraredLeader.Sirc -> Either.Right(go(InfraredLeader.leaderSirc, ::sircModulation))
        is InfraredLeader.Unknown -> Either.Left("fail to modulate: unknown leader.")
    }
}

//
sealed class InfraredCodeFrame {

    data class ProtocolUnknown(val value: List<Bit>) : InfraredCodeFrame() {
        override fun toDigestString(): String {
            val xs = toListNonEmptyList(8, value)
                .map { fromLsbFirstBitStream(it).toString(16) }
                .map { "0x$it" }
            val ys = xs.joinToString(",")
            return "ProtocolUnknown: $ys"
        }
    }

    data class ProtocolAeha(val bitstream: BitStream) : InfraredCodeFrame() {
        override fun toDigestString(): String {
            val xs = toListNonEmptyList(8, bitstream)
                .map { fromLsbFirstBitStream(it).toString(16) }
                .map { "0x$it" }
            val ys = xs.joinToString(",")
            return "ProtocolAeha: $ys"
        }
    }

    data class ProtocolNec(
        val addressHi: BitStream,
        val addressLo: BitStream,
        val commandHi: BitStream,
        val commandLo: BitStream,
        val stop: Bit
    ) : InfraredCodeFrame() {
        override fun toDigestString(): String {
            val a = fromLsbFirstBitStream(addressHi).toString(16)
            val b = fromLsbFirstBitStream(addressLo).toString(16)
            val c = fromLsbFirstBitStream(commandHi).toString(16)
            val d = fromLsbFirstBitStream(commandLo).toString(16)
            return "ProtocolNec: 0x$a,0x$b,0x$c,0x$d"
        }
    }

    data class ProtocolSirc12(val command: BitStream, val address: BitStream) :
        InfraredCodeFrame() {
        override fun toDigestString(): String {
            val a = fromLsbFirstBitStream(command).toString(16)
            val b = fromLsbFirstBitStream(address).toString(16)
            return "ProtocolSirc12: 0x$a,0x$b"
        }
    }

    data class ProtocolSirc15(val command: BitStream, val address: BitStream) :
        InfraredCodeFrame() {
        override fun toDigestString(): String {
            val a = fromLsbFirstBitStream(command).toString(16)
            val b = fromLsbFirstBitStream(address).toString(16)
            return "ProtocolSirc15: 0x$a,0x$b"
        }
    }

    data class ProtocolSirc20(
        val command: BitStream,
        val address: BitStream,
        val extended: BitStream
    ) : InfraredCodeFrame() {
        override fun toDigestString(): String {
            val a = fromLsbFirstBitStream(command).toString(16)
            val b = fromLsbFirstBitStream(address).toString(16)
            val c = fromLsbFirstBitStream(extended).toString(16)
            return "ProtocolSirc20: 0x$a,0x$b,0x$c"
        }
    }

    //
    abstract fun toDigestString(): String
}

//
typealias Frame = List<MarkAndSpace>

// 第1,2,3...フレームを区切る時間(8ms = 8000us)
val threshold_frame_gap = Microseconds(8000)

// 入力を各フレームに分ける
fun decodePhase1(input: List<MarkAndSpace>): Either<String, List<Frame>> {
    fun go(xs: List<MarkAndSpace>): List<List<MarkAndSpace>> {
        // 入力を始めのフレームと残りに分ける
        val (fst, snd) = span(xs) { markandspace -> markandspace.space.micros < threshold_frame_gap.micros }
        val init = fst + snd.take(1)
        val rest = snd.drop(1)

        return when {
            init.isEmpty() -> listOf()
            else -> listOf(init) + go(rest)
        }
    }
    //
    return NonEmptyList.fromList(go(input)).toEither { "decodePhase1: input is empty" }
}

// 入力フレームをリーダ部とビット配列にする
fun decodePhase2(input: Frame): Either<String, Pair<InfraredLeader, List<Bit>>> {
    return runBlocking {
        either {
            val init =
                input.firstOrNone().toEither { "decodePhase2: Unexpected end of input" }.bind()
            val rest = input.drop(1)
            val leader = InfraredLeader.fromMarkAndSpace(init)
            val bs = demodulate(leader, rest).toList().flatten()
            Pair(leader, bs)
        }
    }
}

// 入力リーダ部とビット配列から赤外線信号にする
fun decodePhase3(input: Pair<InfraredLeader, List<Bit>>): Either<String, InfraredCodeFrame> {
    val (leader, bits) = input
    return when (leader) {
        is InfraredLeader.Aeha -> decodeAehaProtocol(bits)
        is InfraredLeader.Nec -> decodeNecProtocol(bits)
        is InfraredLeader.Sirc -> decodeSircProtocol(bits)
        else -> decodeUnknownProtocol(bits)
    }
}

//
fun decodeAehaProtocol(input: List<Bit>): Either<String, InfraredCodeFrame> {
    fun get(xs: List<Bit>): Option<BitStream> = NonEmptyList.fromList(xs)
    return get(input)
        .toEither { "fail to read: broken input (AEHA)" }
        .map { InfraredCodeFrame.ProtocolAeha(it) }
}

//
fun decodeNecProtocol(input: List<Bit>): Either<String, InfraredCodeFrame> {
    fun get(xss: List<List<Bit>>, index: Int): Option<BitStream> =
        NonEmptyList.fromList(xss[index])
    //
    val xss = toListList(8, input)
    return runBlocking {
        either {
            val custom0 = get(xss, 0).toEither { "fail to read: custom code0 (NEC)" }.bind()
            val custom1 = get(xss, 1).toEither { "fail to read: custom code1 (NEC)" }.bind()
            val data0 = get(xss, 2).toEither { "fail to read: data0 (NEC)" }.bind()
            val data1 = get(xss, 3).toEither { "fail to read: data1 (NEC)" }.bind()
            val stopbt = get(xss, 4).toEither { "fail to read: stop bit (NEC)" }.bind()
            InfraredCodeFrame.ProtocolNec(custom0, custom1, data0, data1, stopbt.first())
        }
    }
}

//
fun decodeSircProtocol(input: List<Bit>): Either<String, InfraredCodeFrame> {
    //
    suspend fun go(): Either<String, Either<String, InfraredCodeFrame>> {
        fun get(xs: List<Bit>): Option<BitStream> = NonEmptyList.fromList(xs)
        //
        return either {
            // 12-bit version, 7 command bits, 5 address bits.
            // 15-bit version, 7 command bits, 8 address bits.
            // 20-bit version, 7 command bits, 5 address bits, 8 extended bits.
            val comm =
                get(input.take(7)).toEither { "fail to read: command code (SIRC)" }.bind()
            val addr = get(input.drop(7)).toEither { "fail to read: address (SIRC)" }.bind()
            when (val width = comm.size + addr.size) {
                12 -> Either.Right(InfraredCodeFrame.ProtocolSirc12(comm, addr))
                15 -> Either.Right(InfraredCodeFrame.ProtocolSirc15(comm, addr))
                20 -> {
                    val first5bits = NonEmptyList.fromListUnsafe(addr.take(5))
                    val after5bits = NonEmptyList.fromListUnsafe(addr.drop(5))
                    Either.Right(InfraredCodeFrame.ProtocolSirc20(comm, first5bits, after5bits))
                }
                else -> Either.Left("fail to read: num of $width bits is not allowed (SIRC)")
            }
        }
    }
    //
    return runBlocking { go() }.flatten()
}

//
fun decodeUnknownProtocol(input: List<Bit>): Either<String, InfraredCodeFrame> {
    return Either.Right(InfraredCodeFrame.ProtocolUnknown(input))
}

//
fun decodeToIrCodeFrames(input: List<MarkAndSpace>): Either<String, NonEmptyList<InfraredCodeFrame>> {
    return decodePhase1(input)
        .flatMap { it.traverseEither(::decodePhase2) }
        .flatMap { it.traverseEither(::decodePhase3) }
        .flatMap { NonEmptyList.fromList(it).toEither { "decodeToIrCodeFrames: fail to decode" } }
}

//
fun decodeToIrCodeFrames2(input: List<MarkAndSpace>): Result<List<InfraredCodeFrame>> {
    return decodeToIrCodeFrames(input)
        .fold(
            ifLeft = { Result.failure(Exception(it)) },
            ifRight = { Result.success(it) }
        )
}

//
abstract class IrRemoteControlCode {
    abstract fun toDigestString(): String
}

data class IrRemoteUnknown(val value: List<InfraredCodeFrame>) : IrRemoteControlCode() {
    override fun toDigestString(): String {
        return this.toString()
    }
}

//
fun decodeToIrRemoteControlCode(input: List<MarkAndSpace>): Either<String, NonEmptyList<IrRemoteControlCode>> {
    val frames = decodeToIrCodeFrames(input)
    return frames.map(::decodePhase4)
}

// 各機種の赤外線信号にする
fun decodePhase4(frames: List<InfraredCodeFrame>): NonEmptyList<IrRemoteControlCode> {
    val seq = sequenceOf(
        decodePanasonicHvac(frames),
        decodeToshibaTv(frames),
    )
    return seq.dropWhile { it.isEmpty() }
        .toList()
        .flatMap { it.toList() }
        .getOrElse(0) { nonEmptyListOf(IrRemoteUnknown(frames)) }
}

//
fun encode(input: InfraredCodeFrame): Either<String, NonEmptyList<MarkAndSpace>> {
    return when (input) {
        is InfraredCodeFrame.ProtocolNec ->
            modulate(
                InfraredLeader.Nec,
                input.addressHi + input.addressLo + input.commandHi + input.commandLo + input.stop
            )
        is InfraredCodeFrame.ProtocolAeha -> modulate(InfraredLeader.Aeha, input.bitstream)
        is InfraredCodeFrame.ProtocolSirc12 ->
            modulate(InfraredLeader.Sirc, input.command + input.address)
        is InfraredCodeFrame.ProtocolSirc15 ->
            modulate(InfraredLeader.Sirc, input.command + input.address)
        is InfraredCodeFrame.ProtocolSirc20 ->
            modulate(InfraredLeader.Sirc, input.command + input.address + input.extended)
        else -> Either.Left("")
    }
}
