package com.sjb.securitydoormanager.room.dao

import androidx.room.*
import com.sjb.securitydoormanager.room.bean.DoorData
import androidx.room.OnConflictStrategy.REPLACE


@Dao
interface DoorDao {

    /**
     * 插入一条数据
     */
    @Insert(onConflict = REPLACE)
    fun insertDoor(doorData: DoorData)


    /**
     * 删除一条数据
     */
    @Delete
    fun deleteDoor(doorData: DoorData)


    @Update
    fun updateDoor(doorData: DoorData)

    @Query("delete from doordata")
    fun clear()


}