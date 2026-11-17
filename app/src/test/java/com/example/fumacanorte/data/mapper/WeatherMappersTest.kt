package com.example.fumacanorte.data.mapper

import com.example.fumacanorte.data.local.entity.CurrentWeatherEntity
import com.example.fumacanorte.data.local.entity.HourlyForecastEntity
import com.example.fumacanorte.data.local.entity.WeatherWithHourlyRelation
import com.example.fumacanorte.data.remote.dto.CurrentWeatherDto
import com.example.fumacanorte.data.remote.dto.HourlyForecastDto
import com.example.fumacanorte.data.remote.dto.OpenMeteoResponseDto
import com.example.fumacanorte.domain.model.WeatherCondition
import com.example.fumacanorte.domain.model.WeatherSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherMappersTest {

    @Test
    fun `toEntities maps complete OpenMeteoResponseDto into Current and Hourly entities`() {
        val dto = OpenMeteoResponseDto(
            latitude = -3.10,
            longitude = -60.02,
            timezone = "America/Manaus",
            current = CurrentWeatherDto(
                time = "2026-11-17T15:00",
                temperature2m = 32.1,
                relativeHumidity2m = 66,
                apparentTemperature = 36.4,
                precipitation = 0.2,
                weatherCode = 2,
                windSpeed10m = 8.5
            ),
            hourly = HourlyForecastDto(
                time = listOf("2026-11-17T15:00", "2026-11-17T16:00"),
                temperature2m = listOf(32.1, 30.8),
                relativeHumidity2m = listOf(66, 72),
                precipitationProbability = listOf(30, 60),
                precipitation = listOf(0.2, 1.5),
                weatherCode = listOf(2, 80),
                windSpeed10m = listOf(8.5, 11.0)
            )
        )

        val bundle = dto.toEntities(lastUpdatedEpochMillis = 1_763_420_400_000L)

        assertNotNull(bundle)
        requireNotNull(bundle)
        assertEquals(CurrentWeatherEntity.SINGLETON_ID, bundle.current.id)
        assertEquals(WeatherSnapshot.MANAUS_LOCATION_NAME, bundle.current.locationName)
        assertEquals(32.1, bundle.current.temperatureCelsius, 0.001)
        assertEquals(1_763_420_400_000L, bundle.current.lastUpdatedEpochMillis)

        assertEquals(2, bundle.hourlyForecasts.size)
        assertEquals("2026-11-17T15:00", bundle.hourlyForecasts[0].timeIso)
        assertEquals(0, bundle.hourlyForecasts[0].orderIndex)
        assertEquals("2026-11-17T16:00", bundle.hourlyForecasts[1].timeIso)
        assertEquals(1, bundle.hourlyForecasts[1].orderIndex)
        assertEquals(60, bundle.hourlyForecasts[1].precipitationProbabilityPercent)
    }

    @Test
    fun `toEntities returns null when current or hourly is missing or empty`() {
        val missingCurrent = OpenMeteoResponseDto(
            latitude = -3.10,
            longitude = -60.02,
            timezone = "America/Manaus",
            current = null,
            hourly = null
        )
        assertNull(missingCurrent.toEntities(1000L))

        val emptyHourly = OpenMeteoResponseDto(
            latitude = -3.10,
            longitude = -60.02,
            timezone = "America/Manaus",
            current = CurrentWeatherDto(
                time = "2026-11-17T15:00",
                temperature2m = 30.0,
                relativeHumidity2m = 70,
                apparentTemperature = 33.0,
                precipitation = 0.0,
                weatherCode = 0,
                windSpeed10m = 5.0
            ),
            hourly = HourlyForecastDto(
                time = emptyList(),
                temperature2m = emptyList(),
                relativeHumidity2m = emptyList(),
                precipitationProbability = emptyList(),
                precipitation = emptyList(),
                weatherCode = emptyList(),
                windSpeed10m = emptyList()
            )
        )
        assertNull(emptyHourly.toEntities(1000L))
    }

    @Test
    fun `toDomain sorts hourly forecasts by orderIndex and resolves WeatherCondition`() {
        val relation = WeatherWithHourlyRelation(
            current = CurrentWeatherEntity(
                id = CurrentWeatherEntity.SINGLETON_ID,
                locationName = "Manaus, AM",
                latitude = -3.10,
                longitude = -60.02,
                timezone = "America/Manaus",
                temperatureCelsius = 31.0,
                relativeHumidityPercent = 70,
                apparentTemperatureCelsius = 35.0,
                precipitationMm = 0.0,
                windSpeedKmh = 10.0,
                wmoCode = 95,
                observedAtIso = "2026-11-17T15:00",
                lastUpdatedEpochMillis = 9_999L
            ),
            hourlyForecasts = listOf(
                HourlyForecastEntity(
                    timeIso = "2026-11-17T16:00",
                    orderIndex = 1,
                    temperatureCelsius = 29.0,
                    relativeHumidityPercent = 80,
                    precipitationProbabilityPercent = 85,
                    precipitationMm = 4.2,
                    windSpeedKmh = 15.0,
                    wmoCode = 81
                ),
                HourlyForecastEntity(
                    timeIso = "2026-11-17T15:00",
                    orderIndex = 0,
                    temperatureCelsius = 31.0,
                    relativeHumidityPercent = 70,
                    precipitationProbabilityPercent = 40,
                    precipitationMm = 0.0,
                    windSpeedKmh = 10.0,
                    wmoCode = 2
                )
            )
        )

        val snapshot = relation.toDomain()

        assertEquals(WeatherCondition.THUNDERSTORM, snapshot.current.condition)
        assertEquals(2, snapshot.hourlyForecasts.size)
        assertEquals("2026-11-17T15:00", snapshot.hourlyForecasts[0].timeIso)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, snapshot.hourlyForecasts[0].condition)
        assertEquals("2026-11-17T16:00", snapshot.hourlyForecasts[1].timeIso)
        assertEquals(WeatherCondition.RAIN_SHOWERS_MODERATE, snapshot.hourlyForecasts[1].condition)
    }
}
