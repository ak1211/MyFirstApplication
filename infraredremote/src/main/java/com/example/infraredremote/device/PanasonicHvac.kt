// Copyright (c) 2021 Akihiro Yamamoto.
// Licensed under the MIT License <https://spdx.org/licenses/MIT.html>
// See LICENSE file in the project root for full license information.
//
package com.example.infraredremote.device.PanasonicHvac

import kotlinx.coroutines.*
import arrow.core.*
import arrow.core.computations.option
import com.example.infraredremote.*
import kotlin.IllegalArgumentException
import kotlin.math.max
import kotlin.math.min

//
// Panasonic HVAC first frame value is
// LSB first                                    -- MSB first
// 0x02 20 e0 04 00 00 00 06                    -- 0x40 04 07 20 00 00 00 60
//
// first byte "01000000"
// LSB first                                    -- MSB first
// 1   2   4   8  16  32  64 128                -- 128  64  32  16   8   4   2   1
// |   |   |   |   |   |   |   |                --   |   |   |   |   |   |   |   |
// 0   1   0   0   0   0   0   0 == 02h         --   0   1   0   0   0   0   0   0 == 40h
//
// second byte "00000100"
// LSB first                                    -- MSB first
// 1   2   4   8  16  32  64 128                -- 128  64  32  16   8   4   2   1
// |   |   |   |   |   |   |   |                --   |   |   |   |   |   |   |   |
// 0   0   0   0   0   1   0   0 == 20h         --   0   0   0   0   0   1   0   0 == 04h
//
// 3rd byte "00000111"
// LSB first                                    -- MSB first
// 1   2   4   8  16  32  64 128                -- 128  64  32  16   8   4   2   1
// |   |   |   |   |   |   |   | 32+64+128=224  --   |   |   |   |   |   |   |   | 1+2+4=7
// 0   0   0   0   0   1   1   1 == e0h         --   0   0   0   0   0   1   1   1 == 07h
//
// 4th byte "00100000"
// LSB first                                    -- MSB first
// 1   2   4   8  16  32  64 128                -- 128  64  32  16   8   4   2   1
// |   |   |   |   |   |   |   |                --   |   |   |   |   |   |   |   |
// 0   0   1   0   0   0   0   0 == 04h         --   0   0   1   0   0   0   0   0 == 20h
//
// 5th byte "00000000"
// 6th byte "00000000"
// 7th byte "00000000"
//
// 8th byte "01100000"
// LSB first                                    -- MSB first
// 1   2   4   8  16  32  64 128                -- 128  64  32  16   8   4   2   1
// |   |   |   |   |   |   |   | 2+4=6          --   |   |   |   |   |   |   |   | 32+64=96
// 0   1   1   0   0   0   0   0 == 06h         --   0   1   1   0   0   0   0   0 == 60h
//
val constantFirstFrame: InfraredCodeFrame = InfraredCodeFrame.ProtocolAeha(
    nonEmptyListOf(
        "01000000", // Byte #1 -> 02h (LSB first) | 40h (MSB first)
        "00000100", // Byte #2 -> 20h (LSB first) | 04h (MSB first)
        "00000111", // Byte #3 -> e0h (LSB first) | 07h (MSB first)
        "00100000", // Byte #4 -> 04h (LSB first) | 20h (MSB first)
        "00000000", // Byte #5 -> 00h (LSB first) | 00h (MSB first)
        "00000000", // Byte #6 -> 00h (LSB first) | 00h (MSB first)
        "00000000", // Byte #7 -> 00h (LSB first) | 00h (MSB first)
        "01100000", // Byte #8 -> 06h (LSB first) | 60h (MSB first)
        "1"         // stop bit (>= 8ms trailer)
    ).traverseEither { fromBinaryString(it) }.map { it.flatten() }.orNull()!!
)

//
val typicalSecondFrame: InfraredCodeFrame = InfraredCodeFrame.ProtocolAeha(
    nonEmptyListOf(
        "01000000", // Byte #1 -> 00000010(bitflipped)
        "00000100", // Byte #2 -> 00100000(bitflipped)
        "00000111", // Byte #3 -> 11100000(bitflipped)
        "00100000", // Byte #4 -> 00000100(bitflipped)
        "00000000", // Byte #5 -> 0
        "10011100", // Byte #6 -> 00111001(bitflipped) mode[7:4] -> 0011(cool), switch[0] -> 1(on)
        "00101100", // Byte #7 -> 00110100(bitflipped)[4:1] -> 1010 -> 8+2 +(offset 16C) -> 26C
        "00000001",
        "11110101", // Byte #9 -> 10101111(bitflipped) fan[7:4] -> 1010(auto), swing[3:0] -> 1111(auto)
        "10110101",
        "00000000",
        "01110000",
        "00000111",
        "00000010", // Byte#14 -> 01000000(bitflipped) profile[7:0] -> 64
        "00000000",
        "01100001",
        "00000110",
        "00111001",
        "11111111", // Byte#19 -> checksum
    ).traverseEither { fromBinaryString(it) }.map { it.flatten() }.orNull()!!
)

//
enum class Mode(val v: Int) {
    Auto(0b0000),
    Dry(0b0010),
    Cool(0b0011),
    Heat(0b0100),
    Fan(0b0110);

    companion object {
        fun fromInt(i: Int): Mode {
            return values().firstOrNull { it.v == i }
                ?: throw IllegalArgumentException("had no values of enum")
        }
    }
}

//
enum class Switch(val v: Int) {
    On(1),
    Off(0);

    companion object {
        fun fromInt(i: Int): Switch {
            return values().firstOrNull { it.v == i }
                ?: throw IllegalArgumentException("had no values of enum")
        }
    }
}

