package com.example.coroutinetest.ui

import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutinetest.R
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Timer
import kotlin.random.Random

class CoroutineExceptionActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private val workList = mutableListOf<Thread>()
    private val timer = Timer()
    private val scope = Dispatchers.Default
    private val scopeIO = Dispatchers.IO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(scope).launch {
            test()
        }
    }

    private suspend fun test() {
        Looper.getMainLooper()
        Looper.myLooper()

        val handler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG, "CoroutineExceptionHandler got $exception")
        }
        for (index in 0..1000) {
            CoroutineScope(scopeIO).launch() {
                Log.d(TAG, "job launch")
                executeException()
            }
        }
        val deferred = CoroutineScope(scope).async() {
            Log.d(TAG, "job async")
            executeException()
        }
        Log.d(TAG, "test before joinAll")
//        joinAll(job, deferred)
        Log.d(TAG, "test after joinAll")
    }

    private fun executeException() {
        val randomSeed = Random.nextInt(0, 9)
        val msg = "recordException $randomSeed"
        val exception = when (randomSeed) {
            0 -> NullPointerException(msg)
            1 -> ArrayIndexOutOfBoundsException(msg)
            2 -> SecurityException(msg)
            3 -> RuntimeException(msg)
            4 -> IOException(msg)
            5 -> ClassNotFoundException(msg)
            6 -> StackOverflowError(msg)
            8 -> OutOfMemoryError(msg)
            else -> Throwable(msg)
        }
        throw exception
    }
}