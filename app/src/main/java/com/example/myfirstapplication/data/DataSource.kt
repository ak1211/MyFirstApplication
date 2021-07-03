package com.example.myfirstapplication.data

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DataSource(resources: Resources) {
    private val initialIrRemoteCodeList = listOf<IrRemoteCodeItem>(
        IrRemoteCodeItem(
            1, "照明OFF",
            IncomingMessage(
                "[3439,1764,416,457,414,456,416,1328,415,1328,416,458,413,1328,416,455,416,457,415,456,415,1327,416,456,414,457,415,1327,416,456,414,1328,416,456,415,1328,415,456,416,455,416,1328,415,457,415,456,415,457,415,456,415,1327,416,1328,415,1328,415,1328,415,456,415,1328,415,456,415,456,415,457,414,1328,415,1327,417,455,416,457,415,1327,416,457,414,456,415,65535,3440,1764,415,458,413,456,415,1329,414,1328,416,457,414,1328,415,457,415,456,415,458,413,1328,415,456,415,456,414,1328,416,457,415,1328,416,456,416,1328,415,456,415,456,415,1327,415,458,414,456,416,456,414,457,414,1327,416,1328,415,1328,416,1328,415,456,414,1329,415,457,415,457,415,456,415,1327,416,1327,415,458,414,457,415,1327,416,456,415,456,415]"
            ).decode()
        ), IrRemoteCodeItem(
            2, "照明ON普段",
            IncomingMessage(
                "[3440,1764,415,457,414,456,415,1329,415,1327,415,458,415,1328,415,457,414,455,417,457,414,1332,411,456,415,457,414,1326,417,456,416,1327,415,456,416,1327,415,456,415,456,416,1327,415,456,415,456,417,456,414,457,414,1327,417,457,415,1326,415,1328,416,457,415,1325,416,457,415,456,415,456,415,455,416,1328,415,456,414,454,417,1328,415,456,415,455,416,65535,3440,1764,415,457,415,455,415,1328,416,1327,416,456,416,1329,415,456,415,455,416,457,415,1327,415,456,416,456,416,1326,416,457,416,1296,446,456,415,1329,415,457,415,456,415,1328,416,457,415,456,414,455,416,457,416,1326,416,456,416,1328,415,1326,417,457,416,1326,416,456,416,456,415,456,414,455,416,1329,416,456,414,456,416,1328,416,456,414,455,416]"
            ).decode()
        ), IrRemoteCodeItem(
            3, "照明ON全灯",
            IncomingMessage(
                "[3441,1763,416,455,415,455,416,1328,416,1328,415,455,416,1328,416,455,415,455,416,456,416,1326,416,456,416,457,415,1326,416,456,416,1328,416,455,415,1328,416,456,415,456,414,1327,416,457,415,456,414,455,416,457,415,455,416,456,414,1327,416,1328,415,455,417,1327,416,456,414,455,417,1328,415,456,415,1327,417,455,416,456,414,1327,418,455,415,456,415,65535,3439,1763,416,456,416,456,415,1326,416,1328,416,457,414,1326,416,457,415,457,414,456,415,1328,416,457,415,456,415,1326,417,456,415,1327,415,456,416,1328,415,456,415,456,416,1326,417,456,415,456,416,457,416,456,414,456,416,456,415,1327,416,1328,416,457,415,1325,416,457,416,455,416,1326,416,457,415,1328,415,456,415,456,416,1328,414,456,415,456,416]"
            ).decode()
        ),
        IrRemoteCodeItem(
            4, "扇風機電源",
            IncomingMessage(
                "[3123,1674,376,1206,376,1205,377,416,374,416,374,416,373,1207,375,416,375,416,373,1207,374,1206,375,416,375,1206,375,416,374,417,375,1206,376,1206,376,415,374,1206,376,1205,377,417,373,1207,375,415,375,416,374,415,376,416,374,415,375,1205,377,416,373,417,375,415,374,1205,377,417,373,415,376,416,374,415,375,417,373,416,375,415,374,417,374,1206,376,1206,376,415,374,417,375,1206,377,414,375,417,374,414,376,1205,377,1206,375,416,373,417,374,415,375,417,374,415,375,417,373,414,376,417,374,415,375,416,373,416,375,1206,376,416,373,416,375,1206,376]"
            ).decode()
        ),
        IrRemoteCodeItem(
            5, "扇風機首振り",
            IncomingMessage(
                "[3171,1626,378,1201,380,1202,379,414,377,413,377,414,376,1204,377,414,377,412,377,1203,379,1202,379,414,376,1204,378,411,379,415,375,1204,378,1203,378,413,377,1203,379,1202,379,414,377,1205,376,413,378,414,376,412,379,413,377,413,378,1203,378,413,377,414,377,413,377,1204,377,414,378,412,377,414,377,412,378,414,376,413,378,414,376,413,378,1203,378,1203,378,414,377,413,377,1204,378,414,376,414,377,413,376,1204,377,1205,376,1205,376,414,377,413,377,415,377,413,377,415,375,412,378,415,375,413,378,414,376,414,376,1204,378,1203,378,414,376,1205,376]"
            ).decode()
        )
    )
    private val irRemoteCodeLiveData = MutableLiveData(initialIrRemoteCodeList)

    fun addIrRemoteCode(codeItem: IrRemoteCodeItem) {
        val currentList = irRemoteCodeLiveData.value
        if (currentList == null) {
            irRemoteCodeLiveData.postValue(listOf(codeItem))
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(0, codeItem)
            irRemoteCodeLiveData.postValue(updatedList)
        }
    }

    fun removeIrRemoteCode(ircode: IrRemoteCodeItem) {
        val currentList = irRemoteCodeLiveData.value
        if (currentList != null) {
            val updateList = currentList.toMutableList()
            updateList.remove(ircode)
            irRemoteCodeLiveData.postValue(updateList)
        }
    }

    fun updateIrRemoteCode(ircode: IrRemoteCodeItem) {
        val currentList = irRemoteCodeLiveData.value
        if (currentList != null) {
            val updateList = mutableListOf<IrRemoteCodeItem>()
            currentList.forEach { item ->
                if (item.id == ircode.id) {
                    updateList.add(ircode)
                } else {
                    updateList.add(item)
                }
            }
            irRemoteCodeLiveData.postValue(updateList)
        }
    }

    fun getIrRemoteCodeForId(id: Long): IrRemoteCodeItem? {
        irRemoteCodeLiveData.value?.let { ircodes ->
            return ircodes.firstOrNull { it.id == id }
        }
        return null
    }

    fun getIrRemoteList(): LiveData<List<IrRemoteCodeItem>> {
        return irRemoteCodeLiveData
    }

    companion object {
        private var INSTANCE: DataSource? = null

        fun getDataSource(resources: Resources): DataSource {
            return synchronized(DataSource::class) {
                val newInstance = INSTANCE ?: DataSource(resources)
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}