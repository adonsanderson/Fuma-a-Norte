package com.example.fumacanorte.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.fumacanorte.data.local.entity.CurrentWeatherEntity
import com.example.fumacanorte.data.local.entity.HourlyForecastEntity
import com.example.fumacanorte.data.local.entity.WeatherWithHourlyRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Transaction
    @Query("SELECT * FROM current_weather WHERE id = :snapshotId LIMIT 1")
    fun observeWeatherSnapshot(
        snapshotId: Int = CurrentWeatherEntity.SINGLETON_ID
    ): Flow<WeatherWithHourlyRelation?>

    @Query("SELECT * FROM hourly_forecast WHERE timeIso = :timeIso LIMIT 1")
    fun observeHourlyForecast(timeIso: String): Flow<HourlyForecastEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCurrentWeather(current: CurrentWeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecasts(forecasts: List<HourlyForecastEntity>)

    @Query("DELETE FROM hourly_forecast WHERE snapshotId = :snapshotId")
    suspend fun deleteHourlyForecastsForSnapshot(
        snapshotId: Int = CurrentWeatherEntity.SINGLETON_ID
    )

    @Transaction
    suspend fun replaceWeatherData(
        current: CurrentWeatherEntity,
        forecasts: List<HourlyForecastEntity>
    ) {
        upsertCurrentWeather(current)
        deleteHourlyForecastsForSnapshot(current.id)
        insertHourlyForecasts(forecasts)
    }
}
