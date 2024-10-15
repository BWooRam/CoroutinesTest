package com.example.coroutinetest.ui.restart

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.coroutinetest.R
import com.example.coroutinetest.data.State
import com.example.coroutinetest.worker.WorkerImp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class RestartStateFlowActivity : AppCompatActivity(R.layout.activity_restart) {
    private val viewModel: RestartStateFlowViewModel by lazy {
        ViewModelProvider(
            this,
            RestartStateFlowViewModel.ViewModelFactory(application)
        )[RestartStateFlowViewModel::class.java]
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
                Log.d(TAG, "collect state = $state")
            }
        }

        findViewById<Button>(R.id.btWorkStart).setOnClickListener {
            testFlowRunningFold()
        }

        findViewById<Button>(R.id.btRestart).setOnClickListener {
            testRestart()
        }
    }

    private fun testRestart() {
        viewModel.state.restart()
    }

    private fun testFlowRunningFold() {
        worker
            .many(1)
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

                CoroutineScope(ioDispatcher).launch {
                    val result = viewModel.sendMutexChannelItem(State(randomIsLoading, data))
                    Log.d(TAG, "testFlowRunningFold createState = $createState, isSuccess = ${result.isSuccess}")
                }
            }.work()
    }
}