package com.example.fumacanorte.domain.model

data class HourlyForecast(
    val timeIso: String,
    val temperatureCelsius: Double,
    val relativeHumidityPercent: Int,
    val precipitationProbabilityPercent: Int,
    val precipitationMm: Double,
    val windSpeedKmh: Double,
    val condition: WeatherCondition,
    val wmoCode: Int
)
