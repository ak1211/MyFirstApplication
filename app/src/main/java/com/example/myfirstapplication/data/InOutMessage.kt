package com.example.myfirstapplication.data

import android.util.Log
import com.example.infraredremote.MarkAndSpace
import com.example.infraredremote.fromIntList
import com.example.infraredremote.toIntList
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

data class IncomingMessage(val incoming: StringBuilder = StringBuilder()) {

    constructor(s: String) : this() {
        incoming.append(s)
    }

    operator fun plus(y: String): IncomingMessage {
        return IncomingMessage(incoming.append(y))
    }

    fun lines(): List<String> {
        val s = incoming.toString()
        return s.split("\n")
    }

    fun decode(): List<MarkAndSpace> {
        return lines().firstOrNull()
            ?.let {
                runCatching {
                    Json.decodeFromString(ListSerializer(Int.serializer()), it)
                }.map(::fromIntList)
                    .getOrNull()
            }
            ?: listOf()
    }
}

data class OutgoingMessage(val outgoing: List<MarkAndSpace>) {

    fun encode(): Result<String> {
        val s = toIntList(outgoing)
        Log.d("Message", s.toString())
        return runCatching {
            val x = Json.encodeToString(ListSerializer(Int.serializer()), s)
            x
        }
    }
}

