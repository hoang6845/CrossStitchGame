package com.example.crossstitch.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.PatternRepository
import kotlinx.coroutines.launch

class PatternViewModel(private val patternRepository: PatternRepository):ViewModel() {
    private val _grid = MutableLiveData<Array<IntArray>>()
    val grid:LiveData<Array<IntArray>> get() = _grid

    fun updateGrid(color:Int, row:Int, col:Int){
        _grid.value?.get(row)?.set(col, color)
    }

    fun addPattern(pattern: PatternData) = viewModelScope.launch {
        patternRepository.addPattern(pattern)
    }

    fun updatePattern(pattern: PatternData) = viewModelScope.launch {
        patternRepository.addPattern(pattern)
    }

    companion object {
        fun providerFactory(patternRepository: PatternRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PatternViewModel::class.java)) {
                        return PatternViewModel(patternRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}