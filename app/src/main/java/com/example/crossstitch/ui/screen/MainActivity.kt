package com.example.crossstitch.ui.screen

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import com.example.crossstitch.R
import com.example.crossstitch.databinding.ActivityMainBinding
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.UserManager
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.viewmodel.ImageViewModel
import com.example.crossstitch.viewmodel.PatternViewModel
lateinit var mainBinding: ActivityMainBinding
class MainActivity : AppCompatActivity() {
    lateinit var patternViewModel: PatternViewModel
    lateinit var imageViewModel:ImageViewModel
    var navController: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(applicationContext), GameProgressRepository.getInstance(applicationContext))
        patternViewModel = ViewModelProvider(this, factory).get(PatternViewModel::class.java)

        imageViewModel = ViewModelProvider(this).get(ImageViewModel::class.java)


        var userManager = UserManager(this)

        // Kiểm tra xem có phải lần đầu tiên vào app không
        if (userManager.isFirstTimeUser()) {
            // Nếu lần đầu, đặt tên mặc định và lưu lại
            userManager.saveUserName("Hoang")
        }
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
        setSupportActionBar(mainBinding.toolbar)
//        supportFragmentManager.beginTransaction().replace(R.id.fragment, PatternMenu()).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)|| navController?.let {
            item.onNavDestinationSelected(
                it
            )
        } == true
    }
}