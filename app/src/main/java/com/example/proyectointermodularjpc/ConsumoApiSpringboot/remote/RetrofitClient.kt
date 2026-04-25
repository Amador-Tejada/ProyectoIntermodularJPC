package com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8081/"

    private var currentToken: String? = null

    /**
     * Función para actualizar el token cuando el usuario se autentica
     */
    fun updateToken(token: String?) {
        currentToken = token
    }

    /**
     * Interceptor que añade Authorization: Bearer <token> si hay token.
     */
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val token = currentToken

        val requestConAuth = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        chain.proceed(requestConAuth)
    }

    /**
     * Interceptor de diagnóstico para ver qué está enviando/recibiendo la API.
     */
    private val peekBodyInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        try {
            val snippet = response.peekBody(4_096).string()
            android.util.Log.d(
                "API",
                "${request.method} ${request.url} -> ${response.code} ${response.message}; body='${snippet.take(500)}'",
            )
        } catch (_: Throwable) {
        }

        response
    }

    /**
     * Gson estándar sin políticas de nombres automáticas para evitar desajustes con Spring Boot.
     */
    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(peekBodyInterceptor)
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
