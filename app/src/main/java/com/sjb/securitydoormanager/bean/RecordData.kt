package com.sjb.securitydoormanager.bean

/**
 * desc: 安检记录状态
 * 通过时间-防区位置-结果
 */
data class RecordData(
    val time: String,
    val zone: String,
    val result: String
)