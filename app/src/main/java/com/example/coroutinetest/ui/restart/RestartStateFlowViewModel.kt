package com.example.coroutinetest.ui.restart

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import restartableStateIn


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
    val state: StateFlow<State> by lazy {
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