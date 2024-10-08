package com.example.coroutinetest.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.coroutinetest.R
import com.example.coroutinetest.worker.WorkerImp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class CoroutineActivity : AppCompatActivity(R.layout.activity_coroutine) {
    private val viewModel: CoroutineViewModel by lazy {
        ViewModelProvider(
            this,
            CoroutineViewModel.ViewModelFactory(application)
        )[CoroutineViewModel::class.java]
    }
    private val TAG = javaClass.simpleName
    private val worker: WorkerImp = WorkerImp(Dispatchers.IO)
    private val ioDispatcher = Dispatchers.IO
    private val defaultDispatcher = Dispatchers.Default
    private val mainDispatcher = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(defaultDispatcher).launch {
            viewModel.state.collect { state ->
                Log.d(TAG, "viewModel state = $state")
            }
        }

        findViewById<Button>(R.id.btLaunch).setOnClickListener {
            testLaunch()
        }
        findViewById<Button>(R.id.btWithContext).setOnClickListener {
            testWithContext()
        }
        findViewById<Button>(R.id.btMultiWithContext).setOnClickListener {
            testMultiWithContext()
        }
        findViewById<Button>(R.id.btMultiAsync).setOnClickListener {
            testMultiAsync()
        }
        findViewById<Button>(R.id.btErrorWithContext).setOnClickListener {
            testErrorWithContext()
        }
        findViewById<Button>(R.id.btErrorAsync).setOnClickListener {
            testErrorAsync()
        }
        findViewById<Button>(R.id.btTestFlowRunningFold).setOnClickListener {
            testFlowRunningFold()
        }
    }

    private fun testLaunch() {
        CoroutineScope(defaultDispatcher).launch {
            Log.d(TAG, "testLaunch Thread Name = ${Thread.currentThread().name}")
        }
    }

    private fun testWithContext() {
        CoroutineScope(mainDispatcher).launch {
            Log.d(TAG, "testWithContext launch outer Thread Name = ${Thread.currentThread().name}")
            val result1 = withContext(ioDispatcher) {
                Log.d(
                    TAG,
                    "testWithContext launch inner Thread Name = ${Thread.currentThread().name}"
                )
                "test1"
            }

            val deferred = CoroutineScope(mainDispatcher).async {
                Log.d(
                    TAG,
                    "testWithContext async outer Thread Name = ${Thread.currentThread().name}"
                )
                withContext(ioDispatcher) {
                    Log.d(
                        TAG,
                        "testWithContext async inner Thread Name = ${Thread.currentThread().name}"
                    )
                }
                return@async "test2"
            }

            val result2 = deferred.await()

            Log.d(TAG, "testWithContext result1 = $result1")
            Log.d(TAG, "testWithContext result2 = $result2")
        }
    }

    private fun testMultiWithContext() {
        CoroutineScope(mainDispatcher).launch {
            val result1 = withContext(ioDispatcher) {
                Log.d(
                    TAG,
                    "testMultiWithContext withContext Thread Name = ${Thread.currentThread().name}"
                )
                "test1"
            }
            val result2 = withContext(ioDispatcher) {
                Log.d(
                    TAG,
                    "testMultiWithContext withContext Thread Name = ${Thread.currentThread().name}"
                )
                "test2"
            }

            val result = result1.plus(result2)
            Log.d(TAG, "testMultiWithContext result = $result")
        }
    }

    private fun testMultiAsync() {
        CoroutineScope(mainDispatcher).launch {
            val deferred1 = CoroutineScope(mainDispatcher).async {
                Log.d(
                    TAG,
                    "testMultiAsync launch inner Thread Name = ${Thread.currentThread().name}"
                )
                "test1"
            }

            val deferred2 = CoroutineScope(mainDispatcher).async {
                Log.d(
                    TAG,
                    "testMultiAsync launch inner Thread Name = ${Thread.currentThread().name}"
                )
                "test2"
            }

            val result1 = deferred1.await()
            val result2 = deferred2.await()
            val result = result1.plus(result2)
            Log.d(TAG, "testMultiAsync result = $result")
        }
    }

    private fun testErrorWithContext() {
        CoroutineScope(mainDispatcher).launch {
            kotlin.runCatching {
                withContext(ioDispatcher) {
                    throw NullPointerException("testErrorWithContext result1 NullPointerException")
                }
            }.onSuccess { result ->
                Log.d(TAG, "testErrorWithContext onSuccess result1 = $result")
            }.onFailure { e ->
                Log.d(TAG, "testErrorWithContext onFailure result1 e = $e")
            }

            kotlin.runCatching {
                withContext(ioDispatcher) {
                    throw NullPointerException("testErrorWithContext result2 NullPointerException")
                }
            }.onSuccess { result ->
                Log.d(TAG, "testErrorWithContext onSuccess result2 = $result")
            }.onFailure { e ->
                Log.d(TAG, "testErrorWithContext onFailure result2 e = $e")
            }
        }
    }

    private fun testErrorAsync() {
        CoroutineScope(mainDispatcher).launch {
            runCatching {
                val deferred = CoroutineScope(mainDispatcher).async {
                    throw NullPointerException("testErrorAsync result1 NullPointerException")
                }
                deferred.await()
            }.onSuccess { result ->
                Log.d(TAG, "testErrorAsync onSuccess result1 = $result")
            }.onFailure { e ->
                Log.d(TAG, "testErrorAsync onFailure result1 e = $e")
            }

            runCatching {
                val deferred = CoroutineScope(mainDispatcher).async {
                    throw NullPointerException("testErrorAsync result2 NullPointerException")
                }
                deferred.await()
            }.onSuccess { result ->
                Log.d(TAG, "testErrorAsync onSuccess result2 = $result")
            }.onFailure { e ->
                Log.d(TAG, "testErrorAsync onFailure result2 e = $e")
            }
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val data: List<String> = arrayListOf()
    )

    private fun testFlowRunningFold() {
        /*CoroutineScope(defaultDispatcher).launch {
            val randomIsLoading = Random.nextBoolean()
            val randomTestIndex = Random.nextInt(0, 10)
            val data = mutableListOf<String>()

            if (randomTestIndex > 1) {
                for (index in 0..randomTestIndex) {
                    data.add("Test$index")
                }
            }
            viewModel.channelItem.trySend(State(randomIsLoading, data))
        }*/

        worker
            .many(10)
            .interval(1000)
            .job {
                val randomIsLoading = Random.nextBoolean()
                val randomTestIndex = Random.nextInt(0, 100)
                val data = mutableListOf<String>()

                if (randomTestIndex > 1) {
                    for (index in 0..randomTestIndex) {
                        data.add("Test$index")
                    }
                }
                val createState = State(randomIsLoading, data)
                val result = viewModel.sendChannelItem(State(randomIsLoading, data))
                Log.d(TAG, "testErrorAsync createState = $createState, isSuccess = ${result.isSuccess}")
            }.work()
    }

}