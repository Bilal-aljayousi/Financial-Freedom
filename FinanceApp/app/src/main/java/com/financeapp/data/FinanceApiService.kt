package com.financeapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface FinanceApiService {
    @GET("v8/finance/chart")
    suspend fun getStockData(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "1d",
        @Query("range") range: String = "1d"
    ): StockResponse
}

data class StockResponse(
    val chart: ChartResult?
)

data class ChartResult(
    val result: List<ChartQuote>?
)

data class ChartQuote(
    val meta: StockMeta?
)

data class StockMeta(
    val symbol: String,
    val regularMarketPrice: Double,
    val previousClose: Double
)

object FinanceApi {
    private const val BASE_URL = "https://query1.finance.yahoo.com/"

    val retrofit: FinanceApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FinanceApiService::class.java)
    }
}
