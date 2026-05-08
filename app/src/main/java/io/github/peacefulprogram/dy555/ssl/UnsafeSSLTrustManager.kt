package io.github.peacefulprogram.dy555.ssl

import android.annotation.SuppressLint
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * 不安全的SSL信任管理器，用于绕过证书验证
 * 仅用于开发调试，生产环境不应使用
 */
@SuppressLint("TrustAllX509TrustManager")
class UnsafeSSLTrustManager : X509TrustManager {
    
    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
        // 信任所有客户端证书
    }
    
    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        // 信任所有服务器证书
    }
    
    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}