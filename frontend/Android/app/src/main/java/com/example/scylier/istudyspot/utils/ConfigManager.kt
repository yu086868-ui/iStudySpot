package com.example.scylier.istudyspot.utils

import android.content.Context
import android.content.SharedPreferences

class ConfigManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("iStudySpot", Context.MODE_PRIVATE)

    companion object {
        private var instance: ConfigManager? = null

        fun getInstance(context: Context): ConfigManager {
            if (instance == null) {
                instance = ConfigManager(context.applicationContext)
            }
            return instance!!
        }
    }

    // Token相关
    fun saveToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun removeToken() {
        sharedPreferences.edit().remove("token").apply()
    }

    // 用户信息相关
    fun saveUserId(id: String) {
        sharedPreferences.edit().putString("userId", id).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("userId", null)
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString("username", null)
    }

    fun saveNickname(nickname: String) {
        sharedPreferences.edit().putString("nickname", nickname).apply()
    }

    fun getNickname(): String? {
        return sharedPreferences.getString("nickname", null)
    }

    // 清除所有数据
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
