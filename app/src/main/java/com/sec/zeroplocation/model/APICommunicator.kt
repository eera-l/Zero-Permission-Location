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

    @Throws(KlaxonException::class)
    public fun sendGET(params: String, basePath : String, completionHandler: (response:Geolocation?) -> Unit)  {
        val requestQueue = VolleySingleton.instance?.requestQueue
        val jsonObjReq = object : JsonObjectRequest(Method.GET, basePath + params, null,
            Response.Listener<JSONObject> { response ->
                Log.d(TAG, "GET request OK. Response: $response")
                if (parseJSON(response)) completionHandler(geolocation)
                else completionHandler(null)
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "GET request fail. Error: ${error.message}")
                completionHandler(null)
            }) {

        }
        requestQueue?.add(jsonObjReq)
    }

    private fun parseJSON(json : JSONObject) : Boolean {
        try {
            val message = Klaxon()
                .parse<MessageResult>(json = json.toString())!!
            geolocation = message!!.data
            return true
        } catch (e: KlaxonException) {

        }
        return false
    }
}