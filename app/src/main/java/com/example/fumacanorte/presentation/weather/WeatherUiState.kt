package com.example.fumacanorte.presentation.weather

import androidx.compose.runtime.Immutable
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.WeatherSnapshot

@Immutable
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data object Empty : WeatherUiState
    data class Error(val error: DomainError) : WeatherUiState
    data class Success(
        val snapshot: WeatherSnapshot,
        val isRefreshing: Boolean = false,
        val syncError: DomainError? = null
    ) : WeatherUiState
}
