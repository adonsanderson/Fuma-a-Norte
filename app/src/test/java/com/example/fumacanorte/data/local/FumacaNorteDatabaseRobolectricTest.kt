package com.example.fumacanorte.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.fumacanorte.data.local.db.FumacaNorteDatabase
import com.example.fumacanorte.data.remote.dto.CurrentWeatherDto
import com.example.fumacanorte.data.remote.dto.HourlyForecastDto
import com.example.fumacanorte.data.remote.dto.OpenMeteoResponseDto
import com.example.fumacanorte.data.remote.source.WeatherRemoteDataSource
import com.example.fumacanorte.data.repository.WeatherRepositoryImpl
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.DomainResult
import com.example.fumacanorte.domain.model.WeatherCondition
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FumacaNorteDatabaseRobolectricTest {

    private lateinit var database: FumacaNorteDatabase
    private lateinit var remoteDataSource: WeatherRemoteDataSource
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FumacaNorteDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remoteDataSource = mockk()
        repository = WeatherRepositoryImpl(
            weatherDao = database.weatherDao(),
            remoteDataSource = remoteDataSource,
            ioDispatcher = Dispatchers.Unconfined,
            currentTimeMillisProvider = { 1_763_420_400_000L }
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `offline-first integration persists Room snapshot and preserves cache on network failure`() = runTest {
        val initialDto = createDto(
            currentTemp = 32.5,
            hours = listOf("2026-11-17T15:00", "2026-11-17T16:00"),
            temps = listOf(32.5, 31.2),
            codes = listOf(2, 80)
        )
        coEvery { remoteDataSource.fetchManausForecast() } returns DomainResult.Success(initialDto)

        repository.observeManausWeather().test {
            assertNull(awaitItem())

            val firstRefresh = repository.refreshManausWeather()
            assertEquals(DomainResult.Success(Unit), firstRefresh)

            val persisted = awaitItem()
            assertNotNull(persisted)
            requireNotNull(persisted)
            assertEquals(32.5, persisted.current.temperatureCelsius, 0.001)
            assertEquals(2, persisted.hourlyForecasts.size)
            assertEquals("2026-11-17T15:00", persisted.hourlyForecasts[0].timeIso)
            assertEquals(WeatherCondition.RAIN_SHOWERS_SLIGHT, persisted.hourlyForecasts[1].condition)

            // Simulate offline failure: cache in Room must remain intact
            coEvery { remoteDataSource.fetchManausForecast() } returns DomainResult.Failure(DomainError.NoInternet)
            val failedRefresh = repository.refreshManausWeather()
            assertEquals(DomainResult.Failure(DomainError.NoInternet), failedRefresh)

            // Subsequent successful sync replaces hourly items atomically
            val updatedDto = createDto(
                currentTemp = 29.0,
                hours = listOf("2026-11-17T17:00"),
                temps = listOf(29.0),
                codes = listOf(95)
            )
            coEvery { remoteDataSource.fetchManausForecast() } returns DomainResult.Success(updatedDto)
            assertEquals(DomainResult.Success(Unit), repository.refreshManausWeather())

            val updated = awaitItem()
            assertNotNull(updated)
            requireNotNull(updated)
            assertEquals(29.0, updated.current.temperatureCelsius, 0.001)
            assertEquals(1, updated.hourlyForecasts.size)
            assertEquals("2026-11-17T17:00", updated.hourlyForecasts[0].timeIso)
            assertEquals(WeatherCondition.THUNDERSTORM, updated.hourlyForecasts[0].condition)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeHourlyForecast reads specific persisted hour from Room`() = runTest {
        val dto = createDto(
            currentTemp = 31.0,
            hours = listOf("2026-11-17T15:00", "2026-11-17T16:00"),
            temps = listOf(31.0, 29.4),
            codes = listOf(1, 63)
        )
        coEvery { remoteDataSource.fetchManausForecast() } returns DomainResult.Success(dto)
        repository.refreshManausWeather()

        repository.observeHourlyForecast("2026-11-17T16:00").test {
            val hour = awaitItem()
            assertNotNull(hour)
            requireNotNull(hour)
            assertEquals(29.4, hour.temperatureCelsius, 0.001)
            assertEquals(WeatherCondition.RAIN_MODERATE, hour.condition)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createDto(
        currentTemp: Double,
        hours: List<String>,
        temps: List<Double>,
        codes: List<Int>
    ): OpenMeteoResponseDto = OpenMeteoResponseDto(
        latitude = -3.10,
        longitude = -60.02,
        timezone = "America/Manaus",
        current = CurrentWeatherDto(
            time = hours.first(),
            temperature2m = currentTemp,
            relativeHumidity2m = 72,
            apparentTemperature = currentTemp + 3.5,
            precipitation = 0.0,
            weatherCode = codes.first(),
            windSpeed10m = 8.4
        ),
        hourly = HourlyForecastDto(
            time = hours,
            temperature2m = temps,
            relativeHumidity2m = hours.map { 74 },
            precipitationProbability = hours.map { 45 },
            precipitation = hours.map { 0.8 },
            weatherCode = codes,
            windSpeed10m = hours.map { 9.5 }
        )
    )
}
