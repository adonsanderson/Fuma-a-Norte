package com.example.fumacanorte.domain.usecase

import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class ObserveHourlyDetailUseCase(
    private val repository: WeatherRepository
) {
    operator fun invoke(timeIso: String): Flow<HourlyForecast?> {
        require(timeIso.isNotBlank()) { "timeIso must not be blank" }
        return repository.observeHourlyForecast(timeIso)
    }
}
