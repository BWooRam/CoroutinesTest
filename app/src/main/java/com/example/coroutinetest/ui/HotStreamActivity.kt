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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HotStreamActivity : AppCompatActivity(R.layout.activity_main) {
    private var viewModel: HotStreamViewModel? = null
    private val TAG = javaClass.simpleName
    private val worker: WorkerImp = WorkerImp(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()

        CoroutineScope(Dispatchers.Default).launch {
            viewModel!!.channelItem.consumeEach { user ->
                Log.d(TAG, "초기테스트 user = $user")
            }
        }

        findViewById<Button>(R.id.btLoadInfo).setOnClickListener {
            Log.d(TAG, "initEvent start")
            initEvent()
        }

        findViewById<Button>(R.id.btRandomTest).setOnClickListener {
            Log.d(TAG, "worker start")
            //Worker 초기화
            worker
                .many(1)
                .interval(1000)
                .job {
                    viewModel!!.receiveEventUser()
                }.work()
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            HotStreamViewModel.ViewModelFactory(application)
        )[HotStreamViewModel::class.java]
    }

    private fun initEvent() {
        for (count in 0..10) {
            CoroutineScope(Dispatchers.Default).launch {
                viewModel!!.flowChannelItem.collect { user ->
                    Log.d(TAG, "flowChannelItem$count user = $user")
                }
            }
        }

        /*for (count in 0..10) {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel!!.sharedFlowItem.collect { user ->
                    Log.d(TAG, "sharedFlowItem$count user = $user")
                }
            }
        }*/

        /*CoroutineScope(Dispatchers.Default).launch {
            viewModel!!.sharedFlowItem.collect { user ->
                Log.d(TAG, "sharedFlowItem user = $user")
            }
        }*/
    }

}