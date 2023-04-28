package com.sjb.securitydoormanager.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sjb.securitydoormanager.SJBApp

/**
 * desc: 数据库的操作类
 */
abstract class AppDataBase : RoomDatabase() {

    companion object {
        private var instance: AppDataBase? = null

        fun getInstance(): AppDataBase {
            return instance?: synchronized(this){
                instance ?: buildDataBase(SJBApp.context).also{
                    instance=it
                }
            }

        }

        private fun buildDataBase(context:Context):AppDataBase{
            return Room.databaseBuilder(context,AppDataBase::class.java,"sjb-database")
                .addCallback(object :RoomDatabase.Callback(){
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                }).build()
        }

    }


}