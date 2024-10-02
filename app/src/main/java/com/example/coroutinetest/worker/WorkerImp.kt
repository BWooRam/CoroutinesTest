package com.example.coroutinetest.worker

import android.util.Log
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List

interface Worker {
    fun work()
}

class WorkerImp(
    private val dispatcher: CoroutineDispatcher
) : Worker {
    private var mWorkerCount: Int = 0
    private var mWorkerInterval: Long = 0
    private var mWorkerRunnable: Runnable? = null
    private var mWorkerRunnableList: ArrayList<Runnable> = arrayListOf()
    private var isStart = false
    private var timer: Timer? = null

    /**
     *
     */
    fun many(count: Int): WorkerImp {
        this@WorkerImp.mWorkerCount = count
        return this@WorkerImp
    }

    /**
     *
     */
    fun job(runnable: Runnable): WorkerImp {
        this@WorkerImp.mWorkerRunnable = runnable
        return this@WorkerImp
    }

    /**
     *
     */
    fun job(runnableList: List<Runnable>): WorkerImp {
        this@WorkerImp.mWorkerRunnableList.clear()
        this@WorkerImp.mWorkerRunnableList.addAll(runnableList)
        return this@WorkerImp
    }

    /**
     * 설정할 초를 입력해주세요
     */
    fun interval(interval: Long): WorkerImp {
        this@WorkerImp.mWorkerInterval = interval
        return this@WorkerImp
    }

    /**
     *
     */
    override fun work() {
        this@WorkerImp.isStart = true

        //Timer 생성
        if (timer == null)
            timer = Timer()

        //Timer 실행
        timer?.schedule(object : TimerTask() {
            override fun run() {
                //Worker 생성
                for (index in 0 until mWorkerCount) {
                    //Worker 실행
                    CoroutineScope(dispatcher).launch {
                        //worker 시작
                        val secondTime = TimeUtils.getSecondTime().toInt()
                        val currentTime = TimeUtils.getTime()
                        if (secondTime % 10 == 0) {
                            Log.d(
                                "Worker",
                                "work() secondTime = $secondTime, currentTime = $currentTime"
                            )
                            val runnable =
                                if (this@WorkerImp.mWorkerRunnable != null) this@WorkerImp.mWorkerRunnable else mWorkerRunnableList[index]
                            runnable?.run()
                        }
                    }
                }
            }
        }, 0, mWorkerInterval)
    }

    fun stop() {
        Log.d("Worker", "stop()")
        this@WorkerImp.isStart = false
        timer?.cancel()
        timer = null
    }

    fun isStart() = this@WorkerImp.isStart

}