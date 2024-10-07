package com.example.coroutinetest.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutinetest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoroutineActivity : AppCompatActivity(R.layout.activity_coroutine) {
    private val TAG = javaClass.simpleName
    private val ioDispatcher = Dispatchers.IO
    private val defaultDispatcher = Dispatchers.Default
    private val mainDispatcher = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.btLaunch).setOnClickListener {
            testLaunch()
        }
        findViewById<Button>(R.id.btWithContext).setOnClickListener {
            testWithContext()
        }
    }

    private fun testLaunch() {
        CoroutineScope(defaultDispatcher).launch {
            Log.d(TAG, "testLaunch Thread Name = ${Thread.currentThread().name}")
        }
    }

    private fun testWithContext() {
        CoroutineScope(mainDispatcher).launch {
            Log.d(TAG, "testWithContext outer Thread Name = ${Thread.currentThread().name}")
            withContext(ioDispatcher){
                Log.d(TAG, "testWithContext inner Thread Name = ${Thread.currentThread().name}")
            }
        }
    }
}