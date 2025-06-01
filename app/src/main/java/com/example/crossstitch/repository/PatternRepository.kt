package com.example.crossstitch.repository

import android.content.Context
import com.example.crossstitch.di.AppModule
import com.example.crossstitch.model.dao.PatternDao
import com.example.crossstitch.model.entity.PatternData

class PatternRepository(private val patternDao: PatternDao) {
    fun getAll() = patternDao.findAll()
    suspend fun addPattern(pattern: PatternData):Long = patternDao.insert(pattern)
    suspend fun updatePattern(pattern: PatternData) = patternDao.update(pattern)
    suspend fun deletePattern(pattern: PatternData) = patternDao.delete(pattern)
    suspend fun findOne(id:Int):PatternData = patternDao.find(id)
    suspend fun deleteAll() = patternDao.deleteAll()
    suspend fun findAllCategory():List<String> = patternDao.findAllCategory()

    companion object{
        private var _instance:PatternRepository? = null
        fun getInstance(context: Context): PatternRepository {
            if (_instance == null){
                _instance = PatternRepository(AppModule.getPatternDao(context))
            }
            return _instance as PatternRepository
        }
    }
}