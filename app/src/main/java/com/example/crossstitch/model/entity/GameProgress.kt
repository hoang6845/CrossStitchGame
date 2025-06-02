package com.example.crossstitch.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "GameProgress",
    foreignKeys = [
        ForeignKey(
            entity = PatternData::class,
            parentColumns = ["id"],
            childColumns = ["patternId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patternId"], unique = true)]


)
data class GameProgress(
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,
    var patternId:Int,
    var myCrossStitchGrid: Array<IntArray>,
    var completedCells:Int,
    var mistake:Int
) {
}