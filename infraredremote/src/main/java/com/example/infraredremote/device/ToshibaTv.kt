// Copyright (c) 2021 Akihiro Yamamoto.
// Licensed under the MIT License <https://spdx.org/licenses/MIT.html>
// See LICENSE file in the project root for full license information.
//
package com.example.infraredremote.device.ToshibaTv

import arrow.core.*
import arrow.core.computations.option
import com.example.infraredremote.*
import kotlinx.coroutines.runBlocking


//
sealed class ToshibaTv(val command: Int) : IrRemoteControlCode() {
    override fun toDigestString(): String = toString()

    object Switch : ToshibaTv(0x12ed)
    object InputSelection : ToshibaTv(0x0ff0)
    object SoundSelection : ToshibaTv(0x13ec)
    object ChannelBs : ToshibaTv(0x7c83)
    object ChannelCs : ToshibaTv(0x7d82)
    object Channel1 : ToshibaTv(0x619e)
    object Channel2 : ToshibaTv(0x629d)
    object Channel3 : ToshibaTv(0x639c)
    object Channel4 : ToshibaTv(0x649b)
    object Channel5 : ToshibaTv(0x659a)
    object Channel6 : ToshibaTv(0x6699)
    object Channel7 : ToshibaTv(0x6798)
    object Channel8 : ToshibaTv(0x6897)
    object Channel9 : ToshibaTv(0x6996)
    object Channel10 : ToshibaTv(0x6a95)
    object Channel11 : ToshibaTv(0x6b94)
    object Channel12 : ToshibaTv(0x6c93)
    object VolumeUp : ToshibaTv(0x1ae5)
    object VolumeDown : ToshibaTv(0x1ee1)
    object ChannelUp : ToshibaTv(0x1be4)
    object ChannelDown : ToshibaTv(0x1fe0)
    object Mute : ToshibaTv(0x10ef)
    data class Unknown(val v: Int) : ToshibaTv(-1)

    companion object {
        val Address = 0x40bf

        //
        fun fromInt(c: Int): ToshibaTv {
            return when (c) {
                Switch.command -> Switch
                InputSelection.command -> InputSelection
                SoundSelection.command -> SoundSelection
                ChannelBs.command -> ChannelBs
                ChannelCs.command -> ChannelCs
                Channel1.command -> Channel1
                Channel2.command -> Channel2
                Channel3.command -> Channel3
                Channel4.command -> Channel4
                Channel5.command -> Channel5
                Channel6.command -> Channel6
                Channel7.command -> Channel7
                Channel8.command -> Channel8
                Channel9.command -> Channel9
                Channel10.command -> Channel10
                Channel11.command -> Channel11
                Channel12.command -> Channel12
                VolumeUp.command -> VolumeUp
                VolumeDown.command -> VolumeDown
                ChannelUp.command -> ChannelUp
                ChannelDown.command -> ChannelDown
                Mute.command -> Mute
                else -> Unknown(c)
            }
        }
    }
}

//
fun decodeToshibaTv(input: List<InfraredCodeFrame>): Option<NonEmptyList<ToshibaTv>> {
    val frame = input.firstOrNull()
    if (frame is InfraredCodeFrame.ProtocolNec) {
        val bs = ::fromLsbFirstBitStream
        val id = bs(frame.addressHi) * 256 + bs(frame.addressLo)
        if (id == ToshibaTv.Address) {
            val data = bs(frame.commandHi) * 256 + bs(frame.commandLo)
            return Some(nonEmptyListOf(ToshibaTv.fromInt(data)))
        }
    }
    return None
}

//
fun encodeToshibaTv(input: ToshibaTv): List<InfraredCodeFrame> {
    return runBlocking {
        option {
            val bs8 = { v: Int -> toLsbFirstBitStream(8, v) }
            val a = bs8(ToshibaTv.Address shr 8 and 0xff).bind()
            val b = bs8(ToshibaTv.Address and 0xff).bind()
            val c = bs8(input.command shr 8 and 0xff).bind()
            val d = bs8(input.command and 0xff).bind()
            InfraredCodeFrame.ProtocolNec(a, b, c, d, Bit(1))
        }
    }.toList()
}