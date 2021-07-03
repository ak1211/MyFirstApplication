package com.example.myfirstapplication


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapplication.data.IrRemoteCodeItem
import com.example.myfirstapplication.data.OutgoingMessage

const val IR_REMOTE_CODE_ID = "ir remocon code id"

class MainActivity : AppCompatActivity() {
    val terminalFragmentTAG = "terminal"
    var recyclerView: RecyclerView? = null
    var concatAdapter: ConcatAdapter? = null

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            if (result?.resultCode == Activity.RESULT_OK) {
                Log.v("MainActivity", "OK")
            }
        }

    private val irRemoconCodesViewModel by viewModels<IrRemoteCodesViewModel> {
        IrRemoconCodesViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val headerAdapter = HeaderAdapter()
        val irRemoconCodeAdapter = IrRemoteCodeAdapter(::adapterOnClick, ::adapterOnLongClick)
        concatAdapter = ConcatAdapter(headerAdapter, irRemoconCodeAdapter)

        recyclerView = findViewById(R.id.recycler_view)

        recyclerView?.let {
            it.adapter = concatAdapter
            it.layoutManager =
                LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        }

        irRemoconCodesViewModel.irRemoteCodeLiveData.observe(this, {
            it?.let {
                irRemoconCodeAdapter.submitList(it as MutableList<IrRemoteCodeItem>)
                headerAdapter.updateIrRemoconCodeCount(it.size)
            }
        })

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainerView,
                    TerminalFragment.newInstance("param1"),
                    terminalFragmentTAG
                )
                .commit()
        }
    }

    private fun adapterOnClick(ircode: IrRemoteCodeItem) {
        val message = OutgoingMessage(ircode.irRemoconCode)
        val terminal =
            supportFragmentManager.findFragmentByTag("terminal") as TerminalFragment?
        terminal?.send(message)
    }

    private fun adapterOnLongClick(ircode: IrRemoteCodeItem): Boolean {
        val intent = Intent(this, IrRemoteCodeDetailActivity()::class.java)
        intent.putExtra(IR_REMOTE_CODE_ID, ircode.id)
        startForResult.launch(intent)
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        Log.v("MainActivity", "onNewIntent")
        intent?.also {
            if ("android.hardware.usb.action.USB_DEVICE_ATTACHED" == it.action) {
                Log.v("MainActivity", "DEVICE ATTACHED!!")
                val terminal =
                    supportFragmentManager.findFragmentByTag("terminal") as TerminalFragment?
                terminal?.status("USB device detected")
            }
        }
        super.onNewIntent(intent)
    }
}
