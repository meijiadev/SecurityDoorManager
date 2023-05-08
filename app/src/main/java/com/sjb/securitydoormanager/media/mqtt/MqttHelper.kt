package com.sjb.securitydoormanager.media.mqtt

import android.content.Context
import android.text.TextUtils
import com.android.util.DateUtil
import com.blankj.utilcode.util.DeviceUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.itfitness.mqttlibrary.MqttAndroidClient
import com.orhanobut.logger.Logger
import com.sjb.securitydoormanager.media.mqtt.data.Qos
import com.sjb.securitydoormanager.media.mqtt.mode.*
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.UUID

/**
 * author:10500
 * date: 2023/1/30 15:33
 * desc: MqttHelper mqtt业务相关工具类
 */
const val host = "tcp://192.168.2.131:1883"

/**
 *用户发布主题  + 【设备SN码】
 */
const val DOOR_PUB = "securityDoor/pub/"

/**
 * 用户订阅主题 +【设备SN码】
 */
const val DOOR_SUB = "securityDoor/sub/"

/**
 * +设备SN码(人脸识别订阅主题) or +组织ID  (用户订阅广播主题)
 */
const val FACE_SUB = "faceIdentify/sub/"

/**
 * 人脸识别 发布主题+ 【设备SN码】
 */
const val FACE_PUB = "faceIdentify/pub/"

const val sn = "SD00000004"

class MqttHelper {

    private val mqttClient: MqttAndroidClient

    private val connectOptions: MqttConnectOptions

    private var mqttActionListener: IMqttActionListener? = null

    constructor(context: Context, serverUrl: String, name: String) {
        val macAddress = DeviceUtils.getAndroidID()
        val clientId = if (!TextUtils.isEmpty(macAddress)) {
            macAddress
        } else {
            UUID.randomUUID().toString()
        }
        mqttClient = MqttAndroidClient(context, serverUrl, clientId)
        connectOptions = MqttConnectOptions().apply {
            isCleanSession = false
            connectionTimeout = 30
            keepAliveInterval = 10
            isAutomaticReconnect = true
            userName = name
            password = DES3Util.createPass(sn).toCharArray()
        }

        val jsonObject = JsonObject()
        jsonObject.addProperty("status", "PowerOff")
        val json = Gson().toJson(jsonObject)
        val msg = "${MessageEvent.MsgType.DOOR_CONNECT.type}$json"
        connectOptions.setWill(DOOR_SUB + sn, msg.toByteArray(), Qos.QOS_TWO.value(), false)
    }

    fun connect(qos: Qos, isFailRetry: Boolean, mqttCallback: MqttCallback) {
        Logger.i("正在链接...")
        mqttClient.setCallback(mqttCallback)
        if (mqttActionListener == null) {
            mqttActionListener = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Logger.i("Mqtt 连接成功")
                    devSubscribe(DOOR_SUB + sn, qos)
                    faceSubscribe(FACE_SUB + sn, qos)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Logger.i("Mqtt 连接失败,asyncAction:${asyncActionToken},${exception?.message}")
//                    if (isFailRetry) {
//                        mqttClient.connect(connectOptions, null, mqttActionListener)
//                    }
                }
            }
        }
        mqttClient.connect(connectOptions, null, mqttActionListener)
    }

    /**
     * 订阅设备主题
     */
    private fun devSubscribe(topic: String, qos: Qos) {
        mqttClient.subscribe(topic, qos.value(), null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Logger.i("Dev subscribe success：${asyncActionToken.toString()}")
                val devStatus =DeviceStatus(
                    MessageEvent.getUUID(),
                    "Running",
                    DateUtil.getCurrentDateTime(),
                    "",
                    "v1.0.0"
                )
                val msg = MessageEvent.MsgType.DOOR_CONNECT.type + Gson().toJson(devStatus)
                publish(msg)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Logger.i("subscribe failure：${asyncActionToken.toString()}")
            }
        })
    }

    /**
     * 订阅人脸识别主题
     */
    private fun faceSubscribe(topic: String, qos: Qos) {
        mqttClient.subscribe(topic, qos.value(), null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Logger.i("Face subscribe success:${asyncActionToken.toString()}")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Logger.e("Face subscribe failure：${asyncActionToken.toString()}")
            }
        })
    }

    /**
     * 发送消息
     */
     fun publish(message: String) {
        val msg = MqttMessage()
        msg.isRetained = false
        msg.payload = message.toByteArray()
        msg.qos = Qos.QOS_TWO.value()
        Logger.i("发布出去的消息：$msg")
        mqttClient.publish(DOOR_PUB + sn, msg)

    }

    /**
     * 获取应用场景 /securityDoor/getScenario{"tx":"950637ad-1349-402b-a69e-2473c79e6c31"}
     */
    fun getScenario(): String {
        return "${MessageEvent.MsgType.DOOR_SCENARIO.type}{\"tx\":\"${UUID.randomUUID()}\"}"
    }



    /**
     * 断开连接
     */
    fun disconnect() {
        mqttClient.disconnect()
    }
}