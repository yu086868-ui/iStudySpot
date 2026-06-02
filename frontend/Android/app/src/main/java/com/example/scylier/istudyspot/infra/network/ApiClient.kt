package com.example.scylier.istudyspot.infra.network

import com.example.scylier.istudyspot.BuildConfig
import com.example.scylier.istudyspot.models.BaseResponse
import com.example.scylier.istudyspot.models.auth.TokenResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {
    private val baseUrl: String = BuildConfig.BASE_URL
    private val gson = Gson()

    var currentToken: String? = null
    var currentRefreshToken: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        currentToken?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private val tokenAuthenticator = Authenticator { _, response ->
        val refreshToken = currentRefreshToken ?: return@Authenticator null
        synchronized(this) {
            val newToken = refreshAccessToken(refreshToken) ?: return@Authenticator null
            currentToken = newToken
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val json = gson.toJson(mapOf("refreshToken" to refreshToken))
            val body = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("${baseUrl}api/auth/refresh")
                .post(body)
                .build()
            val client = OkHttpClient.Builder().build()
            val refreshResponse = client.newCall(request).execute()
            if (refreshResponse.isSuccessful) {
                val responseBody = refreshResponse.body?.string()
                val jsonObject = responseBody?.let { com.google.gson.JsonParser.parseString(it).asJsonObject }
                val code = jsonObject?.get("code")?.asInt ?: 0
                val dataObj = jsonObject?.getAsJsonObject("data")
                val token = dataObj?.get("token")?.asString
                if (code in 200..299 && token != null) {
                    token
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    private val okHttpClient: OkHttpClient = if (BuildConfig.DEBUG) {
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    } else {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
