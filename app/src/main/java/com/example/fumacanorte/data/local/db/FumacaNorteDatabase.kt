package com.example.fumacanorte.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fumacanorte.data.local.dao.WeatherDao
import com.example.fumacanorte.data.local.entity.CurrentWeatherEntity
import com.example.fumacanorte.data.local.entity.HourlyForecastEntity

@Database(
    entities = [
        CurrentWeatherEntity::class,
        HourlyForecastEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FumacaNorteDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        const val DATABASE_NAME = "fumaca_norte.db"
    }
}
