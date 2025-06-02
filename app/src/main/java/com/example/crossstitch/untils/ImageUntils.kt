package com.example.crossstitch.untils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String = "my_image_${System.currentTimeMillis()}.png") {
    val mimeType = "image/png"
    val fos: OutputStream?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10 (API 29) trở lên
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CrossStitch")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val contentResolver = context.contentResolver
        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            fos = contentResolver.openOutputStream(it)
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            fos?.close()

            // Update IS_PENDING to 0 after saving
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            contentResolver.update(imageUri, contentValues, null, null)

            Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
        }

    } else {
        // Android 9 trở xuống (API < 29)
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/CrossStitch"
        val file = File(imagesDir)
        if (!file.exists()) file.mkdirs()

        val image = File(file, filename)
        fos = FileOutputStream(image)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        // Thông báo hệ thống cập nhật ảnh
        MediaScannerConnection.scanFile(context, arrayOf(image.toString()), arrayOf(mimeType), null)

        Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
    }
}
