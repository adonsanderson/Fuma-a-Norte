package com.example.fumacanorte.domain.usecase

import app.cash.turbine.test
import com.example.fumacanorte.domain.model.CurrentWeather
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.domain.model.WeatherCondition
import com.example.fumacanorte.domain.model.WeatherSnapshot
import com.example.fumacanorte.domain.repository.WeatherRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ObserveManausWeatherUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var useCase: ObserveManausWeatherUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = ObserveManausWeatherUseCase(repository)
    }

    @Test
    fun `invoke emits weather snapshots streamed from repository`() = runTest {
        val snapshot = createSampleSnapshot()
        every { repository.observeManausWeather() } returns flowOf(null, snapshot)

        useCase().test {
            assertNull(awaitItem())
            assertEquals(snapshot, awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { repository.observeManausWeather() }
    }

    private fun createSampleSnapshot(): WeatherSnapshot = WeatherSnapshot(
        locationName = WeatherSnapshot.MANAUS_LOCATION_NAME,
        latitude = WeatherSnapshot.MANAUS_LATITUDE,
        longitude = WeatherSnapshot.MANAUS_LONGITUDE,
        timezone = WeatherSnapshot.MANAUS_TIMEZONE,
        current = CurrentWeather(
            temperatureCelsius = 31.4,
            relativeHumidityPercent = 68,
            apparentTemperatureCelsius = 35.8,
            precipitationMm = 0.0,
            windSpeedKmh = 9.2,
            condition = WeatherCondition.PARTLY_CLOUDY,
            wmoCode = 2,
            observedAtIso = "2026-11-17T15:00"
        ),
        hourlyForecasts = listOf(
            HourlyForecast(
                timeIso = "2026-11-17T15:00",
                temperatureCelsius = 31.4,
                relativeHumidityPercent = 68,
                precipitationProbabilityPercent = 20,
                precipitationMm = 0.0,
                windSpeedKmh = 9.2,
                condition = WeatherCondition.PARTLY_CLOUDY,
                wmoCode = 2
            )
        ),
        lastUpdatedEpochMillis = 1_763_420_400_000L
    )
}
