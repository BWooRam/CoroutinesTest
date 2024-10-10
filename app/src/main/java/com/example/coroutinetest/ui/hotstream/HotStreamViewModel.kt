package com.example.coroutinetest.ui.hotstream

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.coroutinetest.data.User
import com.example.coroutinetest.data.UserDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.random.Random


class HotStreamViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val _sharedFlowItem: MutableSharedFlow<User> = MutableSharedFlow<User>(
        replay = 0,
        extraBufferCapacity = 20,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sharedFlowItem: SharedFlow<User> = _sharedFlowItem.asSharedFlow()
    val channelItem: Channel<User> = Channel<User>(capacity = Channel.CONFLATED)
    val flowChannelItem: Flow<User> = channelItem.receiveAsFlow()

    private var currentJob: Job? = null
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    /**
     * TODO
     *
     */
    fun receiveEventUser() {
        val user = createRandomUser()
        channelItem.trySend(user)
        val isSuccess = _sharedFlowItem.tryEmit(user)
        Log.d("HotStreamViewModel", "isSuccess = $isSuccess")
    }

    private fun createRandomUser(): User {
        val randomId = Random.nextInt(0, 10000)
        return User(
            id = randomId,
            name = "name$randomId",
            email = "email$randomId",
            userName = "userName$randomId",
            userDetail = UserDetail(
                id = randomId,
                title = null,
                content = null,
                tags = arrayListOf(),
                level = null,
                room = null,
                endTime = null,
                startTime = null
            )
        )
    }

    /**
     * 해당 ViewModel에 종속성을 주입시키는 방법이다.
     * 이 경우에는 특정 객체나 값을 주입시키기 위한 생성자라고 생각하면 될듯하다.
     */
    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HotStreamViewModel(application) as T
        }
    }
}