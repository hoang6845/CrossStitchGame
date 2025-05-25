package com.example.crossstitch.repository

import android.content.Context
import com.example.crossstitch.di.AppModule
import com.example.crossstitch.model.dao.GameProgressDao
import com.example.crossstitch.model.entity.GameProgress

class GameProgressRepository(private val gameProgressDao: GameProgressDao) {
    fun getAll() = gameProgressDao.getAll()
    suspend fun addGameProgress(gameProgress: GameProgress) = gameProgressDao.addGameProgress(gameProgress)
    suspend fun updateGameProgress(gameProgress: GameProgress) = gameProgressDao.updateProgress(gameProgress)
    suspend fun findOne(patternId:Int):GameProgress? = gameProgressDao.getProgressByPatternId(patternId)

    companion object{
        private var _instance:GameProgressRepository? = null
        fun getInstance(context: Context): GameProgressRepository {
            if (_instance == null){
                _instance = GameProgressRepository(AppModule.getGameProgressDao(context))
            }
            return _instance as GameProgressRepository
        }
    }
}