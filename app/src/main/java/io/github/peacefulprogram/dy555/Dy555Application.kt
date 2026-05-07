package io.github.peacefulprogram.dy555

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import cn.hutool.crypto.digest.MD5
import coil.ImageLoader
import coil.ImageLoaderFactory
import io.github.peacefulprogram.dy555.ext.showLongToast
import io.github.peacefulprogram.dy555.http.HttpDataRepository
import io.github.peacefulprogram.dy555.room.Dy555Database
import io.github.peacefulprogram.dy555.viewmodel.CategoriesViewModel
import io.github.peacefulprogram.dy555.viewmodel.HomeViewModel
import io.github.peacefulprogram.dy555.viewmodel.PlayHistoryViewModel
import io.github.peacefulprogram.dy555.viewmodel.PlaybackViewModel
import io.github.peacefulprogram.dy555.viewmodel.SearchResultViewModel
import io.github.peacefulprogram.dy555.viewmodel.SearchViewModel
import io.github.peacefulprogram.dy555.viewmodel.VideoDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class Dy555Application : Application(), ImageLoaderFactory {

    private val TAG = Dy555Application::class.java.simpleName

    override fun onCreate() {
        context = this
        // Load saved M3U8 API server address
        Constants.M3U8_EXTRACT_API_SERVER = io.github.peacefulprogram.dy555.util.PreferenceManager.getM3u8ApiServer(this)
        // Load saved base URL (网站地址) - 添加这行
        val savedBaseUrl = io.github.peacefulprogram.dy555.util.PreferenceManager.getBaseUrl(this)
        if (!savedBaseUrl.isNullOrBlank()) {
            Constants.BASE_URL = savedBaseUrl
        }
        startKoin {
            androidContext(this@Dy555Application)
            androidLogger()
            modules(httpModule(), viewModelModule(), roomModule())
        }
        // First update base URL to ensure we have a working domain
        reloadVideoServer()
        //reloadVideoServertest()
        super.onCreate()
    }

    private fun roomModule() = module {
        single {
            Room.databaseBuilder(this@Dy555Application, Dy555Database::class.java, "dy555").apply {
                if (BuildConfig.DEBUG) {
                    val queryCallback = object : RoomDatabase.QueryCallback {
                        override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                            Log.i(TAG, "room sql: $sqlQuery  args: $bindArgs")
                        }
                    }
                    setQueryCallback(queryCallback, Executors.newSingleThreadExecutor())
                }
            }.build()
        }

        single {
            get<Dy555Database>().searchHistoryDao()
        }

        single {
            get<Dy555Database>().videoHistoryDao()
        }

        single {
            get<Dy555Database>().episodeHistoryDao()
        }

    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this).okHttpClient {
        OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor { chain ->
                chain.request().newBuilder().header("user-agent", Constants.USER_AGENT)
                    .header("referer", Constants.BASE_URL).build().let { chain.proceed(it) }
            }
            .sslSocketFactory(sslSocketFactory, trustManager)
            .build()