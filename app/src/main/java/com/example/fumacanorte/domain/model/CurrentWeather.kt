package com.example.fumacanorte.domain.model

data class CurrentWeather(
    val temperatureCelsius: Double,
    val relativeHumidityPercent: Int,
    val apparentTemperatureCelsius: Double,
    val precipitationMm: Double,
    val windSpeedKmh: Double,
    val condition: WeatherCondition,
    val wmoCode: Int,
    val observedAtIso: String
)
