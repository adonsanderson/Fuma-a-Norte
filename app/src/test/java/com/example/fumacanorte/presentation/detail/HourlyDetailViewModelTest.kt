package com.example.fumacanorte.presentation.detail

import app.cash.turbine.test
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.domain.model.WeatherCondition
import com.example.fumacanorte.domain.usecase.ObserveHourlyDetailUseCase
import com.example.fumacanorte.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HourlyDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val observeHourlyDetailUseCase: ObserveHourlyDetailUseCase = mockk()

    @Test
    fun `emits Loading then Success when hourly forecast exists`() = runTest(testDispatcher) {
        val timeIso = "2026-11-17T16:00"
        val forecast = HourlyForecast(
            timeIso = timeIso,
            temperatureCelsius = 29.8,
            relativeHumidityPercent = 76,
            precipitationProbabilityPercent = 70,
            precipitationMm = 3.5,
            windSpeedKmh = 14.0,
            condition = WeatherCondition.RAIN_SHOWERS_MODERATE,
            wmoCode = 81
        )
        every { observeHourlyDetailUseCase(timeIso) } returns flowOf(forecast)

        val viewModel = HourlyDetailViewModel(timeIso, observeHourlyDetailUseCase)

        viewModel.uiState.test {
            assertEquals(HourlyDetailUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(HourlyDetailUiState.Success(forecast), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then NotFound when hourly forecast is missing`() = runTest(testDispatcher) {
        val timeIso = "2026-11-17T23:00"
        every { observeHourlyDetailUseCase(timeIso) } returns flowOf(null)

        val viewModel = HourlyDetailViewModel(timeIso, observeHourlyDetailUseCase)

        viewModel.uiState.test {
            assertEquals(HourlyDetailUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(HourlyDetailUiState.NotFound, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
