package com.example.fumacanorte.data.mapper

import com.example.fumacanorte.data.local.entity.CurrentWeatherEntity
import com.example.fumacanorte.data.local.entity.HourlyForecastEntity
import com.example.fumacanorte.data.local.entity.WeatherWithHourlyRelation
import com.example.fumacanorte.data.remote.dto.OpenMeteoResponseDto
import com.example.fumacanorte.domain.model.CurrentWeather
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.domain.model.WeatherCondition
import com.example.fumacanorte.domain.model.WeatherSnapshot

data class WeatherEntitiesBundle(
    val current: CurrentWeatherEntity,
    val hourlyForecasts: List<HourlyForecastEntity>
)

fun OpenMeteoResponseDto.toEntities(
    lastUpdatedEpochMillis: Long
): WeatherEntitiesBundle? {
    val currentDto = current ?: return null
    val hourlyDto = hourly ?: return null

    val itemCount = minOf(
        hourlyDto.time.size,
        hourlyDto.temperature2m.size,
        hourlyDto.relativeHumidity2m.size,
        hourlyDto.precipitationProbability.size,
        hourlyDto.precipitation.size,
        hourlyDto.weatherCode.size,
        hourlyDto.windSpeed10m.size
    )
    if (itemCount <= 0) return null

    val currentEntity = CurrentWeatherEntity(
        id = CurrentWeatherEntity.SINGLETON_ID,
        locationName = WeatherSnapshot.MANAUS_LOCATION_NAME,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone.ifBlank { WeatherSnapshot.MANAUS_TIMEZONE },
        temperatureCelsius = currentDto.temperature2m,
        relativeHumidityPercent = currentDto.relativeHumidity2m,
        apparentTemperatureCelsius = currentDto.apparentTemperature,
        precipitationMm = currentDto.precipitation,
        windSpeedKmh = currentDto.windSpeed10m,
        wmoCode = currentDto.weatherCode,
        observedAtIso = currentDto.time,
        lastUpdatedEpochMillis = lastUpdatedEpochMillis
    )

    val hourlyEntities = (0 until itemCount)
        .map { index ->
            HourlyForecastEntity(
                timeIso = hourlyDto.time[index],
                snapshotId = CurrentWeatherEntity.SINGLETON_ID,
                orderIndex = index,
                temperatureCelsius = hourlyDto.temperature2m[index],
                relativeHumidityPercent = hourlyDto.relativeHumidity2m[index],
                precipitationProbabilityPercent = hourlyDto.precipitationProbability[index],
                precipitationMm = hourlyDto.precipitation[index],
                windSpeedKmh = hourlyDto.windSpeed10m[index],
                wmoCode = hourlyDto.weatherCode[index]
            )
        }
        .distinctBy { it.timeIso }

    return WeatherEntitiesBundle(
        current = currentEntity,
        hourlyForecasts = hourlyEntities
    )
}

fun WeatherWithHourlyRelation.toDomain(): WeatherSnapshot = WeatherSnapshot(
    locationName = current.locationName,
    latitude = current.latitude,
    longitude = current.longitude,
    timezone = current.timezone,
    current = current.toDomain(),
    hourlyForecasts = hourlyForecasts
        .sortedBy { it.orderIndex }
        .map { it.toDomain() },
    lastUpdatedEpochMillis = current.lastUpdatedEpochMillis
)

fun CurrentWeatherEntity.toDomain(): CurrentWeather = CurrentWeather(
    temperatureCelsius = temperatureCelsius,
    relativeHumidityPercent = relativeHumidityPercent,
    apparentTemperatureCelsius = apparentTemperatureCelsius,
    precipitationMm = precipitationMm,
    windSpeedKmh = windSpeedKmh,
    condition = WeatherCondition.fromWmoCode(wmoCode),
    wmoCode = wmoCode,
    observedAtIso = observedAtIso
)

fun HourlyForecastEntity.toDomain(): HourlyForecast = HourlyForecast(
    timeIso = timeIso,
    temperatureCelsius = temperatureCelsius,
    relativeHumidityPercent = relativeHumidityPercent,
    precipitationProbabilityPercent = precipitationProbabilityPercent,
    precipitationMm = precipitationMm,
    windSpeedKmh = windSpeedKmh,
    condition = WeatherCondition.fromWmoCode(wmoCode),
    wmoCode = wmoCode
)
