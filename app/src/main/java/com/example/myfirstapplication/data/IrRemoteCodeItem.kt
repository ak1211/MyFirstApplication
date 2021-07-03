package com.example.myfirstapplication.data

import com.example.infraredremote.MarkAndSpace

data class IrRemoteCodeItem(
    val id: Long,
    val name: String,
    val irRemoconCode: List<MarkAndSpace>
)