package com.example.crossstitch.ui.screen

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.crossstitch.R
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentHomePageBinding
import com.example.crossstitch.viewmodel.ImageViewModel
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private lateinit var homeBinding: FragmentHomePageBinding

class HomePage : Fragment() {
    private var navController: NavController? = null
    private var imageViewModel: ImageViewModel? = null
    private var photoUri: Uri? = null
    private val REQUEST_CAMERA_PERMISSION = 100
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                imageViewModel?.setBitmap(bitmap)
                val converter = ConverterPixel()
                val grid = converter.generatePatternFromBitmap(
                    bitmap,
                    resources.getInteger(R.integer.max_rows),
                    resources.getInteger(R.integer.max_columns)
                )
                val palette = converter.KMeansColor(grid, 24)
                imageViewModel?.setPalette(palette)
                imageViewModel?.setGrid(converter.quantizeColors(grid, 24, palette))

                navController?.navigate(R.id.createOwnPattern)
            }
        }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 1 // giảm độ phân giải (1: giữ nguyên, 2: giảm 1/2, 4: giảm 1/4)
                }
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                if (bitmap != null) {
                    imageViewModel?.setBitmap(bitmap)
                    val converter = ConverterPixel()
                    val grid = converter.generatePatternFromBitmap(
                        bitmap,
                        resources.getInteger(R.integer.max_rows),
                        resources.getInteger(R.integer.max_columns)
                    )
                    val palette = converter.KMeansColor(grid, 24)
                    imageViewModel?.setPalette(palette)
                    imageViewModel?.setGrid(converter.quantizeColors(grid, 24, palette))

                    navController?.navigate(R.id.createOwnPattern)
                }else {
                    Toast.makeText(requireContext(), "Chụp ảnh thanh cong nhung convert anh that bai", Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            Toast.makeText(requireContext(), "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show()
        }

        photoUri = null

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        imageViewModel = ViewModelProvider(requireActivity()).get(ImageViewModel::class.java)
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
            handleCreateOwnPattern()
        }
        homeBinding.btnInProgress.setOnClickListener {
            navController?.navigate(R.id.menuPatternCollectionContainer)
        }
        return homeBinding.root
    }

    private fun handleCreateOwnPattern() {
        AlertDialog.Builder(requireContext())
            .setTitle("Pick image from")
            .setItems(arrayOf("Gallery", "Take a photo")) { dialog, which ->
                when (which) {
                    0 -> {
                        pickImageLauncher.launch("image/*")
                    }

                    1 -> {
                        checkCameraPermission()
                    }
                }
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun handleCapture(){
        var captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        photoUri?.let {    takePictureLauncher.launch(it)}
    }
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
    fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            handleCapture()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleCapture()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}