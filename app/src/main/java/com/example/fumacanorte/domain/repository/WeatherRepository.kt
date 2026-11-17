package com.example.fumacanorte.domain.repository

import com.example.fumacanorte.domain.model.DomainResult
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeManausWeather(): Flow<WeatherSnapshot?>
    fun observeHourlyForecast(timeIso: String): Flow<HourlyForecast?>
    suspend fun refreshManausWeather(): DomainResult<Unit>
}
