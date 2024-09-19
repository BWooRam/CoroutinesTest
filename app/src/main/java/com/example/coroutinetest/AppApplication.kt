package com.example.coroutinetest

import android.app.Application
import android.util.Log

class AppApplication : Application() {
    private val TAG = javaClass.simpleName

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thead, error ->
            Log.d(TAG, "Thread setDefaultUncaughtExceptionHandler thead = ${thead.name}, error = $error")
        }
    }
}