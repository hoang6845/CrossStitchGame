package com.example.crossstitch.ui.screen

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.example.crossstitch.R
import com.example.crossstitch.databinding.ActivityMainBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.di.ScreenSize
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.viewmodel.ImageViewModel
import com.example.crossstitch.viewmodel.PatternViewModel

lateinit var binding: ActivityMainBinding
class MainActivity : AppCompatActivity() {
    lateinit var patternViewModel: PatternViewModel
    lateinit var imageViewModel: ImageViewModel
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val factory = PatternViewModel.providerFactory(
            PatternRepository.getInstance(applicationContext),
            GameProgressRepository.getInstance(applicationContext)
        )
        patternViewModel = ViewModelProvider(this, factory).get(PatternViewModel::class.java)

//        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.img)
//        val converterP = ConverterPixel()
//        val converter = Converter()
//        val grid = converterP.generatePatternFromBitmap(bitmap, resources.getInteger(R.integer.max_rows), resources.getInteger(R.integer.max_columns))
//        val palette = converterP.KMeansColor(grid, )
//        CoroutineScope (Dispatchers.IO).launch {
//            var idCreated = patternViewModel.addPattern(PatternData(id = null, name = "Test", collorPalette = palette!!, gridColor = converterP.quantizeColors(grid, 24, palette), image = converter.bitmapToByteArray(converterP.colorMatrixToBitmap(grid)))).await()
//            patternViewModel.addProgress(
//                GameProgress(
//                id = 0, patternId = idCreated!!.toInt(),
//                myCrossStitchGrid =  Array(resources.getInteger(R.integer.max_rows)) { IntArray(resources.getInteger(R.integer.max_columns)) { Int.MIN_VALUE } },
//                completedCells = 0,
//                mistake = 0
//            )
//            )
//        }
        imageViewModel = ViewModelProvider(this).get(ImageViewModel::class.java)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        navController!!.addOnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.homePage, R.id.gameManager -> {
                    binding.toolbar.visibility = View.GONE
                }

                else -> {
                    binding.toolbar.visibility = View.VISIBLE
                    updateToolBar(destination)
                }
            }

        }

        binding.toolbar.setNavigationOnClickListener {
            navController?.navigate(R.id.homePage)
        }

        initSetSize()
    }

    private fun initSetSize() {
        val displayMetrics = resources.displayMetrics
        ScreenSize.widthDp = displayMetrics.widthPixels / displayMetrics.density
        ScreenSize.heightDp = displayMetrics.heightPixels / displayMetrics.density
        if (ScreenSize.widthDp > 600) {
            ScreenSize.cellSizeDp = (ScreenSize.heightDp / Constants.NUMROWS)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateToolBar(destination: NavDestination){
        when (destination.id){
            R.id.menuPatternContainer -> {
                binding.titleToolbar.visibility = View.VISIBLE
                binding.titleSetting.visibility = View.GONE
                binding.titleCollection.visibility = View.GONE
                binding.titleUploadImage.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
            }
            R.id.menuPatternCollectionContainer -> {
                binding.titleToolbar.visibility = View.GONE
                binding.titleSetting.visibility = View.GONE
                binding.titleCollection.visibility = View.VISIBLE
                binding.titleUploadImage.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
            }
            R.id.createOwnPattern -> {
                binding.titleToolbar.visibility = View.GONE
                binding.titleSetting.visibility = View.GONE
                binding.titleCollection.visibility = View.GONE
                binding.titleUploadImage.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.GONE
            }
            R.id.verify -> {
                binding.titleToolbar.visibility = View.VISIBLE
                binding.titleSetting.visibility = View.GONE
                binding.titleCollection.visibility = View.GONE
                binding.titleUploadImage.visibility = View.GONE
                binding.btnDelete.visibility = View.VISIBLE
            }
        }
    }

}