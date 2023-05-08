package com.sjb.securitydoormanager.media.mqtt.mode

import java.util.UUID

/**
 * author:10500
 * date: 2023/2/6 15:33
 * desc: MessageEvent
 */
object MessageEvent {

    enum class MsgType(val type: String) {
        DOOR_CONNECT("/securityDoor/connect"),
        DOOR_SCENARIO("/securityDoor/getScenario"),
        // 安检门获取配置
        DOOR_UPLOAD_CONFIG("/securityDoor/uploadConfig"),
        // 安检门记录上报
        DOOR_RECORD("/securityDoor/record"),
        // 安检门记录上报后得返回值
        DOOR_RECORD_RESPONSE("/securityDoor/record/response"),
        // 更新配置信息
        DOOR_UPDATE_CONFIG("/securityDoor/updatedConfig"),
        // 上传文件-确认完成
        DOOR_FILE_COMPLETED("/file/completed"),

        // 获取人脸识别列表请求
        DOOR_FACE_LIST("/faceList"),
        // 人脸识别列表响应
        DOOR_FACE_LIST_RESPONSE("/faceList/response"),
        // 平台新增人脸
        DOOR_ADD_FACE("/addFace"),
        // 平台编辑人脸
        DOOR_UPDATE_FACE("/updateFace"),
        // 平台删除人脸
        DOOR_REMOVE_FACE("/removeFace"),
        // 图片上传成功的提醒
        DOOR_UPLOAD_COMPLETE("/file/completed")

    }


    fun getUUID(): String {
        val uuid = UUID.randomUUID()
        return uuid.toString()
    }


}