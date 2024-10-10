package com.example.coroutinetest.data

data class TopicMessage(
    var topic: String,
    var message: String,
    var status: DeviceStatus,
){
    override fun equals(other: Any?): Boolean {
        return if (other is TopicMessage)
            topic == other.topic
        else
            false
    }
}

/**
 *
 */
enum class DeviceStatus {
    Disconnect,
    Connect,
    Lost,
}