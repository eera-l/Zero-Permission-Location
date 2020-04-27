package com.sec.zeroplocation.controller

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sec.zeroplocation.R
import com.sec.zeroplocation.model.APICommunicator
import com.sec.zeroplocation.model.CSVreader
import com.sec.zeroplocation.model.CellInfo
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.map.model.MapTilesType
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var mapFragment: MapFragment
    private lateinit var tomtomMap: TomtomMap
    private lateinit var textInfo : TextView
    val wifipath = "https://api.mylnikov.org/geolocation/wifi?v=1.1&data=open&bssid="
    val geocode1 = "https://api.tomtom.com/search/2/reverseGeocode/"
    val geocode2 = ".JSON?key=WPtuRcLMvrphkNqHeYmHGo5SkK0YjLtu"
    private lateinit var apiCommunicator: APICommunicator

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnWifi = findViewById<Button>(R.id.btn_wifi)
        val btnCell = findViewById<Button>(R.id.btn_cell)

        textInfo = findViewById(R.id.txt_info)
        textInfo.visibility = View.INVISIBLE
        apiCommunicator = APICommunicator()
        initMap()

        btnWifi.setOnClickListener {
            val wifiMgr =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiMgr.connectionInfo
            try {
                Thread(Runnable {
                    var wifiBssid = wifiInfo.bssid.toString()
                    // Test BSSID
                    //wifiBssid = "38:1C:1A:2F:47:D0"
                    wifiBssid = wifiBssid.trim()

                    apiCommunicator.sendWiFiGET(wifiBssid, wifipath) { response ->
                        if (response != null) {
                            mapFragment.getAsyncMap { tomtomMap ->
                                val position =
                                    LatLng(response!!.lat, response.lon)
                                val geol = "${response.lat},${response.lon}$geocode2"
                                apiCommunicator.sendAddressGET(geol, geocode1) { response1 ->
                                    lateinit var countryInfo : String
                                    if (response1 != null) {
                                        val addressInfo = response1!!
                                        countryInfo = "${addressInfo.freeformAddress}, \n" +
                                                addressInfo.country + ".\nLat.: ${position.latitude}" +
                                                "\nLon.: ${position.longitude}"

                                    } else {
                                        countryInfo = "Lat.: ${position.latitude}" +
                                                "\nLon.: ${position.longitude}"
                                    }
                                    updateMap(position, countryInfo)
                                    textInfo.visibility = View.VISIBLE
                                    textInfo.text = "Your WiFi access point is located in: "
                                }

                            }
                        } else {
                            Toast.makeText(this, "Your WiFi BSSID " +
                                    "could not be found on the database", Toast.LENGTH_LONG).show()
                        }
                    } }).start()
            } catch (e: NullPointerException) {
                Toast.makeText(this, "Please turn on the WiFi " +
                        "on your phone", Toast.LENGTH_LONG).show()
            }
        }

        btnCell.setOnClickListener {
            val regex =
                "CellIdentity\\w{3,5}:\\{ mMcc=\\d{3} mMnc=\\d{1,4} mLac=\\d{1,12} mCid=\\d{1,15} mPsc=\\d{1,5}\\}".toRegex()
            val telephonyManager : TelephonyManager
            try {
                telephonyManager =
                    getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                if (telephonyManager.allCellInfo != null && telephonyManager.allCellInfo.size > 0) {
                    val cellLocation = telephonyManager.allCellInfo
                    val cellInfo = cellLocation[0].toString()
                    val wholeInfo = this@MainActivity.readRegex(cellInfo, regex)
                    val cReader = CSVreader()
                    val file = File( assets.open("cell_got.csv").bufferedReader().use { it.readText() }).toString()
                    val rows =
                        cReader.readWithHeader(file)
                    var idx = -1
                    for (i in rows.indices) {
                        if (rows[i]["mcc"] == wholeInfo.mcc &&
                                rows[i]["mnc"] == wholeInfo.mnc &&
                                rows[i]["lac"] == wholeInfo.lac &&
                                rows[i]["cellid"] == wholeInfo.cid) {
                            idx = i
                            break
                        }
                    }

                    if (idx != -1) {
                        Thread(Runnable {
                            val lat = rows[idx]["lat"]
                            val lon = rows[idx]["lon"]
//                            val lat = 59.3402375
//                            val lon = 18.0300585
                            val geol = "${lat},${lon}$geocode2"
                            apiCommunicator.sendAddressGET(geol, geocode1) { response1 ->
                                lateinit var countryInfo: String
                                if (response1 != null) {
                                    val addressInfo = response1!!
                                    countryInfo = "${addressInfo.freeformAddress}, \n" +
                                            addressInfo.country + ".\nLat.: $lat" +
                                            "\nLon.: $lon"

                                } else {
                                    countryInfo = "Lat.: $lat" +
                                            "\nLon.: $lon"
                                }
                                val position = LatLng(lat!!.toDouble(), lon!!.toDouble())
                                updateMap(position, countryInfo)
                                textInfo.visibility = View.VISIBLE
                                textInfo.text = "Your cell tower is located in: "
                        }
                        }).start()
                    } else {
                        Toast.makeText(this, "Your cell tower ID " +
                                "could not be found on the database", Toast.LENGTH_LONG).show()
                    }
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
        this.tomtomMap = tomtomMap
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
        tomtomMap.addOnMapPanningListener(removeTextInfoOnMapPanningListener)
    }

    private val removeTextInfoOnMapPanningListener: TomtomMapCallback.OnMapPanningListener = object : TomtomMapCallback.OnMapPanningListener {
        override fun onMapPanningOngoing() {
            textInfo.visibility = View.INVISIBLE
        }

        override fun onMapPanningStarted() {
            textInfo.visibility = View.INVISIBLE
            if (!tomtomMap.markers.isEmpty()) {
                tomtomMap.removeMarkers()
            }
        }
        override fun onMapPanningEnded() {
            textInfo.visibility = View.INVISIBLE
        }
    }

    private fun updateMap(position : LatLng, countryInfo : String) : TomtomMap {
        if (!tomtomMap.markers.isEmpty()) {
            tomtomMap.removeMarkers()
        }
        tomtomMap.getUiSettings().setCameraPosition(
            CameraPosition
                .builder(position)
                .zoom(10.0)
                .bearing(0.0)
                .build()
        )

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
