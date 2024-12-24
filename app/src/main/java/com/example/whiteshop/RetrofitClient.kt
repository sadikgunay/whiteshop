package com.sadikgunay.whiteshop

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // API'nin temel URL'si
            .addConverterFactory(GsonConverterFactory.create()) // JSON'u Kotlin nesnelerine dönüştürmek için Gson kullanıyoruz
            .build()
            .create(WeatherApiService::class.java) // WeatherApiService arayüzünü kullanarak Retrofit nesnesini oluşturuyoruz
    }
}
