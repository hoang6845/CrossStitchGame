package com.example.crossstitch.model.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.crossstitch.converter.Converter
import com.example.crossstitch.model.entity.PatternData

@Database(entities = [PatternData::class], version = 1)
@TypeConverters(value = [Converter::class])
abstract class AppDatabase:RoomDatabase() {
    abstract fun patternDao():PatternDao
}