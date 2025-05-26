package com.example.crossstitch.model.entity

import android.content.Context
import android.content.SharedPreferences

class UserManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

    // Kiểm tra xem đây có phải là lần đầu tiên vào ứng dụng không
    fun getUserName(): String {
        return sharedPreferences.getString(USER_NAME_KEY, "Guest") ?: "Guest"
    }

    // Lưu tên người dùng
    fun saveUserName(name: String) {
        val editor = sharedPreferences.edit()
        editor.putString(USER_NAME_KEY, name)
        editor.apply()
    }

    // Kiểm tra xem có phải lần đầu tiên mở ứng dụng không
    fun isFirstTimeUser(): Boolean {
        return !sharedPreferences.contains(USER_NAME_KEY)
    }

    companion object {
        private const val USER_NAME_KEY = "user_name"
    }
}
