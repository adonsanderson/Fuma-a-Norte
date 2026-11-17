package com.example.fumacanorte.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface FumacaNorteRoute {
    @Serializable
    data object WeatherDashboard : FumacaNorteRoute

    @Serializable
    data class HourlyDetail(val timeIso: String) : FumacaNorteRoute
}
