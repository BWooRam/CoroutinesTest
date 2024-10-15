package com.example.coroutinetest.ui.restart

import RestartableStateFlow
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coroutinetest.data.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import restartableStateIn
import java.text.SimpleDateFormat
import java.util.Date


class RestartStateFlowViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val TAG = javaClass.simpleName
    val channelItem: Channel<State> = Channel(
        capacity = Channel.UNLIMITED,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        onUndeliveredElement = { state ->
            Log.d(TAG, "onUndeliveredElement state = $state")
        })
    val state: RestartableStateFlow<State> by lazy {
        val initialState = State()
        channelItem
            .receiveAsFlow()
            .runningFold(initialState) { acc, value ->
                acc.copy(isLoading = value.isLoading, data = value.data)
            }.restartableStateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = initialState
        )
    }

    private val syncMutex = Mutex()
    suspend fun sendMutexChannelItem(state: State): ChannelResult<Unit> {
        val mutexResult = syncMutex.withLock {
            val result = channelItem.trySend(state)

            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date(System.currentTimeMillis()))
            Log.d(TAG, "sendMutexChannelItem result isSuccess = ${result.isSuccess}, time = $time")
            result
        }
        return mutexResult
    }

    /**
     * 해당 ViewModel에 종속성을 주입시키는 방법이다.
     * 이 경우에는 특정 객체나 값을 주입시키기 위한 생성자라고 생각하면 될듯하다.
     */
    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RestartStateFlowViewModel(application) as T
        }
    }
}