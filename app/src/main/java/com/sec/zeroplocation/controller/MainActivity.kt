package com.sec.zeroplocation.controller

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sec.zeroplocation.R
import com.sec.zeroplocation.model.APICommunicator
import com.sec.zeroplocation.model.Address
import com.sec.zeroplocation.model.CellInfo
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.map.model.MapTilesType


class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName
    private lateinit var mapFragment: MapFragment
    private lateinit var tomtomMap: TomtomMap
    val wifipath = "https://api.mylnikov.org/geolocation/wifi?v=1.1&data=open&bssid="
    val geocode1 = "https://api.tomtom.com/search/2/reverseGeocode/"
    val geocode2 = ".JSON?key=WPtuRcLMvrphkNqHeYmHGo5SkK0YjLtu"

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnWifi = findViewById<Button>(R.id.btn_wifi)
        val btnCell = findViewById<Button>(R.id.btn_cell)

        initMap()

        btnWifi.setOnClickListener {
            val wifiMgr =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiMgr.connectionInfo
            try {
                Thread(Runnable {
                    val apiCommunicator = APICommunicator()
                    var wifiBssid = wifiInfo.bssid.toString()
                    // Test BSSID
                    wifiBssid = "00:0C:42:1F:65:E9"
                    wifiBssid = wifiBssid.trim()

                    apiCommunicator.sendWiFiGET(wifiBssid, wifipath) { response ->
                        if (response != null) {
                            mapFragment.getAsyncMap { tomtomMap ->
                                val position =
                                    LatLng(response!!.lat, response.lon)
                                val geol = "${response.lat},${response.lon}$geocode2"
                                apiCommunicator.sendAddressGET(geol, geocode1) { response1 ->
                                    val addressInfo = response1!!
                                    updateMap(position, tomtomMap, addressInfo)
                                }

                            }
                        } else {
                            Toast.makeText(this, "Your WiFi BSSID " +
                                    "was not found on the database", Toast.LENGTH_LONG).show()
                        }
                    } }).start()
            } catch (e: NullPointerException) {
                Toast.makeText(this, "Please turn on the WiFi " +
                        "on your phone", Toast.LENGTH_LONG).show()
            }
        }

        btnCell.setOnClickListener {
            mapFragment.getAsyncMap {tomtomMap ->
                val position =
                    LatLng(55.22007181031, 36.5464590362)
                //updateMap(position, tomtomMap)
            }
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
                        Log.d(TAG, "Mcc: ${wholeInfo.mcc}, " +
                                         "Mnc: ${wholeInfo.mnc}, " +
                                         "lac: ${wholeInfo.lac}, " +
                                         "cid: ${wholeInfo.cid}")
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
    private fun initMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment
        mapFragment.getAsyncMap(onMapReadyCallback)
    }

    private val onMapReadyCallback = OnMapReadyCallback { tomtomMap ->
        val position =
            LatLng(10.22007181031, -16.5464590362)
        tomtomMap.uiSettings.currentLocationView.show()
        tomtomMap.uiSettings.mapTilesType = MapTilesType.VECTOR
        tomtomMap.getUiSettings().setCameraPosition(
            CameraPosition
                .builder(position)
                .zoom(0.7)
                .bearing(0.0)
                .build()
        )
        tomtomMap.markerSettings.markerBalloonViewAdapter = TextBalloonViewAdapter()
    }

    private fun updateMap(position : LatLng, tomtomMap: TomtomMap, addressInfo: Address) : TomtomMap {
        if (!tomtomMap.markers.isEmpty()) {
            tomtomMap.removeMarkers()
        }
        tomtomMap.getUiSettings().setCameraPosition(
            CameraPosition
                .builder(position)
                .zoom(12.0)
                .bearing(0.0)
                .build()
        )
        val countryInfo = "${addressInfo.municipality}, " +
                "${addressInfo.countrySubdivision}, " +
                addressInfo.country
        val markerBuilder = MarkerBuilder(position)
            .markerBalloon(SimpleMarkerBalloon(countryInfo))
        tomtomMap.addMarker(markerBuilder).select()
        return tomtomMap
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
