package com.example.crossstitch.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Pattern")
data class PatternData(
    @PrimaryKey(autoGenerate = true)
    var id:Int?=null,
    var collorPalette:List<Int>,
    var name:String,
    var gridColor: Array<IntArray>,
    var image: ByteArray
) {

}