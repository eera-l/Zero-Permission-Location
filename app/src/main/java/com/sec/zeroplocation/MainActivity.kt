package com.sec.zeroplocation

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.*
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnWifi = findViewById<Button>(R.id.btn_wifi)
        val btnCell = findViewById<Button>(R.id.btn_cell)

        val txtGPS = findViewById<TextView>(R.id.txt_coordinates)
        val txtBSSID = findViewById<TextView>(R.id.txt_bssid)
        val txtCellID = findViewById<TextView>(R.id.txt_cellid)

        txtCellID.movementMethod = ScrollingMovementMethod()

        btnWifi.setOnClickListener {
            val wifiMgr =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiMgr.connectionInfo
            txtBSSID.text = wifiInfo.bssid.toString()
        }

        btnCell.setOnClickListener {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val cellLocation = telephonyManager.allCellInfo

            if (cellLocation != null) {
                txtCellID.text = cellLocation[0].toString()
            }

        }
    }
}
