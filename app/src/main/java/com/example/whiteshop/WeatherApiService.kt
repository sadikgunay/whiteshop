package com.sadikgunay.whiteshop

import com.sadikgunay.whiteshop.dataclass.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("q") city: String,       // Şehir adı (örn: "Kocaeli")
        @Query("appid") apiKey: String, // API anahtarı
        @Query("units") units: String = "metric" // Celsius birimi (varsayılan olarak)
    ): Call<WeatherResponse>
}
