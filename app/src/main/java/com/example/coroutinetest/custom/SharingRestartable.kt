import android.util.Log
import kotlinx.coroutines.flow.*

interface SharingRestartable : SharingStarted {
    fun restart()
    fun getRestartFlow(): SharedFlow<SharingCommand>
    fun getMargeFlow(): Flow<SharingCommand>
    fun getSharingStartedFlow(): Flow<SharingCommand>
    fun getSubscriptionCountFlow(): Flow<Int>
}

fun SharingStarted.makeRestartable(): SharingRestartable {
    return SharingRestartableImpl(this)
}

/**
 * https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-sharing-started/
 *
 * @property sharingStarted
 */
private data class SharingRestartableImpl(
    private val sharingStarted: SharingStarted,
) : SharingRestartable {
    private val TAG = javaClass.simpleName
    private val restartFlow = MutableSharedFlow<SharingCommand>(extraBufferCapacity = 2)
    private var margeFlow: Flow<SharingCommand>? = null
    private var sharingStartedFlow: Flow<SharingCommand>? = null
    private var subscriptionCountFlow: Flow<Int>? = null

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> {
        Log.d(TAG, "command Start")
        subscriptionCountFlow = subscriptionCount
        sharingStartedFlow = sharingStarted.command(subscriptionCount)
        margeFlow = merge(restartFlow, sharingStarted.command(subscriptionCount))
        return margeFlow!!
    }

    override fun restart() {
        restartFlow.tryEmit(SharingCommand.STOP_AND_RESET_REPLAY_CACHE)
        restartFlow.tryEmit(SharingCommand.START)
    }

    override fun getRestartFlow(): SharedFlow<SharingCommand> {
        return restartFlow
    }

    override fun getMargeFlow(): Flow<SharingCommand> {
        return margeFlow!!
    }

    override fun getSharingStartedFlow(): Flow<SharingCommand> {
        return sharingStartedFlow!!
    }

    override fun getSubscriptionCountFlow(): Flow<Int> {
        return subscriptionCountFlow!!
    }
}