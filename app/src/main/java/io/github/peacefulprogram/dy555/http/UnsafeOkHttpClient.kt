package io.github.peacefulprogram.dy555.http

import android.annotation.SuppressLint
import io.github.peacefulprogram.dy555.ssl.UnsafeSSLTrustManager
import okhttp3.OkHttpClient
import java.security.SecureRandom
import javax.net.ssl.SSLContext

/**
 * 不安全的 OkHttpClient 工厂类
 * 用于完全绕过 SSL 证书验证
 */
object UnsafeOkHttpClient {
    
    private val unsafeTrustManager = UnsafeSSLTrustManager()
    
    private val sslSocketFactory = SSLContext.getInstance("SSL")
        .apply {
            init(null, arrayOf(unsafeTrustManager), SecureRandom())
        }.socketFactory
    
    @SuppressLint("TrustAllX509TrustManager")
    fun create(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }
            .sslSocketFactory(sslSocketFactory, unsafeTrustManager)
            .build()
    }
}