package com.sjb.securitydoormanager.media.mqtt.mode


/**
 * author:10500
 * date: 2023/2/6 17:12
 * desc: Dev
 */


// 设备上下线通知
data class DeviceStatus(
    val tx: String,
    val status: String,            // 标记 online/offline   1/0
    val connectTime: String,    // 2023-02-02 15:09:32
    val model: String,          //
    val version: String
)

/**
 * 安检门获取应用场景
 */
data class Scenario(
    val scenario: String,
    //val config: Config
)

/**
 * 人脸识别列表请求
 */
data class FaceList(
    val tx: String,
    val pageNo: Int,     //查询得页数
    val pageSize: Int,       //页数大小
    val lastTime: String,     //上次更新得时间  "2023-04-25 00:00:00"
    val deviceType: String    // 设备类型 door
)

/**
 * 人脸识别列表响应
 */
data class FacesResponse(
    val tx: String,                             //
    val status: Int,
    val msg: String,                            // 请求成功
    val data: FaceData
)

data class FaceData(
    val total: Int,                   //总条数
    val pageNo: Int,                     // 当前查询得页数
    val pageSize: Int,               //查询得页数大小
    val faces: List<Faces>            // 人脸列表
)

data class Faces(
    val faceId: String,                                // 人脸ID
    val faceType: String,                             // 人脸类型
    val name: String,                                // 人脸名称
    val facePhoto: String,                          // 人脸照片ID
    val facePhotoUrl: String,                       // 人脸下载得地址
    val deleted: Boolean                           // 是否删除
)

/**
 * 平台新增人脸，通知
 */
data class AddFace(
    val faceId: String,
    val faceType: String,
    val name: String,
    val facePhoto: String,
    val facePhotoUrl: String,
    val path: String
)

/**
 * 编辑人脸，通知
 */
data class UpdateFace(
    val faceId: String,
    val faceType: String,
    val name: String,
    val facePhoto: String,
    val facePhotoUrl: String,
    val path: String
)

/**
 * 删除人脸
 */
data class RemoveFace(
    val faceId: String,
    val path: String
)


// 设备配置安检门本地编辑配置信息 or 平台编辑安检门配置
data class Config(
    val tx: String,
    val sensitives: List<Sensitive>,
    val frequency: Int,
    val scenario: String,        // 应用场景
    val supportModules: List<String>,
    val enableFaceIdentify: Boolean,
    val enableIdCardIdentify: Boolean,
    val enablePassMode: Boolean,
    val passModeConfig: PassModeConfig,
)

data class PassModeConfig(
    val end: String,
    val start: String
)

data class Sensitive(
    val zoneBit: Int,
    val value: Int
)

// 安检记录上报
data class DevRecord(
    val tx: String,
    val passMode: String,                   // 通过方式 IN/OUT
    val scenario: String,             // 应用场景
    val passStatus: Int,                 // 通过状态  Ok / Alarm
    val passTime: String,                   // 通过时间
    val beCheckUserId: String?,               // 受检人ID
    val idCardIdentifyInfo: IDCardIdentifyInfo?,   // 身份识别
    val faceIdentifyInfo: FaceIdentifyInfo?,       // 人脸识别
    val detectInfo: DetectInfo,
)

/**
 * 上传记录后返回值
 */
data class DevResponse(
    val status: String,  //状态
    val msg: String,     // 消息
    val data: DevData
)

/**
 * 平台返回的图片ID和图片上传的url
 */
data class DevData(
    val idCardPhotoId: String,   //身份证照片Id
    val idCardPhotoUrl: String,  //身份证上传url
    val capturePhotoId: String,   // 抓拍照片ID
    val capturePhotoUrl: String  // 抓拍照片上传地址
)

/**
 * 图片上传完成提醒
 */
data class UploadComplete(
    val fileId: String
)

data class IDCardIdentifyInfo(
    val identifyStatus: String?,  // 识别状态 success/failed
    val name: String?,   //名称
    val sex: String?, //性别
    val idCardNo: String?, //身份证号码
    val validityDate: String?,//有效时间
    val idCardPhotoId: String?,  //身份证照片ID
    val identifyMsg: String?, // 识别信息
    val identifyTime: String?,
    val fileInfo: FileInfo?

)

data class FaceIdentifyInfo(
    val identifyStatus: String?, // 识别状态 success/failed
    val identifyMsg: String?, //识别信息
    val identifyTime: String,             //
    val facePhotoId: String?,              // 人脸照片ID
    val capturePhotoId: String?,           // 抓拍照片id
    val similarity: String,                  //相似度
    val fileInfo: FileInfo?
)

data class DetectInfo(
    val detectStatus: String, //探测状态
    val detectMsg: String,
    val detectTime: String,
    val detectDetails: List<AlarmInfo>
)

data class FileInfo(
    val name: String,
    val size: String,
    val mimetype: String,
    val isMultipartUpload: Boolean,
    val md5: String?
)


data class AlarmInfo(
    val zoneBit: String,                    // 区位
    val signal: String,             // 信号量
    val suspectedItem: String           // 疑似物品
)