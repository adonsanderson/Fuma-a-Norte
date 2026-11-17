package com.example.fumacanorte.presentation.detail

import androidx.compose.runtime.Immutable
import com.example.fumacanorte.domain.model.HourlyForecast

@Immutable
sealed interface HourlyDetailUiState {
    data object Loading : HourlyDetailUiState
    data class Success(val forecast: HourlyForecast) : HourlyDetailUiState
    data object NotFound : HourlyDetailUiState
}
