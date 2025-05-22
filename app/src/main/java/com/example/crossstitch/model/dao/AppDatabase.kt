package com.example.crossstitch.model.dao

import androidx.room.Database
import com.example.crossstitch.model.entity.PatternData

@Database(entities = [PatternData::class], version = 1)
abstract class AppDatabase {
}