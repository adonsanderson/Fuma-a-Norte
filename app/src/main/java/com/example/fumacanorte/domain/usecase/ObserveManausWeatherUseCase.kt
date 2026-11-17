package com.example.fumacanorte.domain.usecase

import com.example.fumacanorte.domain.model.WeatherSnapshot
import com.example.fumacanorte.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class ObserveManausWeatherUseCase(
    private val repository: WeatherRepository
) {
    operator fun invoke(): Flow<WeatherSnapshot?> = repository.observeManausWeather()
}
