package com.example.fumacanorte.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hourly_forecast",
    foreignKeys = [
        ForeignKey(
            entity = CurrentWeatherEntity::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["snapshotId"])]
)
data class HourlyForecastEntity(
    @PrimaryKey val timeIso: String,
    val snapshotId: Int = CurrentWeatherEntity.SINGLETON_ID,
    val orderIndex: Int,
    val temperatureCelsius: Double,
    val relativeHumidityPercent: Int,
    val precipitationProbabilityPercent: Int,
    val precipitationMm: Double,
    val windSpeedKmh: Double,
    val wmoCode: Int
)
