package com.example.coroutinetest.data

data class State(
    val isLoading: Boolean = false,
    val data: List<String> = arrayListOf(),
    val error:Throwable? = null,
)