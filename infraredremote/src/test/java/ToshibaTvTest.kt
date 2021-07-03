package com.example.infraredremote

import arrow.core.*
import com.example.infraredremote.device.ToshibaTv.ToshibaTv
import com.example.infraredremote.device.ToshibaTv.encodeToshibaTv
import org.junit.Test
import org.junit.Assert.*
import org.junit.Ignore


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ToshibaTvTest {

    @Ignore
    @Test
    fun test0() {
        val toshibatv = ToshibaTv.Switch
        // "東芝","デジタルテレビ1","電源",
        val inp =
            "5601A900180015001800140018001400190013001900140019001400170040001700150018003F0019003E0018003E0019003F0019003E00170040001800140019003E001800150018003F00180014001800140019003F0018001400170016001700150018003F001800140018003F0018003F001800140019003F0018003F0018003E0019004F03"
        val mss = from38khzOnOffPairHexString(inp)
        val out = mss.flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
        //
        val enc = encodeToshibaTv(toshibatv).map(::encode)
        assertEquals(listOf(mss), enc)
    }

    @Test
    fun test1() {
        val toshibatv = ToshibaTv.Switch
        // "東芝","デジタルテレビ1","電源",
        val inp =
            "5601A900180015001800140018001400190013001900140019001400170040001700150018003F0019003E0018003E0019003F0019003E00170040001800140019003E001800150018003F00180014001800140019003F0018001400170016001700150018003F001800140018003F0018003F001800140019003F0018003F0018003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test2() {
        val toshibatv = ToshibaTv.InputSelection
        // "東芝","デジタルテレビ1","入力切換",
        val inp =
            "5701A80019001400190013001900140018001400180015001700160017003F001900130019003F0018003E0019003E0019003F0018003E0019003E001900150017003F0018003F0019003E0019003E0018003F00190014001800140019001300180015001800140018001500170014001900140019003E0018003F0019003E0018003F0018004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test3() {
        val toshibatv = ToshibaTv.SoundSelection
        // "東芝","デジタルテレビ1","音声切換",
        val inp =
            "5601A90018001500180013001900140019001400180015001800140017003F001900130019003F0019003E0018003E001800400019003E0018003E001900150018003E001800400018003E00180014001900140019003E00180015001800130019001400190014001700150018003F0018003F001900130019003F0018003F0018003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test4() {
        val toshibatv = ToshibaTv.ChannelBs
        // "東芝","デジタルテレビ1","BS"
        val inp =
            "5601A90018001600170014001800150018001400180015001700150017003F001900140018003F0019003E0018003E0019003F0019003E0018003E001900150018003E00180016001700150017003F0019003E0019003E0018003F0019003E00190015001600400018003E00190015001800140018001500170015001700150018003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test5() {
        val toshibatv = ToshibaTv.ChannelCs
        // "東芝","デジタルテレビ1","CS"
        val inp =
            "5701A90018001500180013001900150018001400180015001700140018003F001900140018003F0019003E0018003E0019003F0019003E0018003E001900140019003E00180040001800140018003E0019003F0018003E0019003E0019003F00180014001800150018003E00190015001800140017001600170015001700140019003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test6() {
        val toshibatv = ToshibaTv.Channel1
        // "東芝","デジタルテレビ1","1"
        val inp =
            "5701A80019001500180014001800150018001400180015001700150017003F001900140018003E0018003E0019003E0019003F0018003E0019003E001900150018003E0018003F001900140018001500180013001900140018003E0019003E00190015001800150017003E0019003E0019003F0018003F00180014001800150018003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test7() {
        val toshibatv = ToshibaTv.Channel2
        //"東芝","デジタルテレビ1","2",
        val inp =
            "5601A90019001500170015001700150018001400180015001800150017003F001800150017003F0019003E0019003E0018003F0019003E0019003E001800150018003E001900150018003E0018001600170014001800150018003E0019003E001800150018003E001900150018003E0018003F0019003E00190014001800150017003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test8() {
        val toshibatv = ToshibaTv.Channel3
        //"東芝","デジタルテレビ1","3",
        val inp =
            "5601A90019001500170015001700150018001400180015001800140018003F001800150017003F0019003E0019003E0018003F0019003E0019003E001800150018003E0019003F0019003E0018001400180015001800150017003F0018003E0019001500180014001800150018003E0018003F0018003F00190014001800150017003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test9() {
        val toshibatv = ToshibaTv.Channel4
        // "東芝","デジタルテレビ1","4",
        val inp =
            "5601A90018001600170014001800150018001400180015001800150017003E001900140018003F0019003E0018003E0019003F0019003E0018003E001800160018003E00190015001700150017003F00190014001800150018003E0018003F001800150018003E0019003F001800140018003F0019003E00190014001700160017003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test10() {
        val toshibatv = ToshibaTv.Channel5
        // "東芝","デジタルテレビ1","5",
        val inp =
            "5601A90019001500170014001700150018001400180015001800150017003F001800150017003F0019003E0018003F0018003F0019003E0019003E001800160017003E0019003F001800150017003E00190015001800150018003E0019003D00190015001800140018003F001800150017003F0018003F00190014001700160017003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test11() {
        val toshibatv = ToshibaTv.Channel6
        // "東芝","デジタルテレビ1","6",
        val inp =
            "5601A90019001500180014001700150018001400180015001800140018003F001800150017003F0019003E0019003E0018003F0019003E0019003E001800160017003E001900150018003E0018003E00190015001800150017003F0018003E001900150018003E00190015001700150017003E0019003F00190014001700160017003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test12() {
        val toshibatv = ToshibaTv.Channel7
        // "東芝","デジタルテレビ1","7",
        val inp =
            "5601A90019001500170014001700150018001400180014001900150017003F001800150017003F0019003E0019003E0018003F0019003E0019003E001800160017003E0019003F0018003F0018003E00190015001800130019003F0018003E001900150018001400180015001700150017003F0018003F00190014001700160017003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test13() {
        val toshibatv = ToshibaTv.Channel8
        // "東芝","デジタルテレビ1","8",
        val inp =
            "5701A80019001500170015001800150017001500170015001800140018003F001900140018003E0019003E0018003F0019003F0018003E0019003E001800150018003E0019001500180014001800150018003E001700170017003E0019003E001800160017003E0019003F0019003E001800160016003F00190014001700160018003E0018004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test14() {
        val toshibatv = ToshibaTv.Channel9
        // "東芝","デジタルテレビ1","9",
        val inp =
            "5701A80019001500180014001800150017001500170016001700150017003F001900140018003F0018003E0019003E0019003F0018003E0019003E001900150017003F0018003F00190014001800140019003E001800160017003E0019003E00180016001700150017003F0019003E001900140018003E00190014001800150018003E0018004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test15() {
        val toshibatv = ToshibaTv.Channel10
        // "東芝","デジタルテレビ1","10",
        val inp =
            "5601A90018001600170014001800150018001400180015001700160017003E001900140018003F0019003E0018003E0019003F0019003E0018003D001900150018003E001900150017003E001900150018003E001900150018003E0018003E001900150018003E001800160017003E001900150018003E00180015001700160017003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test16() {
        val toshibatv = ToshibaTv.Channel11
        // "東芝","デジタルテレビ1","11",
        val inp =
            "5701A90018001500180014001800150018001400180015001800140017003F001900130019003F0019003E0018003E0019003F0019003E0018003E001900150018003E0018003F0019003E001900140018003F001800150017003F0019003E0019001400180015001700150018003E001900150017003F00190014001700140019003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test17() {
        val toshibatv = ToshibaTv.Channel12
        // "東芝","デジタルテレビ1","12",
        val inp =
            "5701A80019001500190013001800150017001500180014001800150017003F001900140018003F0018003E0019003E0019003F0018003E0019003E001900150017003E00190015001800140018003F0018003F001800150018003E0019003E001900150017003E0019003E00190015001700160017003E00190014001800150017003F0018004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test19() {
        val toshibatv = ToshibaTv.VolumeUp
        // "東芝","デジタルテレビ1","音量+",
        val inp =
            "5601A90019001500180014001700160017001500170015001800140018003F001900140017003F0019003E0019003E0018003F0019003E0019003E001900150017003E001900150018003E001800160017003E0019003E0019001500180014001800150017003E001900150018003E00190015001700140018003F0019003E0019003E0018004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test20() {
        val toshibatv = ToshibaTv.VolumeDown
        //"東芝","デジタルテレビ1","音量-",
        val inp =
            "5701A80019001500180014001800150018001400170015001800140018003F001900140017003F0019003E0019003E0018003F0019003E0019003E001900150017003E001900150018003E0019003E0018003F0019003E0019001500180014001700150018003E001900150018001400180015001700150017003F0019003E0019003E0018004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test21() {
        val toshibatv = ToshibaTv.ChannelUp
        // "東芝","デジタルテレビ1","チャンネル+",
        val inp =
            "5701A90018001500180014001800150018001400180015001800150016003F001900140018003F0019003E0018003E0019003F0019003E0018003E001900150018003E0019003E0019003E001900140018003F0017003F00190015001800130019001500180014001800150017003E00190015001800140018003F0018003E0019003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test22() {
        val toshibatv = ToshibaTv.ChannelDown
        // "東芝","デジタルテレビ1","チャンネル-",
        val inp =
            "5601A90018001500180014001800150017001400180015001800150017003F001800140018003F0019003E0018003E0019003F0019003E0018003E001900150018003E0019003F0019003D0019003E0019003F0018003E00190014001800150018001500180014001700160017001500170015001800140018003F0019003E0018003E0019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

    @Test
    fun test23() {
        val toshibatv = ToshibaTv.Mute
        // "東芝","デジタルテレビ1","消音",
        val inp =
            "5701A90018001500180014001800150017001500170016001700160016003F001900140018003F0018003E0019003E0019003F0018003E0019003E001900150018003E001800160017001400180015001800140018003F0018001500170015001800140018003F0019003E0018003E0019003F001900140018003F0018003E0019003E0018004F03000095025701530019004F03"
        val out = from38khzOnOffPairHexString(inp).flatMap(::decodeToIrRemoteControlCode)
        assertEquals(Either.Right(nonEmptyListOf(toshibatv)), out)
    }

}
