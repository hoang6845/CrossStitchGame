package com.example.crossstitch.di

import android.content.Context
import androidx.room.Room
import com.example.crossstitch.model.dao.AppDatabase
import com.example.crossstitch.model.dao.GameProgressDao
import com.example.crossstitch.model.dao.PatternDao

object AppModule {

    @Volatile
    private var _instance:AppDatabase?=null

    fun getDB(context:Context): AppDatabase {
        return _instance?: synchronized(this){
            _instance?: Room.databaseBuilder(context, AppDatabase::class.java, "Cross Stitch")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also {
                    _instance = it
                    _instance
                }
        }
    }

    fun getPatternDao(context: Context):PatternDao{
        return getDB(context).patternDao()
    }

    fun getGameProgressDao(context: Context):GameProgressDao{
        return getDB(context).gameProgressDao()
    }
}