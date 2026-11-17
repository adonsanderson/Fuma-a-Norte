package com.example.fumacanorte.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_weather")
data class CurrentWeatherEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val temperatureCelsius: Double,
    val relativeHumidityPercent: Int,
    val apparentTemperatureCelsius: Double,
    val precipitationMm: Double,
    val windSpeedKmh: Double,
    val wmoCode: Int,
    val observedAtIso: String,
    val lastUpdatedEpochMillis: Long
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
