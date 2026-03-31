package com.example.scylier.istudyspot.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://localhost:8080"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun getOkHttpClient(token: String? = null): OkHttpClient {
        val interceptors = mutableListOf<Interceptor>()
        interceptors.add(loggingInterceptor)

        if (!token.isNullOrEmpty()) {
            val authInterceptor = Interceptor {
                val request = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                it.proceed(request)
            }
            interceptors.add(authInterceptor)
        }

        return OkHttpClient.Builder()
            .addInterceptors(interceptors)
            .build()
    }

    fun <T> createService(serviceClass: Class<T>, token: String? = null): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient(token))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(serviceClass)
    }

    private fun OkHttpClient.Builder.addInterceptors(interceptors: List<Interceptor>): OkHttpClient.Builder {
        interceptors.forEach { addInterceptor(it) }
        return this
    }
}
