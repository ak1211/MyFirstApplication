package com.example.infraredremote

import arrow.core.*
import org.junit.Test
import org.junit.Assert.*

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DecodeTest {
    // @formatter:off
    val ir_code = listOf(3453,1751,450,422,448,423,448,1294,425,1318,449,423,448,1294,449,422,450,423,447,423,448,1294,449,423,448,422,449,1293,450,422,448,1294,425,445,450,1294,448,423,449,424,447,1293,450,424,447,423,449,425,446,424,448,1293,451,1293,426,1317,449,1295,449,424,448,1294,448,423,1462,273,600,1150,762,643,788,423,665,230,486,1232,449,275,1597,1355,1846,1084,1872,1082,1871,1083,1875,62360,3454,1753,447,424,424,448,424,1319,424,1319,424,447,424,1319,424,449,446,426,422,447,424,1320,423,448,423,448,423,1319,424,450,421,1319,424,448,447,1295,424,447,424,473,399,1317,425,448,423,447,424,448,423,448,423,1320,424,1318,425,1318,424,1319,424,450,421,1319,424,448,424,447,424,473,398,1319,448,1295,424,448,424,448,423,1318,424,448,424,450,422,53941,1874,1104,1896,1058,1873,1110,1845,1083,1873,1083,1872,1083,1869,1086,1873)
    val json_ir_code = "[3453,1751,450,422,448,423,448,1294,425,1318,449,423,448,1294,449,422,450,423,447,423,448,1294,449,423,448,422,449,1293,450,422,448,1294,425,445,450,1294,448,423,449,424,447,1293,450,424,447,423,449,425,446,424,448,1293,451,1293,426,1317,449,1295,449,424,448,1294,448,423,1462,273,600,1150,762,643,788,423,665,230,486,1232,449,275,1597,1355,1846,1084,1872,1082,1871,1083,1875,62360,3454,1753,447,424,424,448,424,1319,424,1319,424,447,424,1319,424,449,446,426,422,447,424,1320,423,448,423,448,423,1319,424,450,421,1319,424,448,447,1295,424,447,424,473,399,1317,425,448,423,447,424,448,423,448,423,1320,424,1318,425,1318,424,1319,424,450,421,1319,424,448,424,447,424,473,398,1319,448,1295,424,448,424,448,423,1318,424,448,424,450,422,53941,1874,1104,1896,1058,1873,1110,1845,1083,1873,1083,1872,1083,1869,1086,1873]"
    val json_code_pairs = "[[3453, 1751], [450, 422], [448, 423], [448, 1294], [425, 1318], [449, 423], [448, 1294], [449, 422], [450, 423], [447, 423], [448, 1294], [449, 423], [448, 422], [449, 1293], [450, 422], [448, 1294], [425, 445], [450, 1294], [448, 423], [449, 424], [447, 1293], [450, 424], [447, 423], [449, 425], [446, 424], [448, 1293], [451, 1293], [426, 1317], [449, 1295], [449, 424], [448, 1294], [448, 423], [1462, 273], [600, 1150], [762, 643], [788, 423], [665, 230], [486, 1232], [449, 275], [1597, 1355], [1846, 1084], [1872, 1082], [1871, 1083], [1875, 62360], [3454, 1753], [447, 424], [424, 448], [424, 1319], [424, 1319], [424, 447], [424, 1319], [424, 449], [446, 426], [422, 447], [424, 1320], [423, 448], [423, 448], [423, 1319], [424, 450], [421, 1319], [424, 448], [447, 1295], [424, 447], [424, 473], [399, 1317], [425, 448], [423, 447], [424, 448], [423, 448], [423, 1320], [424, 1318], [425, 1318], [424, 1319], [424, 450], [421, 1319], [424, 448], [424, 447], [424, 473], [398, 1319], [448, 1295], [424, 448], [424, 448], [423, 1318], [424, 448], [424, 450], [422, 53941], [1874, 1104], [1896, 1058], [1873, 1110], [1845, 1083], [1873, 1083], [1872, 1083], [1869, 1086]]"
    // @formatter:on

    @Test
    fun decode_json_isEqual() {
        val parsed =
            Json.decodeFromString<List<Int>>(ListSerializer(Int.serializer()), json_ir_code)
        assertEquals(ir_code, parsed)
    }

    @Test
    fun test_fromBinaryString() {
        assertEquals(
            fromBinaryString("00011000").map(::fromMsbFirstBitStream),
            Either.Right(0b00011000)
        )
        assertEquals(
            fromBinaryString("10100011").map(::fromMsbFirstBitStream),
            Either.Right(0b10100011)
        )
        assertEquals(fromBinaryString("00000111").map(::fromMsbFirstBitStream), Either.Right(7))
        assertEquals(
            fromBinaryString("00011000").map(::fromLsbFirstBitStream),
            Either.Right(0b00011000)
        )
        assertEquals(
            fromBinaryString("10100011").map(::fromLsbFirstBitStream),
            Either.Right(0b11000101)
        )
        assertEquals(fromBinaryString("01000000").map(::fromLsbFirstBitStream), Either.Right(0x02))
        assertEquals(fromBinaryString("00000100").map(::fromLsbFirstBitStream), Either.Right(0x20))
        assertEquals(fromBinaryString("00000111").map(::fromLsbFirstBitStream), Either.Right(0xe0))
        assertEquals(fromBinaryString("00100000").map(::fromLsbFirstBitStream), Either.Right(0x04))
        assertEquals(fromBinaryString("01100000").map(::fromLsbFirstBitStream), Either.Right(0x06))
    }

    @Test
    fun mark_and_spaces_isGood() {
        val a = Json.decodeFromString<List<List<Int>>>(
            ListSerializer(ListSerializer(Int.serializer())),
            json_code_pairs
        ).map { (mark, space) -> MarkAndSpace(mark.micros, space.micros) }
        assertEquals(fromIntList(ir_code), a)
    }

    @Test
    fun decode_phase1_isGood() {
        val inp = listOf(3453, 1751, 450, 65535, 448, 423, 448, 8000, 425, 1318)
        val expect = Either.Right(
            listOf(
                fromIntList(listOf(3453, 1751, 450, 65535)),
                fromIntList(listOf(448, 423, 448, 8000)),
                fromIntList(listOf(425, 1318))
            )
        )
        val markAndSpaces = fromIntList(inp)
        val xs = decodePhase1(markAndSpaces)
        assertEquals(expect, xs)
    }

    @Test
    fun decode_phase2_isGood() {
        val inp = listOf(3453, 1751, 450, 422, 448, 423, 448, 1294)
        val expect =
            Either.Right(
                listOf(
                    Pair(InfraredLeader.Aeha, listOf(Bit(0), Bit(0), Bit(1)))
                )
            )
        val markAndSpaces = fromIntList(inp)
        val xs = decodePhase1(markAndSpaces)
        val ys = xs.flatMap { it.traverseEither(::decodePhase2) }
        assertEquals(expect, ys)
    }

    @Test
    fun decode_phase3_NEC_isGood() {
        // @formatter:off
                val code = "[8971,4521,557,1690,558,581,558,1689,560,578,556,582,557,580,558,1690,534,603,559,578,557,580,557,1692,558,1690,558,1689,559,1690,561,576,558,1690,558,579,556,1692,559,578,556,581,558,1690,559,580,558,579,556,580,557,1690,560,578,557,1690,559,1689,553,584,558,1689,559,1692,557,1690,557,39741,8970,2273,559]"
                val inp = Json.decodeFromString<List<Int>>(ListSerializer(Int.serializer()), code)
                // @formatter:on
        val expect =
            Either.Right(
                InfraredCodeFrame.ProtocolNec(
                    fromBinaryString("10100010").orNull()!!,
                    fromBinaryString("00111101").orNull()!!,
                    fromBinaryString("01001000").orNull()!!,
                    fromBinaryString("10110111").orNull()!!,
                    Bit(1)
                )
            )
        val a: List<MarkAndSpace> = fromIntList(inp)
        val b: Either<String, List<List<MarkAndSpace>>> = decodePhase1(a)
        val c: Either<String, List<Pair<InfraredLeader, List<Bit>>>> =
            b.flatMap { it.traverseEither(::decodePhase2) }
        val d: Either<String, Pair<InfraredLeader, List<Bit>>> = c.map { it.first() }
        val e: Either<String, InfraredCodeFrame> = d.flatMap { decodePhase3(it) }
        assertEquals(expect, e)
    }

    @Test
    fun decode_phase3_AEHA_isGood() {
        val expect =
            Either.Right(
                InfraredCodeFrame.ProtocolAeha(
                    nonEmptyListOf(
                        fromBinaryString("00110100").orNull()!!,
                        fromBinaryString("01001010").orNull()!!,
                        fromBinaryString("10010000").orNull()!!,
                        fromBinaryString("11110100").orNull()!!,
                        fromBinaryString("00001000").orNull()!!,
                        fromBinaryString("001").orNull()!!
                    ).flatten(),
                )
            )
        val a: List<MarkAndSpace> = fromIntList(ir_code)
        val b: Either<String, List<List<MarkAndSpace>>> = decodePhase1(a)
        val c: Either<String, List<Pair<InfraredLeader, List<Bit>>>> =
            b.flatMap { it.traverseEither(::decodePhase2) }
        val d: Either<String, Pair<InfraredLeader, List<Bit>>> = c.map { it.first() }
        val e: Either<String, InfraredCodeFrame> = d.flatMap { decodePhase3(it) }
        assertEquals(expect, e)
    }
}
