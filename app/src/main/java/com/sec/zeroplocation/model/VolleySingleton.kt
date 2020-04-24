package com.sec.zeroplocation.model

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


class VolleySingleton : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    private var mRequestQueue: RequestQueue? = null

    fun init(context: Context) {
        mRequestQueue = Volley.newRequestQueue(context)
        val memClass =
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .memoryClass
    }


    val requestQueue: RequestQueue? = null
        get() {
            if (field == null) {
                return Volley.newRequestQueue(applicationContext)
            }
            return field
        }

    companion object {
        private val TAG = VolleySingleton::class.java.simpleName
        @get:Synchronized var instance: VolleySingleton? = null
            private set
    }
}