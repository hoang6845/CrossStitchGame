package com.example.crossstitch.converter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream

class Converter {
    @TypeConverter
    fun fromListInt(value: List<Int>):String?{
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toListInt(value: String):List<Int>{
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromGlid(value: Array<IntArray>):String{
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toGlid(value: String):Array<IntArray>{
        return Gson().fromJson(value, Array<IntArray>::class.java)
    }

    @TypeConverter
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
    @TypeConverter
    fun byteArrayToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}