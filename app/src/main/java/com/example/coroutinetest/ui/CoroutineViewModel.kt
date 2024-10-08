package com.example.coroutinetest.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date


class CoroutineViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val channelItem: Channel<CoroutineActivity.State> =
        Channel<CoroutineActivity.State>(capacity = Channel.BUFFERED, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val state: StateFlow<CoroutineActivity.State> by lazy {
        val initialState = CoroutineActivity.State()
        channelItem
            .receiveAsFlow()
            .runningFold(initialState) { acc, value ->
                acc.copy(isLoading = value.isLoading, data = value.data)
            }
            .stateIn(
                scope = CoroutineScope(Dispatchers.IO),
                started = SharingStarted.Eagerly,
                initialValue = initialState
            )
    }

    private val syncObject = Any()

    @Synchronized
    fun sendChannelItem(state: CoroutineActivity.State): ChannelResult<Unit> {
        val result = synchronized(syncObject) {
            return@synchronized channelItem.trySend(state)
        }

        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date(System.currentTimeMillis()))
        Log.d("CoroutineViewModel", "sendChannelItem result isSuccess = ${result.isSuccess}, time = $time")
        return result
    }

    /**
     * 해당 ViewModel에 종속성을 주입시키는 방법이다.
     * 이 경우에는 특정 객체나 값을 주입시키기 위한 생성자라고 생각하면 될듯하다.
     */
    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CoroutineViewModel(application) as T
        }
    }
}