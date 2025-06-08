package com.example.crossstitch.utils

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.crossstitch.R
import com.example.crossstitch.di.Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TakePhotoUtils(
    private val fragment: Fragment,
    private val onImagePicked: (bitmap: Bitmap) -> Unit,
    private val onError: (errorMessage: String) -> Unit,
) {
    private var photoUri: Uri? = null


    private val pickImageLauncher: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleImageUri(it) }
        }

    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let { handleImageUri(it, 2) }
            } else {
                onError("Take photo failed")
            }
            photoUri = null
        }

    private fun handleImageUri(uri: Uri, sampleSize: Int = 2) {
        try {
            val inputStream = fragment.requireContext().contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize // Giảm độ phân giải để tránh OutOfMemory
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            if (bitmap != null) {
                onImagePicked(bitmap)
            } else {
                onError("Read image failed")
            }
        } catch (e: Exception) {
            onError("Fail: ${e.message}")
        }
    }

    fun showImagePickerDialog() {
        val dialogView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.dialog_image_picker, null)
        val dialog = AlertDialog.Builder(fragment.requireContext())
            .setView(dialogView)
            .create()
        dialogView.findViewById<LinearLayout>(R.id.btn_capture_camera).setOnClickListener{
            captureFromCamera()
            dialog.dismiss()
        }
        dialogView.findViewById<LinearLayout>(R.id.btn_pick_gallery).setOnClickListener{
            pickFromGallery()
            dialog.dismiss()
        }
        dialogView.findViewById<AppCompatButton>(R.id.btn_cancel_dialog).setOnClickListener{
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
    }

    private fun pickFromGallery() {
        pickImageLauncher.launch("image/*")
    }


    private fun captureFromCamera() {
        checkCameraPermissionAndCapture()
    }

    private fun checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                fragment.requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                Constants.REQUEST_CAMERA_PERMISSION
            )
        } else {
            handleCapture()
        }
    }

    private fun handleCapture() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                fragment.requireContext(),
                "${fragment.requireContext().packageName}.provider",
                photoFile
            )
            photoUri?.let { takePictureLauncher.launch(it) }
        } catch (e: Exception) {
            onError("Không thể tạo file ảnh: ${e.message}")
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir =
            fragment.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == Constants.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleCapture()
            } else {
                onError("Need Permission to capture")
            }
        }
    }

}