package com.sjb.securitydoormanager.room.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doorData")
class DoorData (
    @PrimaryKey(autoGenerate = true)
    val id :Int,
    @ColumnInfo(name = "zone1")
    val zone1:Int,
    @ColumnInfo(name = "zone2")
    val zone2:Int,
    @ColumnInfo(name = "zone3")
    val zone3:Int,
    @ColumnInfo(name = "zone4")
    val zone4:Int,
    @ColumnInfo(name = "zone5")
    val zone5:Int,
    @ColumnInfo(name = "zone6")
    val zone6:Int,
    @ColumnInfo(name = "zone7")
    val zone7:Int,
    @ColumnInfo(name = "zone8")
    val zone8:Int,
    @ColumnInfo(name = "zone9")
    val zone9:Int,
    @ColumnInfo(name = "zone10")
    val zone10:Int,
    @ColumnInfo(name = "zone11")
    val zone11:Int,
    @ColumnInfo(name = "zone12")
    val zone12:Int,
    @ColumnInfo(name = "zone13")
    val zone13:Int,
    @ColumnInfo(name = "zone14")
    val zone14:Int,
    @ColumnInfo(name = "zone15")
    val zone15:Int,
    @ColumnInfo(name = "zone16")
    val zone16:Int
)