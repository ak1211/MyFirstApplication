package com.example.myfirstapplication

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialProber

class CustomProber {
    companion object {
        fun getCustomProber(): UsbSerialProber {
            val customTable = ProbeTable()
                .addProduct(0x2886, 0x002f, CdcAcmSerialDriver::class.java)
            return UsbSerialProber(customTable)
        }
    }
}
