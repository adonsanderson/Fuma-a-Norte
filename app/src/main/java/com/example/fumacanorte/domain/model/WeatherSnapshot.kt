package com.example.fumacanorte.domain.model

data class WeatherSnapshot(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourlyForecasts: List<HourlyForecast>,
    val lastUpdatedEpochMillis: Long
) {
    companion object {
        const val MANAUS_LOCATION_NAME = "Manaus, AM"
        const val MANAUS_LATITUDE = -3.10
        const val MANAUS_LONGITUDE = -60.02
        const val MANAUS_TIMEZONE = "America/Manaus"
    }
}
