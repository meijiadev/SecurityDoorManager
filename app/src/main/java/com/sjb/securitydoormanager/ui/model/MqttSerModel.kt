package com.sjb.securitydoormanager.ui.model

import android.graphics.Bitmap
import android.view.View
import androidx.core.graphics.BitmapCompat
import androidx.lifecycle.viewModelScope
import com.android.util.DateUtil
import com.android.util.DateUtil.YMDHMS
import com.google.gson.Gson
import com.hjq.http.EasyHttp
import com.hjq.http.body.JsonBody
import com.hjq.http.listener.OnUpdateListener
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.SJBApp
import com.sjb.securitydoormanager.http.api.UploadImageApi
import com.sjb.securitydoormanager.idr.IDCardInfo
import com.sjb.securitydoormanager.media.mqtt.*
import com.sjb.securitydoormanager.media.mqtt.data.Qos
import com.sjb.securitydoormanager.media.mqtt.mode.*
import com.sjb.securitydoormanager.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.MultipartBody
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap

/**
 * Mqtt相关业务
 */
class MqttSerModel : BaseViewModel() {


    var mqttHelper: MqttHelper? = null

    // 当前身份证人像头像
    var cardBitmap: Bitmap? = null

    var name: String? = null

    private var captureBitmap: Bitmap? = null

    fun initMqttSer() {
        mqttHelper = MqttHelper(SJBApp.context, host, sn)
        mqttHelper?.connect(Qos.QOS_TWO, true, object : MqttCallback {
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
                    parseData(path, json)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Logger.i("token:${token?.message}")
            }
        })
    }


    /**
     * 解析数据
     */
    fun parseData(path: String, json: String) {
        if (path == MessageEvent.MsgType.DOOR_RECORD_RESPONSE.type) {
            val devResponse = Gson().fromJson(json, DevResponse::class.java)
            val cardUrl = devResponse.data.idCardPhotoUrl
            val cardId = devResponse.data.idCardPhotoId
            val captureUrl = devResponse.data.capturePhotoUrl
            val captureId = devResponse.data.capturePhotoId
            uploadImage(cardBitmap, cardUrl, cardId, name)
            uploadImage(captureBitmap, captureUrl, captureId, name + "_capture")
        }
    }


    /**
     * 上传安检记录+身份证信息
     */
    fun uploadRecord(
        passStatus: Int,
        passMode: String,
        cardInfo: IDCardInfo?,
        capturePic: Bitmap?
    ) {
        captureBitmap = capturePic
        val alarmInfoLs = mutableListOf<AlarmInfo>()
        alarmInfoLs.add(AlarmInfo("2", "1234", "手机"))
        alarmInfoLs.add(AlarmInfo("12", "2356", "手机"))
        val time = DateUtil.getCurrentDateTime()
        val detectInfo = DetectInfo("OK", "", time, alarmInfoLs)
        var idCardIdentifyInfo: IDCardIdentifyInfo? = null
        var faceIdentifyInfo: FaceIdentifyInfo? = null
        var fileInfo: FileInfo? = null
        cardInfo?.let {
            it.cardPic?.let { bitmap ->
                cardBitmap = bitmap
                name = it.name
                fileInfo = FileInfo(
                    it.name + DateUtil.getCurrentDateTime(YMDHMS) + ".png",
                    getBitmapSize(bitmap).toString(),
                    "image/png",
                    false,
                    null
                )
            }
            idCardIdentifyInfo = IDCardIdentifyInfo(
                it.identifyStatus,
                it.name,
                it.sex,
                it.cardId,
                it.expireDate,
                null,
                it.identifyMsg,
                time,
                fileInfo
            )
            faceIdentifyInfo = FaceIdentifyInfo(
                it.identifyStatus,
                it.identifyMsg,
                time,
                null,
                null,
                "0.82",
                fileInfo,
            )
        }
        val record = DevRecord(
            UUID.randomUUID().toString(),
            passMode,
            "Phone",
            passStatus,
            time,
            null,
            idCardIdentifyInfo,
            faceIdentifyInfo,
            detectInfo
        )
        val msg = MessageEvent.MsgType.DOOR_RECORD.type + Gson().toJson(record)
        mqttHelper?.publish(msg)
    }

    /**
     * 获得Bitmap的占用内存的大小
     * getAllocationByteCount() 、 getByteCount()
     * @param bitmap
     * @return Bitmap占用内存大小 Byte
     */
    private fun getBitmapSize(bitmap: Bitmap): Int {
        return BitmapCompat.getAllocationByteCount(bitmap)
    }


    /**
     * 上传图片
     */
    private fun uploadImage(bitmap: Bitmap?, url: String, photoId: String, fileName: String?) {
        if (bitmap == null) {
            Logger.e("没有可上传的抓拍图片")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val file = FileUtil.bitmapToFile(bitmap, fileName)
            withContext(Dispatchers.Main) {
                val mediaType = MediaType.parse("image/png")
                val fileBody = MultipartBody.create(mediaType, file)
                val multiBuilder = MultipartBody.Builder()
                multiBuilder.setType(MultipartBody.FORM)
                multiBuilder.addFormDataPart("file", file.name, fileBody)
                val multiBody = multiBuilder.build()
                EasyHttp.post(this@MqttSerModel)
                    .api(UploadImageApi(url))
                    .body(multiBody)
                    .request(object : OnUpdateListener<Void?> {
                        override fun onStart(call: Call) {
                            Logger.e("-----开始上传图片---------$url")
                        }

                        override fun onSucceed(result: Void?) {
                            Logger.e("图片上传成功")
                            uploadCompleted(photoId)
                        }

                        override fun onProgress(progress: Int) {
                            Logger.i("图片上传进度：$progress")
                        }

                        override fun onFail(e: Exception) {
                            Logger.e("图片上传报错：${e.message}")
                        }

                        override fun onEnd(call: Call) {

                        }
                    })

            }


        }
    }

    /**
     * 图片上传完成通知
     */
    private fun uploadCompleted(fileID: String) {
        val uploadComplete = UploadComplete(fileID)
        val msg = MessageEvent.MsgType.DOOR_UPLOAD_COMPLETE.type + Gson().toJson(uploadComplete)
        mqttHelper?.publish(msg)

    }

    /**
     * 将bitmap 转换成inputStream
     */
    private fun getStream(bitmap: Bitmap?): InputStream {
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return ByteArrayInputStream(baos.toByteArray())
    }

}