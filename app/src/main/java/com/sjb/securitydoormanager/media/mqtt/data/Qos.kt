package com.sjb.securitydoormanager.media.mqtt.data

/**
 * author:10500
 * date: 2023/1/31 9:11
 * desc: Qos
 */
enum class Qos {
    QOS_ZERO {
        override fun value(): Int {
            return 0
        }
    },
    QOS_ONE {
        override fun value(): Int {
            return 1
        }
    },
    QOS_TWO {
        override fun value(): Int {
            return 2
        }
    };

    abstract fun value(): Int
}