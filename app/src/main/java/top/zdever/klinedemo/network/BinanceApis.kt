package top.zdever.klinedemo.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/12
 */
interface BinanceApis {

    @GET("api/v3/klines")
    suspend fun klines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("startTime") startTime: Long? = null,
        @Query("endTime") endTime: Long? = null
    ):List<List<Any>>

}