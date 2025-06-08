package com.example.crossstitch.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VerifyViewModel: ViewModel() {
    private val _bitmap = MutableLiveData<Bitmap?>()
    val bitmap: LiveData<Bitmap?> get() = _bitmap

    fun setBitmap(newBitmap: Bitmap) {
        _bitmap.value = newBitmap
    }

    fun clearBitMap(){
        _bitmap.value = null
    }

    private val _category = MutableLiveData<String?>()
    val category: LiveData<String?> get() = _category
    fun setCategory(newCategory: String?) {
        _category.value = newCategory
    }

    private val _position = MutableLiveData<Int>(0)
    val position: LiveData<Int> get() = _position

    fun setPosition(value: Int){
        _position.value = value

    }


}