//
enum class Swing(val v: Int) {
    Auto(0b1111),
    Horizontal(0b0001),
    Notch2(0x0010),
    Notch3(0b0011),
    Notch4(0b0100),
    Notch5(0b0101);

    companion object {
        fun fromInt(i: Int): Swing {
            return values().firstOrNull { it.v == i }
                ?: throw IllegalArgumentException("had no values of enum")
        }
    }
}

//
enum class Fan(val v: Int) {
    Auto(0b1010),
    Slowest(0b0011),
    Notch2(0b0100),
    Notch3(0b0101),
    Notch4(0b0110),
    Notch5(0b0111);

    companion object {
        fun fromInt(i: Int): Fan {
            return values().firstOrNull { it.v == i }
                ?: throw IllegalArgumentException("had no values of enum")
        }
    }
}

//
enum class Profile(profile: UInt) {
    Normal(0x10U),
    Boost(0x11U),
    Quiet(0x30U)
}

//
fun checkSum(input: List<BitStream>): UByte =
    input.take(18)
        .map(::fromLsbFirstBitStream)
        .reduce { acc, x -> (acc + x).mod(256) }
        .toUByte()

//
@JvmInline
value class Celsius(val celsius: Int)

//
data class PanasonicHvac(
    val temperature: Celsius,
    val mode: Mode,
    val switch: Switch,
    val swing: Swing,
    val fan: Fan,
    val profile: UByte,
    val crc: UByte
) : IrRemoteControlCode() {
    override fun toDigestString(): String {
        return this.toString()
    }
}

//
fun decode2ndframe(input: InfraredCodeFrame): Option<NonEmptyList<PanasonicHvac>> {
    suspend fun go() = when {
        input is InfraredCodeFrame.ProtocolAeha && input.bitstream.size >= 152 -> { // 152bits = 19bytes
            val bytes = toListNonEmptyList(8, input.bitstream)
            val byte_of_6th = bytes[6 - 1] // 第6バイトはbytes[5]なので
            val byte_of_7th = bytes[7 - 1]
            val byte_of_9th = bytes[9 - 1]
            val byte_of_14th = bytes[14 - 1]
            val byte_of_19th = bytes[19 - 1]
            //
            val temperature = fromLsbFirstBitStream(byte_of_7th) shr 1 and 0xf
            val mode = fromLsbFirstBitStream(byte_of_6th) shr 4 and 0xf
            val switch = fromLsbFirstBitStream(byte_of_6th) and 0x1
            val fan = fromLsbFirstBitStream(byte_of_9th) shr 4 and 0xf
            val swing = fromLsbFirstBitStream(byte_of_9th) and 0xf
            val profile = fromLsbFirstBitStream(byte_of_14th).toUByte()
            val crc = fromLsbFirstBitStream(byte_of_19th).toUByte()
            val result = option {
                val mode_ = Option.fromNullable(Mode.fromInt(mode)).bind()
                val switch_ = Option.fromNullable(Switch.fromInt(switch)).bind()
                val swing_ = Option.fromNullable(Swing.fromInt(swing)).bind()
                val fan_ = Option.fromNullable(Fan.fromInt(fan)).bind()
                PanasonicHvac(
                    Celsius(16 + temperature),
                    mode_,
                    switch_,
                    swing_,
                    fan_,
                    profile,
                    crc
                )
            }
            NonEmptyList.fromList(result.toList())
        }
        else ->
            None
    }
    return runBlocking { go() }
}

//
fun decodePanasonicHvac(input: List<InfraredCodeFrame>): Option<NonEmptyList<PanasonicHvac>> {
    val init: InfraredCodeFrame? = input.firstOrNull()
    val rest: List<InfraredCodeFrame> = input.drop(1)
    return when {
        init == constantFirstFrame && rest.isNotEmpty() -> {
            return decode2ndframe(rest.first())
        }
        else -> None
    }
}

//
fun encodePanasonicHvac(input: PanasonicHvac): List<InfraredCodeFrame> {
    val temperature:Int = min(max(input.temperature.celsius, 16), 30) // 16℃から30℃まで
    val mode = input.mode.v
    val switch = input.switch.v
    val fan = input.fan.v
    val swing = input.swing.v
    val profile = input.profile
    //
    val byte_of_6th = mode shl 4 or 0x8 or switch       // Bytes #6 -> mode[7:4], switch[0]
    val byte_of_7th = 0x10 or (temperature - 16) shl 1  // Bytes #7 -> 1[5] + temp[4:1]
    val byte_of_9th = (fan shl 4 or swing) and 0xff     // Bytes #9 -> fan[7:4], swing[3:0]
    val byte_of_14th = profile.toInt()                  // Bytes#14 -> profile[7:0]
    val byte_of_15th = 0                             // Bytes#15 -> ????

    val bitstream = (typicalSecondFrame as InfraredCodeFrame.ProtocolAeha).bitstream
    var bytes: MutableList<BitStream> = toListNonEmptyList(8, bitstream).toMutableList()
    fun f(x: Int): Option<BitStream> = toLsbFirstBitStream(8, x)

    // set
    f(byte_of_6th).map { x -> bytes.set(6 - 1, x) }
    f(byte_of_7th).map { x -> bytes.set(7 - 1, x) }
    f(byte_of_9th).map { x -> bytes.set(9 - 1, x) }
    f(byte_of_14th).map { x -> bytes.set(14 - 1, x) }
    f(byte_of_15th).map { x -> bytes.set(15 - 1, x) }
    // checksum
    val crc = checkSum(bytes).toInt()
    f(crc).map { x -> bytes.set(19 - 1, x) }

    val y: List<Bit> = bytes.flatten()
    val z: BitStream = NonEmptyList.fromListUnsafe(y)

    return listOf(constantFirstFrame, InfraredCodeFrame.ProtocolAeha(z))
}
