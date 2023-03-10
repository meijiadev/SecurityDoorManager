package com.sjb.securitydoormanager.mqtt

import android.content.Context
import android.text.TextUtils
import com.android.util.DateUtil
import com.blankj.utilcode.util.DeviceUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.itfitness.mqttlibrary.MqttAndroidClient
import com.orhanobut.logger.Logger
import com.sjb.securitydoormanager.mqtt.data.Qos
import com.sjb.securitydoormanager.mqtt.data.Topic
import com.sjb.securitydoormanager.mqtt.mode.DeviceStatus
import com.sjb.securitydoormanager.mqtt.mode.MessageEvent
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
const val host = "tcp://192.168.3.5:1883"

class MqttHelper {

    private val mqttClient: MqttAndroidClient

    private val connectOptions: MqttConnectOptions

    private var mqttActionListener: IMqttActionListener? = null

    // 发布的主题
     val topicSend = "securityDoor/pub/SD00000001"
    // 订阅的主题
     val topicRec = "securityDoor/sub/SD00000001"





    constructor(context: Context, serverUrl: String, name: String, pass: String) {
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
            userName = name
            password = pass.toCharArray()
        }

        val jsonObject = JsonObject()
        jsonObject.addProperty("status", 0)
        val json = Gson().toJson(jsonObject)
        val msg = "${MessageEvent.MsgType.DOOR_CONNECT.type}$json"
        connectOptions.setWill(topicSend, msg.toByteArray(), Qos.QOS_TWO.value(), false)
    }

    fun connect(topic: Topic, qos: Qos, isFailRetry: Boolean, mqttCallback: MqttCallback) {
        Logger.i("正在链接...")
        mqttClient.setCallback(mqttCallback)
        if (mqttActionListener == null) {
            mqttActionListener = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Logger.i("Mqtt 连接成功")
                    subscribe(topic, qos)

                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Logger.i("Mqtt 连接失败,asyncAction:${asyncActionToken},${exception?.message}")
                    if (isFailRetry) {
                        mqttClient.connect(connectOptions, null, mqttActionListener)
                    }
                }
            }
        }
        mqttClient.connect(connectOptions, null, mqttActionListener)

    }

    /**
     * 订阅
     */
    private fun subscribe(topic: Topic, qos: Qos) {
        mqttClient.subscribe(topic.value(),qos.value(),null,object :IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Logger.i("subscribe success：${asyncActionToken.toString()}")
                val devStatus=DeviceStatus(MessageEvent.getUUID(),1,DateUtil.getCurrentDateTime(),"","v1.0.0")
                val msg=MessageEvent.MsgType.DOOR_CONNECT.type+Gson().toJson(devStatus)
                publish(qos,msg)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Logger.i("subscribe failure：${asyncActionToken.toString()}")
            }
        })
    }

    /**
     * 发布消息
     */
    fun publish( qos: Qos, message: String) {
        val msg = MqttMessage()
        msg.isRetained = false
        msg.payload = message.toByteArray()
        msg.qos = qos.value()
        Logger.i("发布出去的消息msg:$msg")
        mqttClient.publish(Topic.TOPIC_SEND.value(), msg)
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        mqttClient.disconnect()
    }
}