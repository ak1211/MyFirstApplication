package com.example.myfirstapplication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import com.example.myfirstapplication.data.IrRemoteCodeItem
import com.google.android.material.textfield.TextInputEditText
import android.util.Log

const val CHANGED_IR_REMOTE_CODES_ID = "changed ir remote codes id"

class IrRemoteCodeDetailActivity : AppCompatActivity() {
    private val irRemoconCodesViewModel by viewModels<IrRemoteCodesViewModel> {
        IrRemoconCodesViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_remote_code_detail)

        var currentIrRemoteCodeId: Long? = null
        //
        val id_text = findViewById<TextView>(R.id.detail_id_text)
        val name_text = findViewById<TextInputEditText>(R.id.detail_name_text_input)
        val detail_text = findViewById<TextView>(R.id.detail_codes_view)
        val done_button = findViewById<Button>(R.id.detail_done_button)
        val delete_button = findViewById<Button>(R.id.detail_delete_button)

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            currentIrRemoteCodeId = bundle.getLong(IR_REMOTE_CODE_ID)
        }

        currentIrRemoteCodeId?.let {
            val currentIrRemoteCode = irRemoconCodesViewModel.getIrRemoteCodeForId(it)
            currentIrRemoteCode?.let { item ->
                id_text.text = (item.id.toString())
                name_text.setText(item.name, TextView.BufferType.EDITABLE)
                val s =
                    item.irRemoconCode.map { ms ->
                        "${ms.mark.micros}, ${ms.space.micros}"
                    }
                detail_text.text = s.joinToString(", ")
                delete_button.setOnClickListener {
                    Log.v("Detail", "delete")
                    irRemoconCodesViewModel.removeIrRemoteCode(currentIrRemoteCode)
                    finish()
                }
                done_button.setOnClickListener {
                    val resultIntent = Intent()
                    val name = name_text.text.toString()
                    if (name.isNullOrEmpty()) {
                        setResult(Activity.RESULT_CANCELED, resultIntent)
                    } else {
                        val newCode = IrRemoteCodeItem(
                            currentIrRemoteCode.id,
                            name_text.text.toString(),
                            currentIrRemoteCode.irRemoconCode
                        )
                        irRemoconCodesViewModel.updateIrRemoteCode(newCode)
                        resultIntent.putExtra(CHANGED_IR_REMOTE_CODES_ID, newCode.id)
                        setResult(Activity.RESULT_OK, resultIntent)
                    }
                    finish()
                }
            }
        }
    }
}