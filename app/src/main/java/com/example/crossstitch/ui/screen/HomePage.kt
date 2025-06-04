package com.example.crossstitch.ui.screen

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.crossstitch.R
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentHomePageBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.utils.TakePhotoUtils
import com.example.crossstitch.viewmodel.ImageViewModel

private lateinit var homeBinding: FragmentHomePageBinding

class HomePage : Fragment() {
    private var navController: NavController? = null
    private var imageViewModel: ImageViewModel? = null
    private lateinit var takePhotoUtils: TakePhotoUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        imageViewModel = ViewModelProvider(requireActivity()).get(ImageViewModel::class.java)

        takePhotoUtils = TakePhotoUtils(
            this,
            onImagePicked = { bitmap: Bitmap ->
                imageViewModel?.setBitmap(bitmap)
                val converter = ConverterPixel()
                val grid = converter.generatePatternFromBitmap(
                    bitmap,
                    Constants.NUMROWS,
                    Constants.NUMCOLS)
                val palette = converter.KMeansColor(grid, Constants.MAX_COLORS)
                imageViewModel?.setPalette(palette)
                imageViewModel?.setGrid(converter.quantizeColors(grid, Constants.MAX_COLORS, palette))

                navController?.navigate(R.id.createOwnPattern)
            },
            onError = { errorMessage: String ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeBinding = FragmentHomePageBinding.inflate(layoutInflater, container, false)
        homeBinding.btnStart.setOnClickListener {
            navController?.navigate(R.id.menuPatternContainer)
        }
        homeBinding.btnCreate.setOnClickListener {
            takePhotoUtils.showImagePickerDialog()
        }
        homeBinding.btnInProgress.setOnClickListener {
            navController?.navigate(R.id.menuPatternCollectionContainer)
        }
        return homeBinding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        takePhotoUtils.handlePermissionResult(requestCode, grantResults)
    }
}