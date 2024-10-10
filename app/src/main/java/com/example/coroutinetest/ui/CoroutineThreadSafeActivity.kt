package com.example.coroutinetest.ui

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutinetest.R
import com.example.coroutinetest.worker.WorkerImp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CoroutineThreadSafeActivity : AppCompatActivity(R.layout.activity_thread_safe) {
    private val TAG = javaClass.simpleName
    private val ioDispatcher = Dispatchers.IO
    private val defaultDispatcher = Dispatchers.Default
    private val synchronizedWorker: WorkerImp = WorkerImp(ioDispatcher)
    private val mutexWorker: WorkerImp = WorkerImp(ioDispatcher)
    private val count = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Worker 초기화
        synchronizedWorker
            .many(count)
            .interval(1000)
            .job(getSynchronizedTestRunnableList())

        mutexWorker
            .many(count)
            .interval(1000)
            .job(getMutexTestRunnableList())

        initEvent()
    }

    private fun initEvent() {
        findViewById<Button>(R.id.btSynchronizedWorkerWorkStart).setOnClickListener {
            if (synchronizedWorker.isStart())
                return@setOnClickListener

            synchronizedWorker.work()
        }

        findViewById<Button>(R.id.btSynchronizedWorkerWorkStop).setOnClickListener {
            synchronizedWorker.stop()
        }

        findViewById<Button>(R.id.btMutexWorkerWorkStart).setOnClickListener {
            if (mutexWorker.isStart())
                return@setOnClickListener

            mutexWorker.work()
        }

        findViewById<Button>(R.id.btMutexWorkerWorkStop).setOnClickListener {
            mutexWorker.stop()
        }
    }

    override fun onStart() {
        super.onStart()
        // 화면 매개 변수 가져 오기
        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)
        Log.d(
            "MainActivity",
            "metric width = ${metric.widthPixels}, height = ${metric.heightPixels}"
        )
    }

    private val loggerMutex = Mutex()

    /**
     * *------ Mutex 사용한 예제 ------*
     * 참고 자료 :
     * 1. https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.sync/-mutex/
     * 2. https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html#mutual-exclusion
     *
     * Mutual exclusion for coroutines.
     *
     * Mutex has two states: locked and unlocked. It is non-reentrant, that is invoking lock even from the same thread/coroutine that currently holds the lock still suspends the invoker.
     *
     * JVM API note: Memory semantic of the Mutex is similar to synchronized block on JVM: An unlock operation on a Mutex happens-before every subsequent successful lock on that Mutex. Unsuccessful call to tryLock do not have any memory effects.
     *
     * @return
     */
    private fun getMutexTestRunnableList(): List<Runnable> {
        val runnableList = arrayListOf<Runnable>()
        for (count in 0 until count) {
            runnableList.add {
                val tag = count.toString()
                CoroutineScope(ioDispatcher).launch {
                    Log.d(TAG, "getMutexTestRunnableList Tag = $tag, Work Start")
                    val loggerMutexResult = loggerMutex.withLock {
                        Log.d(TAG, "getMutexTestRunnableList Tag = $tag, 1Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                        Log.d(TAG, "getMutexTestRunnableList Tag = $tag, 2Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                        Log.d(TAG, "getMutexTestRunnableList Tag = $tag, 3Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                        Log.d(TAG, "getMutexTestRunnableList Tag = $tag, 4Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                        Log.d(TAG, "getMutexTestRunnableList Tag = $tag, 5Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                    }
                    Log.d(TAG, "Tag = $tag, loggerMutexResult = $loggerMutexResult")
                }
            }
        }
        return runnableList
    }

    private var synchronizedObject = Any()

    /**
     * *------ synchronized 사용한 예제 ------*
     * 참고 자료 : https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-synchronized/
     *
     *
     * @return
     *
     */
    private fun getSynchronizedTestRunnableList(): List<Runnable> {
        val runnableList = arrayListOf<Runnable>()
        for (count in 0 until count) {
            runnableList.add {
                val tag = count.toString()
                CoroutineScope(ioDispatcher).launch {
                    Log.d(TAG, "getSynchronizedTestRunnableList Tag = $tag, Work Start")
                    synchronized(synchronizedObject) {
                        Log.d(TAG, "getSynchronizedTestRunnableList Tag = $tag, 1Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                        Log.d(TAG, "getSynchronizedTestRunnableList Tag = $tag, 2Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                        Log.d(TAG, "getSynchronizedTestRunnableList Tag = $tag, 3Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                        Log.d(TAG, "getSynchronizedTestRunnableList Tag = $tag, 4Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                        Log.d(TAG, "getSynchronizedTestRunnableList Tag = $tag, 5Line $tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag$tag\n")
                    }
                    Log.d(TAG, "getSynchronizedTestRunnableList Tag = $tag, Work End")
                }
            }
        }
        return runnableList
    }
}