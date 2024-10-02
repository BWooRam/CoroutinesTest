package com.example.coroutinetest.worker

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun getTime(): String {
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("HH:mm:ss")
        val resultdate = Date(currentTime)
        return sdf.format(resultdate)
    }

    fun getSecondTime(): String {
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("ss")
        val resultdate = Date(currentTime)
        return sdf.format(resultdate)
    }
}