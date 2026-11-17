package com.example.fumacanorte.domain.usecase

import app.cash.turbine.test
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.domain.model.WeatherCondition
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

class ObserveHourlyDetailUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var useCase: ObserveHourlyDetailUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = ObserveHourlyDetailUseCase(repository)
    }

    @Test
    fun `invoke emits hourly forecast for requested timestamp`() = runTest {
        val timeIso = "2026-11-17T16:00"
        val hourly = HourlyForecast(
            timeIso = timeIso,
            temperatureCelsius = 30.2,
            relativeHumidityPercent = 74,
            precipitationProbabilityPercent = 65,
            precipitationMm = 2.4,
            windSpeedKmh = 12.5,
            condition = WeatherCondition.RAIN_SHOWERS_MODERATE,
            wmoCode = 81
        )
        every { repository.observeHourlyForecast(timeIso) } returns flowOf(hourly)

        useCase(timeIso).test {
            assertEquals(hourly, awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { repository.observeHourlyForecast(timeIso) }
    }

    @Test
    fun `invoke emits null when hourly forecast is missing`() = runTest {
        val timeIso = "2026-11-17T23:00"
        every { repository.observeHourlyForecast(timeIso) } returns flowOf(null)

        useCase(timeIso).test {
            assertNull(awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { repository.observeHourlyForecast(timeIso) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws IllegalArgumentException when timeIso is blank`() {
        useCase("   ")
    }
}
