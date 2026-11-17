package com.example.fumacanorte.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class WeatherWithHourlyRelation(
    @Embedded val current: CurrentWeatherEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "snapshotId"
    )
    val hourlyForecasts: List<HourlyForecastEntity>
)
