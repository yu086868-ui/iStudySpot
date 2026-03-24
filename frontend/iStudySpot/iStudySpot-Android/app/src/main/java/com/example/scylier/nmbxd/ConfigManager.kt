package com.example.scylier.nmbxd
import android.content.Context
import android.content.SharedPreferences

object ConfigManager {

    private const val PREFS_NAME = "app_settings"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ---------- 通用 get/set ----------

    fun getString(key: String, default: String? = null): String? =
        prefs.getString(key, default)

    fun getInt(key: String, default: Int = 0): Int =
        prefs.getInt(key, default)

    fun getFloat(key: String, default: Float = 0f): Float =
        prefs.getFloat(key, default)

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun set(key: String, value: Any) {
        with(prefs.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Int    -> putInt(key, value)
                is Float  -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                else -> throw IllegalArgumentException("Unsupported value type: ${value::class}")
            }
            apply()
        }
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // ---------- 常量 Key 定义 ----------

    object Keys {
        const val BACKGROUND_OPTION = "background_option"  // 1=默认 2=自定义
        const val BACKGROUND_ALPHA = "background_alpha"
        const val CUSTOM_BACKGROUND_URI = "custom_background_uri"
        const val SHOW_FESTIVAL = "show_festival"  // 0=不显示, 1=公历节日, 2=公历+农历节日
        const val WEATHER_API_KEY = "weather_api_key"
        const val SHOW_LUNAR = "show_lunar"
    }
}