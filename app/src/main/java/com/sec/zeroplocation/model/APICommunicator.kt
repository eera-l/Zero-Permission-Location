package com.sec.zeroplocation.model

import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import org.json.JSONObject
import com.sec.zeroplocation.model.Geolocation

//Partially readapted from: https://www.varvet.com/blog/kotlin-with-volley/
class APICommunicator {
    val TAG = APICommunicator::class.java.simpleName
    lateinit var geolocation: Geolocation
    lateinit var addressInfo : Address

    public fun sendWiFiGET(params: String, basePath : String, completionHandler: (response:Geolocation?) -> Unit)  {
        val requestQueue = VolleySingleton.instance?.requestQueue
        val jsonObjReq = object : JsonObjectRequest(Method.GET, basePath + params, null,
            Response.Listener<JSONObject> { response ->
                Log.d(TAG, "GET request OK. Response: $response")
                if (parseWiFiJSON(response)) completionHandler(geolocation)
                else completionHandler(null)
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "GET request fail. Error: ${error.message}")
                completionHandler(null)
            }) {

        }
        requestQueue?.add(jsonObjReq)
    }

    public fun sendAddressGET(params: String, basePath : String, completionHandler: (response:Address?) -> Unit)  {
        val requestQueue = VolleySingleton.instance?.requestQueue
        val jsonObjReq = object : JsonObjectRequest(Method.GET, basePath + params, null,
            Response.Listener<JSONObject> { response ->
                Log.d(TAG, "GET request OK. Response: $response")
                if (parseAddressJSON(response)) completionHandler(addressInfo)
                else completionHandler(null)
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "GET request fail. Error: ${error.message}")
                completionHandler(null)
            }) {

        }
        requestQueue?.add(jsonObjReq)
    }

    private fun parseWiFiJSON(json : JSONObject) : Boolean {
        try {
            val message = Klaxon()
                .parse<MessageResult>(json = json.toString())!!
            geolocation = message!!.data
            return true
        } catch (e: KlaxonException) {

        }
        return false
    }

    private fun parseAddressJSON(json : JSONObject) : Boolean {
        val regex = "\\\"address\\\"\\:(.*?)\\}\\}\\]*".toRegex()
        var addresss =
            regex.find(json.toString())?.groups?.first()?.value!!.substringAfter(':')
        addresss = addresss.substringBefore("}]")
        Log.d(TAG, "address: " + addresss)
        try {
            val address = Klaxon()
                .parse<Address>(json = addresss)
            this.addressInfo = address!!
            return true
        } catch (e: KlaxonException) {
            e.printStackTrace()
        }
        return false
    }
}