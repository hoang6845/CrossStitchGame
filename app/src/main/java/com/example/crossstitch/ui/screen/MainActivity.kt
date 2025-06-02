package com.example.crossstitch.ui.screen

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.savedstate.SavedState
import com.example.crossstitch.R
import com.example.crossstitch.converter.Converter
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.ActivityMainBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.di.ScreenSize
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.viewmodel.ImageViewModel
import com.example.crossstitch.viewmodel.PatternViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
        setSupportActionBar(mainBinding.toolbar)
        navController!!.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener{
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: SavedState?
            ) {
                when (destination.id){
                    R.id.homePage -> {
                        mainBinding.toolbar.visibility = View.GONE
                    }
                    else -> {
                        mainBinding.toolbar.visibility = View.VISIBLE
                        updateOnMenu(destination)
                    }
                }
                updateOnMenu(destination)
            }
        })

        mainBinding.toolbar.setNavigationOnClickListener{
            navController?.navigateUp()
        }

        val displayMetrics = resources.displayMetrics
        ScreenSize.widthDp = displayMetrics.widthPixels/displayMetrics.density
        ScreenSize.heightDp = displayMetrics.heightPixels/displayMetrics.density
        if (ScreenSize.widthDp>600){
            ScreenSize.cellSizeDp = (ScreenSize.heightDp/Constants.NUMROWS)
        }

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

    fun updateOnMenu(destination: NavDestination){
        when(destination.id){
            R.id.menuPatternContainer -> {
                mainBinding.toolbar.menu.findItem(R.id.menuPatternContainer).setIcon(R.drawable.interests_24px)
                mainBinding.toolbar.menu.findItem(R.id.ownPatternMenu).setIcon(R.drawable.photo_library_24px_default)
            }
            R.id.ownPatternMenu -> {
                mainBinding.toolbar.menu.findItem(R.id.menuPatternContainer).setIcon(R.drawable.interests_24px_default)
                mainBinding.toolbar.menu.findItem(R.id.ownPatternMenu).setIcon(R.drawable.photo_library_24px)
            }
            else -> {
                mainBinding.toolbar.menu.findItem(R.id.menuPatternContainer).setIcon(R.drawable.interests_24px_default)
                mainBinding.toolbar.menu.findItem(R.id.ownPatternMenu).setIcon(R.drawable.photo_library_24px_default)
            }
        }
    }
}