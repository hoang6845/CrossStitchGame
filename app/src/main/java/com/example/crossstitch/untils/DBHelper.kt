package com.example.crossstitch.untils

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.room.Room
import com.example.crossstitch.model.dao.AppDatabase
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DBHelper(private val context: Context) {
    private val databaseName = "CrossStitchDB" // Tên cơ sở dữ liệu của bạn (trùng với tên file DB)
    private val dbPath = context.getDatabasePath(databaseName)

    // Hàm xóa cơ sở dữ liệu hiện tại
    fun clearCurrentDatabase() {
        if (dbPath.exists()) {
            dbPath.delete()
        }
    }

    // Sao chép cơ sở dữ liệu mới từ assets vào thư mục cơ sở dữ liệu của ứng dụng
    fun copyDatabaseFromAssets() {
        // Kiểm tra nếu cơ sở dữ liệu đã tồn tại
        if (!dbPath.exists()) {
            try {
                val assetManager: AssetManager = context.assets
                val files = assetManager.list("") // Lấy tất cả tệp trong thư mục assets
                Log.d("check assets", "Files in assets: ${files?.joinToString() ?: "No files"}")  // In danh sách tệp trong assets

                // Kiểm tra xem tệp CrossStitchDB có tồn tại không
                val dbExists = files?.contains("CrossStitchDB") ?: false
                if (dbExists) {
                    Log.d("check assets", "CrossStitchDB found in assets")
                } else {
                    Log.d("check assets", "CrossStitchDB not found in assets")
                }

                // Sao chép tệp từ assets vào thư mục cơ sở dữ liệu
                val inputStream: InputStream = assetManager.open("CrossStitchDB")
                val outputStream: OutputStream = FileOutputStream(dbPath)

                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                Log.d("check assets Db", "Database copied successfully to: ${dbPath.absolutePath}")

            } catch (e: IOException) {
                Log.e("check assets Db", "Error copying database from assets", e)
            }
        } else {
            Log.d("check assets Db", "Database already exists, no need to copy")
        }
        // Kiểm tra tệp đã được sao chép vào đúng thư mục cơ sở dữ liệu hay chưa
        if (dbPath.exists()) {
            Log.d("DBHelper", "Database copied successfully: ${dbPath.absolutePath}")
        } else {
            Log.d("DBHelper", "Database is existed.")
        }
    }


    // Hàm khởi tạo lại cơ sở dữ liệu với Room
    fun initializeRoomDatabase(): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, databaseName
        ).fallbackToDestructiveMigration()
            .build()
    }
}