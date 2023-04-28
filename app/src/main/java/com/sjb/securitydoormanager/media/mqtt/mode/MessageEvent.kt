package com.sjb.securitydoormanager.media.mqtt.mode

import java.util.UUID

/**
 * author:10500
 * date: 2023/2/6 15:33
 * desc: MessageEvent
 */
object MessageEvent {

    enum class MsgType(val type:String){
        DOOR_CONNECT("/securityDoor/connect"),
        DOOR_SCENARIO("/securityDoor/getScenario"),
        DOOR_GET_CONFIG("/securityDoor/getConfig"),
        DOOR_RECORD("/securityDoor/record"),
        DOOR_UPDATE_CONFIG("/securityDoor/updatedConfig")
    }


    fun getUUID(): String {
        val uuid = UUID.randomUUID()
        return uuid.toString()
    }


}