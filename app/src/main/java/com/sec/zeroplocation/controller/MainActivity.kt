package com.sec.zeroplocation.controller

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.*
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sec.zeroplocation.R
import com.sec.zeroplocation.model.CellInfo
import java.lang.NullPointerException


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
            try {
                txtBSSID.text = wifiInfo.bssid.toString()
            } catch (e: NullPointerException) {
                Toast.makeText(this, "Please turn on the WiFi " +
                        "on your phone", Toast.LENGTH_LONG).show()
            }
        }

        btnCell.setOnClickListener {
            val regex =
                "CellIdentityWcdma:\\{ mMcc=\\d{3} mMnc=\\d{1,4} mLac=\\d{1,12} mCid=\\d{1,15} mPsc=\\d{1,5}\\}".toRegex()
            val telephonyManager : TelephonyManager
            val cellLocation :List<android.telephony.CellInfo>
            try {
                telephonyManager =
                    getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                if (telephonyManager.allCellInfo != null) {
                    cellLocation = telephonyManager.allCellInfo
                    val cellInfo = cellLocation[0].toString()
                    val wholeInfo = this@MainActivity.readRegex(cellInfo, regex)


                        txtCellID.text = "Mcc: ${wholeInfo.mcc}, " +
                                         "Mnc: ${wholeInfo.mnc}, " +
                                         "lac: ${wholeInfo.lac}, " +
                                         "cid: ${wholeInfo.cid}"
                } else {
                    Toast.makeText(this, "It looks like your phone " +
                            "does not have a SIM card", Toast.LENGTH_LONG).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(this, "Your phone model requires location " +
                        "authorization to access cell tower info", Toast.LENGTH_LONG).show()
            }

        }


    }
    private fun readRegex(cellInfo : String, regex : Regex) : CellInfo {

        val wholeInfo = CellInfo("", "", "", "")
        if (cellInfo.contains(regex)) {
            val match = regex.find(cellInfo)
            val infoString = match?.groups?.first()?.value

            val mccReg = "mMcc=\\d{3}".toRegex()
            val mncReg = "mMnc=\\d{1,4}".toRegex()
            val lacReg = "mLac=\\d{1,12}".toRegex()
            val cidReg = "mCid=\\d{1,15}".toRegex()

            val mcc =
                mccReg.find(infoString.toString())?.groups?.first()?.value!!.substringAfter('=')
            val mnc =
                mncReg.find(infoString.toString())?.groups?.first()?.value!!.substringAfter('=')
            val lac =
                lacReg.find(infoString.toString())?.groups?.first()?.value!!.substringAfter('=')
            val cid =
                cidReg.find(infoString.toString())?.groups?.first()?.value!!.substringAfter('=')

            wholeInfo.mcc = mcc
            wholeInfo.mnc = mnc
            wholeInfo.lac = lac
            wholeInfo.cid = cid
        }
        return wholeInfo
    }
}
