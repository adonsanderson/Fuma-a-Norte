package com.example.fumacanorte.presentation.weather

import app.cash.turbine.test
import com.example.fumacanorte.domain.model.CurrentWeather
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.DomainResult
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.domain.model.WeatherCondition
import com.example.fumacanorte.domain.model.WeatherSnapshot
import com.example.fumacanorte.domain.usecase.ObserveManausWeatherUseCase
import com.example.fumacanorte.domain.usecase.RefreshManausWeatherUseCase
import com.example.fumacanorte.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val observeManausWeatherUseCase: ObserveManausWeatherUseCase = mockk()
    private val refreshManausWeatherUseCase: RefreshManausWeatherUseCase = mockk()

    @Test
    fun `emits Loading then Success when cache updates after initial refresh`() = runTest(testDispatcher) {
        val dbFlow = MutableStateFlow<WeatherSnapshot?>(null)
        val snapshot = createSampleSnapshot()
        every { observeManausWeatherUseCase() } returns dbFlow
        coEvery { refreshManausWeatherUseCase() } coAnswers {
            dbFlow.value = snapshot
            DomainResult.Success(Unit)
        }

        val viewModel = WeatherViewModel(observeManausWeatherUseCase, refreshManausWeatherUseCase)

        viewModel.uiState.test {
            assertEquals(WeatherUiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem() as WeatherUiState.Success
            assertEquals(snapshot, success.snapshot)
            assertFalse(success.isRefreshing)
            assertNull(success.syncError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Error when cache is empty and initial refresh fails`() = runTest(testDispatcher) {
        val dbFlow = MutableStateFlow<WeatherSnapshot?>(null)
        every { observeManausWeatherUseCase() } returns dbFlow
        coEvery { refreshManausWeatherUseCase() } returns DomainResult.Failure(DomainError.NoInternet)

        val viewModel = WeatherViewModel(observeManausWeatherUseCase, refreshManausWeatherUseCase)

        viewModel.uiState.test {
            assertEquals(WeatherUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(WeatherUiState.Error(DomainError.NoInternet), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Empty when cache remains null after refresh succeeds`() = runTest(testDispatcher) {
        val dbFlow = MutableStateFlow<WeatherSnapshot?>(null)
        every { observeManausWeatherUseCase() } returns dbFlow
        coEvery { refreshManausWeatherUseCase() } returns DomainResult.Success(Unit)

        val viewModel = WeatherViewModel(observeManausWeatherUseCase, refreshManausWeatherUseCase)

        viewModel.uiState.test {
            assertEquals(WeatherUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(WeatherUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `preserves Success state and exposes syncError when offline with cached data`() = runTest(testDispatcher) {
        val snapshot = createSampleSnapshot()
        val dbFlow = MutableStateFlow<WeatherSnapshot?>(snapshot)
        val refreshGate = CompletableDeferred<DomainResult<Unit>>()
        every { observeManausWeatherUseCase() } returns dbFlow
        coEvery { refreshManausWeatherUseCase() } coAnswers { refreshGate.await() }

        val viewModel = WeatherViewModel(observeManausWeatherUseCase, refreshManausWeatherUseCase)

        viewModel.uiState.test {
            assertEquals(WeatherUiState.Loading, awaitItem())

            testScheduler.runCurrent()
            val refreshingState = awaitItem() as WeatherUiState.Success
            assertEquals(snapshot, refreshingState.snapshot)
            assertTrue(refreshingState.isRefreshing)
            assertNull(refreshingState.syncError)

            refreshGate.complete(DomainResult.Failure(DomainError.NoInternet))
            advanceUntilIdle()

            val offlineState = awaitItem() as WeatherUiState.Success
            assertEquals(snapshot, offlineState.snapshot)
            assertFalse(offlineState.isRefreshing)
            assertEquals(DomainError.NoInternet, offlineState.syncError)

            viewModel.onEvent(WeatherUiEvent.DismissSyncError)
            advanceUntilIdle()

            val dismissedState = awaitItem() as WeatherUiState.Success
            assertNull(dismissedState.syncError)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `manual Refresh and Retry events invoke RefreshManausWeatherUseCase`() = runTest(testDispatcher) {
        val dbFlow = MutableStateFlow<WeatherSnapshot?>(createSampleSnapshot())
        every { observeManausWeatherUseCase() } returns dbFlow
        coEvery { refreshManausWeatherUseCase() } returns DomainResult.Success(Unit)

        val viewModel = WeatherViewModel(observeManausWeatherUseCase, refreshManausWeatherUseCase)
        advanceUntilIdle()

        viewModel.onEvent(WeatherUiEvent.Refresh)
        advanceUntilIdle()

        viewModel.onEvent(WeatherUiEvent.Retry)
        advanceUntilIdle()

        coVerify(exactly = 3) { refreshManausWeatherUseCase() }
    }

    private fun createSampleSnapshot(): WeatherSnapshot = WeatherSnapshot(
        locationName = WeatherSnapshot.MANAUS_LOCATION_NAME,
        latitude = WeatherSnapshot.MANAUS_LATITUDE,
        longitude = WeatherSnapshot.MANAUS_LONGITUDE,
        timezone = WeatherSnapshot.MANAUS_TIMEZONE,
        current = CurrentWeather(
            temperatureCelsius = 32.0,
            relativeHumidityPercent = 66,
            apparentTemperatureCelsius = 36.5,
            precipitationMm = 0.0,
            windSpeedKmh = 9.0,
            condition = WeatherCondition.PARTLY_CLOUDY,
            wmoCode = 2,
            observedAtIso = "2026-11-17T15:00"
        ),
        hourlyForecasts = listOf(
            HourlyForecast(
                timeIso = "2026-11-17T15:00",
                temperatureCelsius = 32.0,
                relativeHumidityPercent = 66,
                precipitationProbabilityPercent = 20,
                precipitationMm = 0.0,
                windSpeedKmh = 9.0,
                condition = WeatherCondition.PARTLY_CLOUDY,
                wmoCode = 2
            )
        ),
        lastUpdatedEpochMillis = 1_763_420_400_000L
    )
}
