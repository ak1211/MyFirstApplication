package com.example.myfirstapplication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.infraredremote.*
import com.example.myfirstapplication.data.DataSource
import com.example.myfirstapplication.data.IrRemoteCodeItem


class IrRemoteCodesViewModel(val dataSource: DataSource) : ViewModel() {
    val irRemoteCodeLiveData = dataSource.getIrRemoteList()

    fun getIrRemoteCodeForId(id: Long): IrRemoteCodeItem? {
        return dataSource.getIrRemoteCodeForId(id)
    }

    fun removeIrRemoteCode(ircode: IrRemoteCodeItem) {
        dataSource.removeIrRemoteCode(ircode)
    }

    fun updateIrRemoteCode(ircode: IrRemoteCodeItem) {
        dataSource.updateIrRemoteCode(ircode)
    }

    fun insertIrRemoteCode(irName: String?, irRemoconCode: List<MarkAndSpace>) {
        val name = if (irName != null) {
            irName
        } else {
            digestFromMarkAndSpaces2(irRemoconCode).getOrNull()
        }
        val maximum = irRemoteCodeLiveData
            .value
            ?.maxByOrNull { it.id }
            ?.let { it.id + 1 }
            ?: 1L
        val newCode = IrRemoteCodeItem(maximum, name ?: "なんかのリモコン", irRemoconCode)
        dataSource.addIrRemoteCode(newCode)
    }
}

class IrRemoconCodesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IrRemoteCodesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IrRemoteCodesViewModel(
                dataSource = DataSource.getDataSource(context.resources)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}