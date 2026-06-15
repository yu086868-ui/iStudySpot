package com.example.scylier.istudyspot.utils

import android.content.Context
import android.content.SharedPreferences

class ConfigManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("iStudySpot", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: ConfigManager? = null

        fun getInstance(context: Context): ConfigManager {
            return instance ?: synchronized(this) {
                instance ?: ConfigManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun removeToken() {
        sharedPreferences.edit().remove("token").apply()
    }

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

    fun saveThemeMode(mode: String) {
        sharedPreferences.edit().putString("theme_mode", mode).apply()
    }

    fun getThemeMode(): String? {
        return sharedPreferences.getString("theme_mode", null)
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
