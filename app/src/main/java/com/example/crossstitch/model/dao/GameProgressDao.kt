package com.example.crossstitch.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.crossstitch.model.entity.GameProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProgressDao {
    @Query("select * from GameProgress")
    fun getAll(): Flow<List<GameProgress>>

    @Insert
    suspend fun addGameProgress(gameProgress: GameProgress)

    @Update
    suspend fun updateProgress(progress: GameProgress)

    @Query("SELECT * FROM GameProgress WHERE patternId = :patternId LIMIT 1")
    suspend fun getProgressByPatternId(patternId: Int): GameProgress?

}