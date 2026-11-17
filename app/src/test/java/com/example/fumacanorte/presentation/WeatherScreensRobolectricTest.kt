package com.example.fumacanorte.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.fumacanorte.domain.model.CurrentWeather
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.domain.model.WeatherCondition
import com.example.fumacanorte.domain.model.WeatherSnapshot
import com.example.fumacanorte.presentation.detail.HourlyDetailScreen
import com.example.fumacanorte.presentation.detail.HourlyDetailUiState
import com.example.fumacanorte.presentation.theme.FumacaNorteTheme
import com.example.fumacanorte.presentation.weather.WeatherDashboardScreen
import com.example.fumacanorte.presentation.weather.WeatherUiEvent
import com.example.fumacanorte.presentation.weather.WeatherUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WeatherScreensRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `dashboard renders Loading state`() {
        composeTestRule.setContent {
            FumacaNorteTheme {
                WeatherDashboardScreen(
                    uiState = WeatherUiState.Loading,
                    onEvent = {},
                    onHourlyClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("weather_loading_view").assertIsDisplayed()
    }

    @Test
    fun `dashboard renders Empty state and triggers Retry on click`() {
        val events = mutableListOf<WeatherUiEvent>()
        composeTestRule.setContent {
            FumacaNorteTheme {
                WeatherDashboardScreen(
                    uiState = WeatherUiState.Empty,
                    onEvent = { events.add(it) },
                    onHourlyClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("weather_empty_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("retry_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(WeatherUiEvent.Retry), events)
    }

    @Test
    fun `dashboard renders Error state and triggers Retry on click`() {
        val events = mutableListOf<WeatherUiEvent>()
        composeTestRule.setContent {
            FumacaNorteTheme {
                WeatherDashboardScreen(
                    uiState = WeatherUiState.Error(DomainError.NoInternet),
                    onEvent = { events.add(it) },
                    onHourlyClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("weather_error_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("retry_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(WeatherUiEvent.Retry), events)
    }

    @Test
    fun `dashboard renders Success with offline banner and navigates on hourly click`() {
        val events = mutableListOf<WeatherUiEvent>()
        var clickedHour: String? = null
        val snapshot = createSampleSnapshot()

        composeTestRule.setContent {
            FumacaNorteTheme {
                WeatherDashboardScreen(
                    uiState = WeatherUiState.Success(
                        snapshot = snapshot,
                        isRefreshing = false,
                        syncError = DomainError.NoInternet
                    ),
                    onEvent = { events.add(it) },
                    onHourlyClick = { clickedHour = it }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("offline_sync_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("current_weather_hero_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("weather_dashboard_list")
            .performScrollToNode(hasTestTag("hourly_item_2026-11-17T15:00"))
        composeTestRule.onNodeWithTag("hourly_item_2026-11-17T15:00").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hourly_item_2026-11-17T15:00").performClick()

        composeTestRule.onNodeWithTag("weather_dashboard_list")
            .performScrollToNode(hasTestTag("offline_sync_banner"))
        composeTestRule.onNodeWithTag("dismiss_sync_error_button").performClick()
        composeTestRule.waitForIdle()

        assertEquals("2026-11-17T15:00", clickedHour)
        assertEquals(listOf(WeatherUiEvent.DismissSyncError), events)
    }

    @Test
    fun `hourly detail renders Success metrics and back button navigates back`() {
        var navigatedBack = false
        val forecast = HourlyForecast(
            timeIso = "2026-11-17T15:00",
            temperatureCelsius = 31.8,
            relativeHumidityPercent = 68,
            precipitationProbabilityPercent = 35,
            precipitationMm = 0.4,
            windSpeedKmh = 9.2,
            condition = WeatherCondition.PARTLY_CLOUDY,
            wmoCode = 2
        )

        composeTestRule.setContent {
            FumacaNorteTheme {
                HourlyDetailScreen(
                    uiState = HourlyDetailUiState.Success(forecast),
                    onNavigateBack = { navigatedBack = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hourly_detail_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("detail_back_button").performClick()
        composeTestRule.waitForIdle()

        assertTrue(navigatedBack)
    }

    private fun createSampleSnapshot(): WeatherSnapshot = WeatherSnapshot(
        locationName = WeatherSnapshot.MANAUS_LOCATION_NAME,
        latitude = WeatherSnapshot.MANAUS_LATITUDE,
        longitude = WeatherSnapshot.MANAUS_LONGITUDE,
        timezone = WeatherSnapshot.MANAUS_TIMEZONE,
        current = CurrentWeather(
            temperatureCelsius = 32.0,
            relativeHumidityPercent = 68,
            apparentTemperatureCelsius = 36.2,
            precipitationMm = 0.0,
            windSpeedKmh = 9.2,
            condition = WeatherCondition.PARTLY_CLOUDY,
            wmoCode = 2,
            observedAtIso = "2026-11-17T15:00"
        ),
        hourlyForecasts = listOf(
            HourlyForecast(
                timeIso = "2026-11-17T15:00",
                temperatureCelsius = 32.0,
                relativeHumidityPercent = 68,
                precipitationProbabilityPercent = 25,
                precipitationMm = 0.0,
                windSpeedKmh = 9.2,
                condition = WeatherCondition.PARTLY_CLOUDY,
                wmoCode = 2
            )
        ),
        lastUpdatedEpochMillis = 1_763_420_400_000L
    )
}
