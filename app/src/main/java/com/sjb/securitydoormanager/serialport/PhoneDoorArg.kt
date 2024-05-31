package com.sjb.securitydoormanager.serialport
// 手机门参数
data class PhoneDoorArg(
    val zone1Spl:Int,
    val zone2Spl:Int,
    val zone3Spl:Int,
    val zone4Spl:Int,        // 1-6区的灵敏度
    val zone5Spl:Int,
    val zone6Spl:Int,

    val overallSpl:Int,    // Data[19]= all_sensi/128; Data[20]= all_sensi%128;  整体灵敏度
    val zoneNumber:Int,    //Data[21]= zone_num 区位数  值00000=0表示6区       值00001=1表示12区   值00010=2表示18区

    val frequency:Int,      // 频点

    val language:Int,      //6--2位：语言0:中文  1:英文   2:土耳其语   3:俄语   4:波兰语 第0位：语言锁    0:语言可选   1:只有英文

     // Data[24]= (auto_set_f<<6)|(poweron_start<<5)|(alarm_mode<<4)|  DoorType
    // 6位：开机自动设频   0：否   1：是
    //5位：上电自动开机   0：是   1：否
    //4位：报警模式       0：挡住红外触发   1：飞物报警
    //3~0位：0：测温测金属门  1：测温门   2：普通测金属门  3：便携门
    //        4：手机门        5：带测温手机门



)
