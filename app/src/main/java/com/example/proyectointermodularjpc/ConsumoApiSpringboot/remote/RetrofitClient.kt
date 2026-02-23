package com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    /**
     * URL base del servidor donde está desplegada la API REST
     *
     * IMPORTANTE PARA LOS ALUMNOS:
     * - Si usáis el emulador de Android Studio: http://10.0.2.2:8080/
     *   (10.0.2.2 es una IP especial que el emulador entiende como "localhost de mi PC")
     *
     * - Si usáis un móvil físico conectado a la misma WiFi: http://TU_IP_LOCAL:8080/
     *   (podéis ver vuestra IP en Windows con "ipconfig" o en Mac con "ifconfig")
     *
     * - Si habéis desplegado el servidor en la nube: https://tu-servidor.com/
     */
    private const val BASE_URL = "http://10.0.2.2:8081/"

    /**
     * Variable que almacena el token JWT actual
     * Se actualiza después del login o registro
     */
    private var currentToken: String? = null

    /**
     * Función para actualizar el token cuando el usuario se autentica
     * Debe llamarse desde el ViewModel después de un login/registro exitoso
     *
     * @param token El nuevo token JWT (o null para cerrar sesión)
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
     * Interceptor de diagnóstico: registra código + un snippet del body (hasta 4KB).
     * Esto ayuda cuando Gson dice "Use JsonReader.setLenient(true)" porque normalmente
     * el servidor está devolviendo HTML/texto en lugar de JSON.
     */
    private val peekBodyInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        try {
            val snippet = response.peekBody(4_096).string()
            android.util.Log.d(
                "API",
                "${request.method} ${request.url} -> ${response.code} ${response.message}; body='${snippet.replace("\n", " ").take(500)}'",
            )
        } catch (_: Throwable) {
            // Ignorar errores de logging.
        }

        response
    }

    /**
     * Gson lenient: evita que la app reviente si el backend manda JSON con pequeñas irregularidades.
     * OJO: si el backend manda HTML (Whitelabel, etc.), seguirá fallando, pero el log del interceptor
     * te dirá exactamente qué está llegando.
     */
    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * Cliente HTTP con logging y auth.
     */
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

    /**
     * Instancia de Retrofit configurada.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Servicio API listo para usar.
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}