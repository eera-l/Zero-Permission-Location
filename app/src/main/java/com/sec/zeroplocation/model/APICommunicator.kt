package com.sec.zeroplocation.model

import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.beust.klaxon.Klaxon
import org.json.JSONObject
import com.sec.zeroplocation.model.Geolocation

//Partially readapted from: https://www.varvet.com/blog/kotlin-with-volley/
class APICommunicator {
    val TAG = APICommunicator::class.java.simpleName
    val basePath = "https://api.mylnikov.org/geolocation/wifi?v=1.1&data=open&bssid="

    public fun sendGET(params: String, completionHandler: (response:Geolocation?) -> Unit) {
        val requestQueue = VolleySingleton.instance?.requestQueue
        val jsonObjReq = object : JsonObjectRequest(Method.GET, basePath + params, null,
            Response.Listener<JSONObject> { response ->
                Log.d(TAG, "GET request OK. Response: $response")
                completionHandler(parseJSON(response))
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "GET request fail. Error: ${error.message}")
                completionHandler(null)
            }) {

        }
        requestQueue?.add(jsonObjReq)
    }

    private fun parseJSON(json : JSONObject) : Geolocation {
       val geoloc = Klaxon()
            .parse<Geolocation>(json = json.toString().substring(8,
            json.toString().lastIndex - 13))
        return geoloc!!
    }
}