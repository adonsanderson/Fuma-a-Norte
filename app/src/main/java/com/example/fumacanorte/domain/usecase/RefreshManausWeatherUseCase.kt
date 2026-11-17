package com.example.fumacanorte.domain.usecase

import com.example.fumacanorte.domain.model.DomainResult
import com.example.fumacanorte.domain.repository.WeatherRepository

class RefreshManausWeatherUseCase(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(): DomainResult<Unit> = repository.refreshManausWeather()
}
