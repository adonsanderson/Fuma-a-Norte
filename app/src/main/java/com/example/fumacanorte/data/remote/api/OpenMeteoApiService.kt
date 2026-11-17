package com.example.fumacanorte.data.remote.api

import com.example.fumacanorte.data.remote.dto.OpenMeteoResponseDto
import com.example.fumacanorte.domain.model.WeatherSnapshot
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApiService {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double = WeatherSnapshot.MANAUS_LATITUDE,
        @Query("longitude") longitude: Double = WeatherSnapshot.MANAUS_LONGITUDE,
        @Query("current") current: String = DEFAULT_CURRENT_PARAMS,
        @Query("hourly") hourly: String = DEFAULT_HOURLY_PARAMS,
        @Query("timezone") timezone: String = WeatherSnapshot.MANAUS_TIMEZONE,
        @Query("forecast_days") forecastDays: Int = DEFAULT_FORECAST_DAYS
    ): OpenMeteoResponseDto

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
        const val DEFAULT_CURRENT_PARAMS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m"
        const val DEFAULT_HOURLY_PARAMS =
            "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m"
        const val DEFAULT_FORECAST_DAYS = 2
    }
}
