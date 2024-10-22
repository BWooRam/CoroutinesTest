package com.example.coroutinetest.ui.common

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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.zip
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
        CoroutineScope(ioDispatcher).launch {
            viewModel.channelItem.consumeEach { state ->
                Log.d(TAG, "channelItem viewModel state = $state")
            }
        }

        CoroutineScope(defaultDispatcher).launch {
            viewModel.channelItem.receiveAsFlow().collect { state ->
                Log.d(TAG, "channelItem receiveAsFlow viewModel state = $state")
            }
        }

        CoroutineScope(defaultDispatcher).launch {
            viewModel.state.collect { state ->
                Log.d(TAG, "collect1 viewModel state = $state")
            }
        }

/*        CoroutineScope(defaultDispatcher).launch {
            viewModel.state.collect { state ->
                Log.d(TAG, "collect2 viewModel state = $state")
            }
        }

        CoroutineScope(defaultDispatcher).launch {
            viewModel.state.collect { state ->
                Log.d(TAG, "collect3 viewModel state = $state")
            }
        }

        CoroutineScope(defaultDispatcher).launch {
            viewModel.state.collect { state ->
                Log.d(TAG, "collect4 viewModel state = $state")
            }
        }*/

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
        findViewById<Button>(R.id.btSupervisorJobError).setOnClickListener {
            testSupervisorJobError()
        }
        findViewById<Button>(R.id.btTestFlowRunningFold).setOnClickListener {
            testFlowRunningFold()
        }
        findViewById<Button>(R.id.btTestFlowMerge).setOnClickListener {
            testFlowMerge()
        }
        findViewById<Button>(R.id.btTestFlowZip).setOnClickListener {
            testFlowZip()
        }
        findViewById<Button>(R.id.btTestFlowCombine).setOnClickListener {
            testFlowCombine()
        }
        findViewById<Button>(R.id.btTestFlowTransformLatest).setOnClickListener {
            testFlowTransformLatest()
        }
        findViewById<Button>(R.id.btTestFlowScan).setOnClickListener {
            testFlowScan()
        }
        findViewById<Button>(R.id.btTestFlowBuffer).setOnClickListener {
            testFlowBuffer()
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

    /**
     * 에러 전파가 부모까지 되지않고 그아래로만 전파해서
     * 자식코루틴만을 취소시킵니다.
     *
     */
    private fun testSupervisorJobError() {
        val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        supervisorScope.launch {
            supervisorScope.launch {
                Log.d(TAG, "job1 success = {\"job1 Complete\"}")
            }
            supervisorScope.launch {
                throw Exception()
            }
            supervisorScope.launch {
                Log.d(TAG, "job3 success = {\"job3 Complete\"}")
            }
            supervisorScope.launch {
                Log.d(TAG, "job4 success = {\"job4 Complete\"}")
            }
        }
    }

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
                //방법 1
                /*val result = viewModel.sendChannelItem(State(randomIsLoading, data))
                Log.d(TAG, "testFlowRunningFold createState = $createState, isSuccess = ${result.isSuccess}")*/

                //방법 2
                /*CoroutineScope(ioDispatcher).launch {
                    val result = viewModel.sendMutexChannelItem(State(randomIsLoading, data))
                    Log.d(TAG, "testFlowRunningFold createState = $createState, isSuccess = ${result.isSuccess}")
                }*/

                //방법 3
                val result = viewModel.channelItem.trySend(State(randomIsLoading, data))
                Log.d(
                    TAG,
                    "testFlowRunningFold createState = $createState, isSuccess = ${result.isSuccess}"
                )
            }.work()
    }

    /**
     * RunningFold와 같은 기능(이름만 다름)
     * https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/scan.html
     */
    private fun testFlowScan() {
        CoroutineScope(defaultDispatcher).launch {
            flowOf(1, 2, 3)
                .scan(emptyList<Int>()) { acc, value ->
                    Log.d(TAG, "testFlowScan acc = $acc, value = $value")
                    acc + value
                }.toList()
        }
    }

    private val flow1 = flowOf<Char>('a', 'b', 'c', 'd', 'e').onEach { delay(400) }
    private val flow2 = flowOf<Int>(1, 2, 3, 4).onEach { delay(1000) }

    private fun testFlowMerge() {
        CoroutineScope(defaultDispatcher).launch {
            val flowMerge = merge(flow1, flow2)
            Log.d(TAG, "testFlowMerge value = ${flowMerge.toList()}")
            flowMerge.collect { value ->
                Log.d(TAG, "testFlowMerge value = $value")
            }
        }
    }

    private fun testFlowZip() {
        CoroutineScope(defaultDispatcher).launch {
            flow1.zip(flow2) { t1, t2 ->
                Log.d(TAG, "testFlowZip t1 = $t1, t2 = $t2")
                return@zip (t1 + t2).code
            }.collect { value ->
                Log.d(TAG, "testFlowZip value = $value")
            }
        }
    }

    private fun testFlowCombine() {
        CoroutineScope(defaultDispatcher).launch {
            combine(flow2, flow1) { t1, t2 ->
                Log.d(TAG, "testFlowCombine t1 = $t1, t2 = $t2")
                return@combine t1 + t2.code
            }.collect { value ->
                Log.d(TAG, "testFlowCombine value = $value")
            }
        }
    }

    /**
     * Flow에서 값이 방출할때마다 이벤트 발생(원래 흐름이 새 값을 방출하면 transform 블록이 취소됨)
     *
     */
    private fun testFlowTransformLatest() {
        CoroutineScope(defaultDispatcher).launch {
            flow<String> {
                emit("a")
                delay(100)
                emit("b")
            }.transformLatest { value ->
                Log.d(TAG, "testFlowTransformLatest transformLatest value = $value")
                emit(value)
                delay(200)
                emit(value + "_last")
            }.catch { error ->
                Log.d(TAG, "testFlowTransformLatest collect error = $error")
            }.collect { value ->
                Log.d(TAG, "testFlowTransformLatest collect value = $value")
            }
        }
    }

    private fun testFlowBuffer() {
        CoroutineScope(defaultDispatcher).launch {
            flowOf("A", "B", "C")
                .onEach  {
                    Log.d(TAG, "testFlowBuffer onEach 1$it")
                }
                .collect {
                    Log.d(TAG, "testFlowBuffer onEach 2$it")
                }

            flowOf("A", "B", "C")
                .onEach  {
                    Log.d(TAG, "testFlowBuffer buffer onEach 1$it")
                }
                .buffer(capacity = 2, BufferOverflow.DROP_LATEST)
                .collect {
                    Log.d(TAG, "testFlowBuffer buffer onEach 2$it")
                }
        }
    }
}