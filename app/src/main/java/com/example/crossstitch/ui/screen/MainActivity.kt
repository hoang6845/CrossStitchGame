package com.example.crossstitch.ui.screen

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.crossstitch.R
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.viewmodel.PatternViewModel

class MainActivity : AppCompatActivity() {
    lateinit var patternViewModel: PatternViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(applicationContext), GameProgressRepository.getInstance(applicationContext))
        patternViewModel = ViewModelProvider(this, factory).get(PatternViewModel::class.java)


        supportFragmentManager.beginTransaction().replace(R.id.fragment, PatternMenu()).commit()
    }
    fun backHome(){
        supportFragmentManager.beginTransaction().replace(R.id.fragment, PatternMenu()).commit()
    }
}