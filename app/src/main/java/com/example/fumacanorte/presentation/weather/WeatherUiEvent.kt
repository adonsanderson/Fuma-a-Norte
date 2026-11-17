package com.example.fumacanorte.presentation.weather

sealed interface WeatherUiEvent {
    data object Refresh : WeatherUiEvent
    data object Retry : WeatherUiEvent
    data object DismissSyncError : WeatherUiEvent
}
