package com.example.coroutinetest.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutinetest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoroutineDispatcherActivity : AppCompatActivity(R.layout.activity_dispatcher) {
    private val TAG = javaClass.simpleName
    private val ioDispatcher = Dispatchers.IO
    private val defaultDispatcher = Dispatchers.Default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<Button>(R.id.btSingleDispatcher).setOnClickListener {
            testMemberVarDispatchers()
        }
        findViewById<Button>(R.id.btMultiDispatcher).setOnClickListener {
            testNonMemberVarDispatchers()
        }
    }

    private fun testNonMemberVarDispatchers() {
        repeat(50) {
            val dispatcher = Dispatchers.IO
            CoroutineScope(dispatcher).launch {
                Log.d(TAG, Thread.currentThread().name)
            }
        }
    }

    private fun testMemberVarDispatchers() {
        repeat(50) {
            CoroutineScope(ioDispatcher).launch {
                Log.d(TAG, Thread.currentThread().name)
            }
        }
    }
}