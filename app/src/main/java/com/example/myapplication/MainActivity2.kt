package com.example.myapplication

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.example.myapplication.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


class MainActivity2 : ComponentActivity() {
    private lateinit var layout:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        layout = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        setContentView(layout.root)
        actionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#3D0D42")))

        layout.scanButton.setOnClickListener {
            startScan()
        }
        layout.nextButton.setOnClickListener {
            val text= layout.field.text.toString()
            if (text.startsWith("0x") && text.length == 42){
                val intent= Intent(this,MainActivity3::class.java)
                intent.putExtra("address",text)
                startActivity(intent)
            }
            else{
                Toast.makeText(this,"Please enter a valid address",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun startScan(){
        val options = ScanOptions()
        options.setPrompt("Volume up to flash on")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.setCaptureActivity(CaptureAct::class.java)
        qrLaucher.launch(options)
    }

    private var qrLaucher: ActivityResultLauncher<ScanOptions> =
        registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                layout.field.setText(result.contents.toString())

            }
        }


    /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
    */


}