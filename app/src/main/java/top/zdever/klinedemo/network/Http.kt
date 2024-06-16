package top.zdever.klinedemo.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/12
 */
object Http {

    private val okClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
//            .client(okClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    fun <T> create(clazz:Class<T>):T{
        return retrofit.create(clazz)
    }

}