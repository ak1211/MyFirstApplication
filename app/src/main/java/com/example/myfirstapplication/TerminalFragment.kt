package com.example.myfirstapplication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.infraredremote.*
import com.example.myfirstapplication.data.IncomingMessage
import com.example.myfirstapplication.data.OutgoingMessage
import com.hoho.android.usbserial.driver.*
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.IOException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val WRITE_WAIT_MILLIS = 2000
private const val READ_WAIT_MILLIS = 2000
private const val INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB"

/**
 * A simple [Fragment] subclass.
 * Use the [TerminalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class TerminalFragment : Fragment(), SerialInputOutputManager.Listener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var terminalText: TextView? = null
    private var incomingMessage: IncomingMessage = IncomingMessage()
    private val mainLooper: Handler = Handler(Looper.getMainLooper())

    private enum class UsbPermission { Unknown, Requested, Granted, Denied }

    private var usbSerialPort: UsbSerialPort? = null
    private var usbConnection: UsbDeviceConnection? = null
    private var usbPermission: UsbPermission = UsbPermission.Unknown
    private var usbIoManager: SerialInputOutputManager? = null
    private var connected: Boolean = false
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Log.v("TerminalFragment", "onReceive")
            if (INTENT_ACTION_GRANT_USB == intent.action) {
                usbPermission =
                    if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        UsbPermission.Denied
                    } else {
                        UsbPermission.Granted
                    }
                connect()
            }
        }
    }

    private val irRemoconCodesViewModel by viewModels<IrRemoteCodesViewModel> {
        IrRemoconCodesViewModelFactory(requireContext())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment TerminalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            TerminalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }

        Log.v("TerminalFragment", "onCreate")
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(broadcastReceiver, IntentFilter(INTENT_ACTION_GRANT_USB))
        when (usbPermission) {
            UsbPermission.Unknown, UsbPermission.Granted -> {
                mainLooper.post(this::connect)
            }
            UsbPermission.Requested, UsbPermission.Denied -> {
                //No operation
            }
        }
    }

    override fun onPause() {
        if (connected) {
            status("disconnected")
            disconnect()
        }
        activity?.unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)
        terminalText = view.findViewById(R.id.terminal_text) as TextView

        return view
    }

    override fun onNewData(data: ByteArray) {
        Log.v("TerminalFragment", "onNewData")
        mainLooper.post { receive(data) }
    }

    override fun onRunError(e: Exception?) {
        mainLooper.post {
            status("connection lost: " + e?.message)
            disconnect()
        }
    }

    private fun connect() {
        Log.v("TerminalFragment", "connect")
        // Find all available drivers from attached devices.
        val manager = activity?.getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = CustomProber.getCustomProber().findAllDrivers(manager)

        if (availableDrivers.isEmpty()) {
            status("connection failed: no driver for device")
            return
        }

        for (v in availableDrivers) {
            Log.v("TerminalFragment", "deviceClass: " + v.device.deviceClass)
            Log.v("TerminalFragment", "deviceSubClass: " + v.device.deviceSubclass)
            Log.v("TerminalFragment", "vendorId: 0x" + v.device.vendorId.toString(16))
            Log.v("TerminalFragment", "productId: 0x" + v.device.productId.toString(16))
            v.device.manufacturerName?.let { Log.v("TerminalFragment", "manufacturerName: $it") }
            v.device.productName?.let { Log.v("TerminalFragment", "productName: $it") }
        }

        // Open a connection to the first available driver.
        val driver: UsbSerialDriver = availableDrivers[0]

        usbConnection = manager.openDevice(driver.device)
        if (usbConnection == null) {
            when {
                manager.hasPermission(driver.device) -> {
                    status("connection failed: open failed")
                }
                usbPermission === UsbPermission.Unknown -> {
                    usbPermission = UsbPermission.Requested
                    val usbPermissionIntent =
                        PendingIntent.getBroadcast(activity, 0, Intent(INTENT_ACTION_GRANT_USB), 0)
                    manager.requestPermission(driver.device, usbPermissionIntent)
                }
                else -> {
                    status("connection failed: permission denied")
                }
            }
            return
        }
        try {
            usbSerialPort = driver.ports[0] // Most devices have just one port (port 0)
            usbIoManager = usbSerialPort?.let { port ->
                port.open(usbConnection)
                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                port.dtr = true
                SerialInputOutputManager(port, this)
            }
            usbIoManager?.start()

            connected = true
            var productName = driver.device.productName.orEmpty()
            if (productName.isEmpty()) {
                status("connected")
            } else {
                status("connect to \"$productName\"")
                Toast.makeText(activity, "connect to \"$productName\"", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            status("connection failed: " + e.message)
        }
    }

    private fun disconnect() {
        connected = false
        usbIoManager?.also {
            it.listener = null
            it.stop()
        }
        usbIoManager = null
        try {
            usbSerialPort?.close()
        } catch (ignored: IOException) {
        }
        usbSerialPort = null
    }

    fun send(str: String) {
        if (!connected) {
            Toast.makeText(activity, "not connected", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(activity, "send", Toast.LENGTH_LONG).show()
        try {
            val data: ByteArray = (str + '\n').toByteArray(Charsets.UTF_8)
            Toast.makeText(activity, "send " + data.size + " bytes\n", Toast.LENGTH_SHORT).show()
            usbSerialPort!!.write(data, WRITE_WAIT_MILLIS)
        } catch (e: Exception) {
            onRunError(e)
        }
    }

    fun send(message: OutgoingMessage) {
        message
            .encode()
            .fold(
                onSuccess = { send(it) },
                onFailure = { onRunError(it as Exception) }
            )
    }

    fun read() {
        if (!connected) {
            Toast.makeText(activity, "not connected", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val buffer = ByteArray(8192)
            val len = usbSerialPort!!.read(buffer, READ_WAIT_MILLIS)
            Log.v("TerminalFragment", len.toString())
            receive(buffer.copyOf(len))
        } catch (e: IOException) {
            status("connection lost: " + e.message)
            disconnect()
        }
    }


    private fun receive(data: ByteArray) {
        val spn = SpannableStringBuilder()
        Toast.makeText(activity, "receive " + data.size + " bytes\n", Toast.LENGTH_SHORT).show()
        if (data.isNotEmpty()) {
            spn.append(data.toString(Charsets.UTF_8))
        }
        terminalText?.append(spn)
        val str: String = data.toString(Charsets.UTF_8)
        incomingMessage = incomingMessage + str
        if (incomingMessage.incoming.find { it == '\r' || it == '\n' } != null) {
            val xs = incomingMessage.decode()
            incomingMessage.incoming.clear()
            if (xs.isNotEmpty()) {
                incomingMessage.incoming.clear()
                digestFromMarkAndSpaces2(xs)
                    .fold(
                        onSuccess = {
                            // Listに追加
                            irRemoconCodesViewModel.insertIrRemoteCode(it, xs)
                            status("\n")
                            status("incoming message: \"${it}\"")
                        },
                        onFailure = {
                            status("\n")
                            status("failure: \"${it.message}\"")
                        }
                    )
            }
        }
    }

    fun status(str: String) {
        val spn = SpannableStringBuilder(str + '\n')
        spn.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorStatusText
                )
            ),
            0,
            spn.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        Log.v("TerminalFragment", "status")
        terminalText?.append(spn)
    }
}