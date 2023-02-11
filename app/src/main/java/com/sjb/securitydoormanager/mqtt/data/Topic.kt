package com.sjb.securitydoormanager.mqtt.data

/**
 * author:10500
 * date: 2023/1/31 9:12
 * desc: Topic
 */
enum class Topic {
    TOPIC_MSG{
        override fun value():String{
            return "securityDoor/sub/SD00000001"
        }
    },
    TOPIC_SEND{
        override fun value():String{
            return "securityDoor/pub/SD00000001"
        }
    };
    abstract fun value(): String
}