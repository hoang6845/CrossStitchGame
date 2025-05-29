package com.example.crossstitch.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class PatternViewModel(private val patternRepository: PatternRepository, private val gameProgressRepository: GameProgressRepository):ViewModel() {
    private val _listPattern = MutableStateFlow<List<PatternData>>(emptyList())
    val listPattern = _listPattern.asStateFlow()
    val listPatternLiveData = listPattern.asLiveData()

    private val _listOwnPattern = MutableStateFlow<List<PatternData>>(emptyList())
    val listOwnPattern = _listOwnPattern.asStateFlow()
    val listOwnPatternLiveData = listOwnPattern.asLiveData()

    private val _listProgress = MutableStateFlow<List<GameProgress>>(emptyList())
    val listGameProgress = _listProgress.asStateFlow()
    var listGameProgressLiveData = listGameProgress.asLiveData()

    private val _currentProgress = MutableLiveData<Int>(-1)
    val currentProgress: LiveData<Int>get() = _currentProgress

    fun setProgress(value:Int){
        _currentProgress.value= value
    }

    private val _currentMistake = MutableLiveData<Int>(-1)
    val currentMistake: LiveData<Int>get() = _currentMistake

    fun setMistake(value:Int){
        _currentMistake.value= value
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            patternRepository.getAll().distinctUntilChanged().collect{list->
                if (list.isNullOrEmpty()){
                    Log.d("DATABASE", "db is null ")
                }else{
                    _listPattern.value = list
                }
            }
        }

        viewModelScope.launch (Dispatchers.IO){
            gameProgressRepository.getAll().distinctUntilChanged().collect{list ->
                if (list.isNullOrEmpty()){
                    Log.d("DATABASE", "db is null ")
                }else{
                    _listProgress.value = list
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            patternRepository.getMyPattern().distinctUntilChanged().collect{list->
                if (list.isNullOrEmpty()){
                    Log.d("DATABASE", "db is null ")
                }else{
                    _listOwnPattern.value = list
                }
            }
        }
    }


    fun addPattern(pattern: PatternData): Deferred<Long> = viewModelScope.async {
        val id = patternRepository.addPattern(pattern)
        return@async id
    }

    fun updatePattern(pattern: PatternData) = viewModelScope.launch {
        patternRepository.addPattern(pattern)
    }

    fun findPatternAsync(id: Int): Deferred<PatternData?> = viewModelScope.async {
        patternRepository.findOne(id)
    }

    fun deleteAll() = viewModelScope.launch {
        patternRepository.deleteAll()
    }

    fun deleteOne(id: Int) = viewModelScope.launch {
        patternRepository.deletePattern(patternRepository.findOne(id))
    }

    fun getProgress(patternId:Int): Deferred<GameProgress?> = viewModelScope.async {
        gameProgressRepository.findOne(patternId)
    }

     suspend fun addProgress(progress: GameProgress) {
        gameProgressRepository.addGameProgress(progress)
    }

    fun updateProgress(progress: GameProgress) = viewModelScope.launch {
        gameProgressRepository.updateGameProgress(progress)
    }



    companion object {
        fun providerFactory(patternRepository: PatternRepository, gameProgressRepository: GameProgressRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PatternViewModel::class.java)) {
                        return PatternViewModel(patternRepository, gameProgressRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}