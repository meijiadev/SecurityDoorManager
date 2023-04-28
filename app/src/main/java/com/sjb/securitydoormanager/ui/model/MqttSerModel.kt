package com.sjb.securitydoormanager.ui.model

import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.SJBApp
import com.sjb.securitydoormanager.media.mqtt.MqttHelper
import com.sjb.securitydoormanager.media.mqtt.TOPIC_MSG
import com.sjb.securitydoormanager.media.mqtt.data.Qos
import com.sjb.securitydoormanager.media.mqtt.host
import com.sjb.securitydoormanager.media.mqtt.sn
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * Mqtt相关业务
 */
class MqttSerModel : BaseViewModel() {

    var mqttHelper: MqttHelper? = null

    fun initMqttSer() {
        mqttHelper = MqttHelper(SJBApp.context, host, sn)
        mqttHelper?.connect(TOPIC_MSG, Qos.QOS_TWO, true, object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Logger.e("错误" + cause?.message)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                // 收到的消息
                val msg = message?.payload?.let { String(it) }
                msg?.let {
                    val index = msg.indexOf("{")
                    if (index < 1) {
                        Logger.i("Mqtt收到未知消息")
                        return
                    }
                    // 前面的路由
                    val path = msg.substring(0, index).trim()
                    // 具体的json消息体
                    val json = msg.substring(index)
                    Logger.i("path:$path,json:$json")
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Logger.i("token:${token?.message}")
            }
        })
    }
}