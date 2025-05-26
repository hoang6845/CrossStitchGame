package com.example.crossstitch.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.crossstitch.model.entity.PatternData
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {
    @Query("select * from Pattern where authorName is null")
    fun findAll(): Flow<List<PatternData>>

    @Query("select * from Pattern where authorName is not null")
    fun findMyPattern(): Flow<List<PatternData>>

    @Query("select * from Pattern where id = :id")
    suspend fun find(id:Int): PatternData

    @Insert
    suspend fun insert(pattern:PatternData):Long

    @Update
    suspend fun update(pattern:PatternData)

    @Delete
    suspend fun delete(pattern:PatternData)

    @Query("Delete from Pattern")
    suspend fun deleteAll()
}