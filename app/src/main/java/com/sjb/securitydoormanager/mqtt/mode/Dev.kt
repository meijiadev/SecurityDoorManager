package com.sjb.securitydoormanager.mqtt.mode

/**
 * author:10500
 * date: 2023/2/6 17:12
 * desc: Dev
 */


// 设备上下线通知
data class DeviceStatus(
    val tx: String,
    val status: Int,            // 标记 online/offline   1/0
    val connectTime: String,    // 2023-02-02 15:09:32
    val model: String,          //
    val version: String
)

/**
 * 安检门获取应用场景
 */
data class Scenario(
    val scenario: String,
    val config: Config
)


// 设备配置
data class DevConfig(
    val tx: String,
    val config: Config,          // 配置信息
    val scenario: String        // 应用场景
)

data class Config(
    val frequency: Int,         // 频率
    val sensitive: Int          // 灵敏度
)

// 安检记录上报
data class DevRecord(
    val tx: String,
    val alarmInfo: List<AlarmInfo>,
    val passMode: String,                   // 通过方式 IN/OUT
    val scenario: String,             // 应用场景
    val passStatus: Int,                 // 通过状态  0 -未通过 1- 通过
    val passTime: String                   // 通过时间
)

data class AlarmInfo(
    val area: String,                    // 区位
    val signal: String,             // 信号量
    val suspectedItem: String           // 疑似物品
)