package com.sadikgunay.whiteshop

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.sadikgunay.whiteshop.dataclass.WeatherResponse
import com.sadikgunay.whiteshop.R
import com.sadikgunay.whiteshop.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var navHostFragment: NavHostFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bottom Navigation Setup
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        if (navHostFragment != null) {
            val navController = navHostFragment!!.navController
            NavigationUI.setupWithNavController(binding.bottomNav, navController)
        }

        // Hava Durumu API Çağrısı
        val weatherTextView: TextView = findViewById(R.id.weatherTextView)

        val apiKey = "892b310d6eeb2adcd31a6e0080cbe388" // API Anahtarı
        val city = "Kocaeli" // Şehir Adı

        // API'yi çağırma
        RetrofitClient.weatherApi.getWeather(city, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let {
                        val displayText = """
                            Şehir: ${it.name}
                            Sıcaklık: ${it.main.temp}°C
                            Nem: ${it.main.humidity}%
                        """.trimIndent()
                        weatherTextView.text = displayText
                    }
                } else {
                    weatherTextView.text = "Hata: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherTextView.text = "Hata: ${t.message}"
            }
        })
    }
}
