package com.example.fumacanorte.data.repository

import app.cash.turbine.test
import com.example.fumacanorte.data.local.dao.WeatherDao
import com.example.fumacanorte.data.local.entity.CurrentWeatherEntity
import com.example.fumacanorte.data.local.entity.HourlyForecastEntity
import com.example.fumacanorte.data.local.entity.WeatherWithHourlyRelation
import com.example.fumacanorte.data.remote.dto.CurrentWeatherDto
import com.example.fumacanorte.data.remote.dto.HourlyForecastDto
import com.example.fumacanorte.data.remote.dto.OpenMeteoResponseDto
import com.example.fumacanorte.data.remote.source.WeatherRemoteDataSource
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.DomainResult
import com.example.fumacanorte.domain.model.WeatherCondition
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryImplTest {

    private lateinit var weatherDao: WeatherDao
    private lateinit var remoteDataSource: WeatherRemoteDataSource
    private val testDispatcher = StandardTestDispatcher()
    private val fixedNowMillis = 1_763_420_400_000L

    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setUp() {
        weatherDao = mockk(relaxed = true)
        remoteDataSource = mockk()
        repository = WeatherRepositoryImpl(
            weatherDao = weatherDao,
            remoteDataSource = remoteDataSource,
            ioDispatcher = testDispatcher,
            currentTimeMillisProvider = { fixedNowMillis }
        )
    }

    @Test
    fun `observeManausWeather emits mapped domain snapshot from Room Flow`() = runTest(testDispatcher) {
        val dbFlow = MutableStateFlow<WeatherWithHourlyRelation?>(null)
        every { weatherDao.observeWeatherSnapshot(any()) } returns dbFlow

        repository.observeManausWeather().test {
            assertNull(awaitItem())

            dbFlow.value = createSampleRelation()
            val snapshot = awaitItem()
            assertNotNull(snapshot)
            requireNotNull(snapshot)
            assertEquals("Manaus, AM", snapshot.locationName)
            assertEquals(31.5, snapshot.current.temperatureCelsius, 0.001)
            assertEquals(1, snapshot.hourlyForecasts.size)
            assertEquals(WeatherCondition.PARTLY_CLOUDY, snapshot.hourlyForecasts.first().condition)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeHourlyForecast emits mapped HourlyForecast from Room Flow`() = runTest(testDispatcher) {
        val timeIso = "2026-11-17T16:00"
        val entity = HourlyForecastEntity(
            timeIso = timeIso,
            orderIndex = 0,
            temperatureCelsius = 29.5,
            relativeHumidityPercent = 78,
            precipitationProbabilityPercent = 70,
            precipitationMm = 3.1,
            windSpeedKmh = 12.0,
            wmoCode = 63
        )
        every { weatherDao.observeHourlyForecast(timeIso) } returns flowOf(entity)

        repository.observeHourlyForecast(timeIso).test {
            val item = awaitItem()
            assertNotNull(item)
            requireNotNull(item)
            assertEquals(timeIso, item.timeIso)
            assertEquals(WeatherCondition.RAIN_MODERATE, item.condition)
            assertEquals(70, item.precipitationProbabilityPercent)
            awaitComplete()
        }
    }

    @Test
    fun `refreshManausWeather persists data transactionally when remote succeeds`() = runTest(testDispatcher) {
        val dto = createSampleResponseDto()
        coEvery { remoteDataSource.fetchManausForecast() } returns DomainResult.Success(dto)

        val result = repository.refreshManausWeather()

        assertEquals(DomainResult.Success(Unit), result)
        coVerify(exactly = 1) {
            weatherDao.replaceWeatherData(
                current = match {
                    it.id == CurrentWeatherEntity.SINGLETON_ID &&
                        it.temperatureCelsius == 32.0 &&
                        it.lastUpdatedEpochMillis == fixedNowMillis
                },
                forecasts = match { it.size == 2 && it[0].timeIso == "2026-11-17T15:00" }
            )
        }
    }

    @Test
    fun `refreshManausWeather preserves existing cache and returns Failure when offline`() = runTest(testDispatcher) {
        coEvery { remoteDataSource.fetchManausForecast() } returns DomainResult.Failure(DomainError.NoInternet)

        val result = repository.refreshManausWeather()

        assertEquals(DomainResult.Failure(DomainError.NoInternet), result)
        coVerify(exactly = 0) { weatherDao.replaceWeatherData(any(), any()) }
        coVerify(exactly = 0) { weatherDao.deleteHourlyForecastsForSnapshot(any()) }
    }

    @Test
    fun `refreshManausWeather returns DataParsingError and does not touch Room when payload is incomplete`() = runTest(testDispatcher) {
        val malformedDto = OpenMeteoResponseDto(
            latitude = -3.10,
            longitude = -60.02,
            timezone = "America/Manaus",
            current = null,
            hourly = null
        )
        coEvery { remoteDataSource.fetchManausForecast() } returns DomainResult.Success(malformedDto)

        val result = repository.refreshManausWeather()

        assertEquals(DomainResult.Failure(DomainError.DataParsingError), result)
        coVerify(exactly = 0) { weatherDao.replaceWeatherData(any(), any()) }
    }

    private fun createSampleRelation(): WeatherWithHourlyRelation = WeatherWithHourlyRelation(
        current = CurrentWeatherEntity(
            id = CurrentWeatherEntity.SINGLETON_ID,
            locationName = "Manaus, AM",
            latitude = -3.10,
            longitude = -60.02,
            timezone = "America/Manaus",
            temperatureCelsius = 31.5,
            relativeHumidityPercent = 69,
            apparentTemperatureCelsius = 36.0,
            precipitationMm = 0.0,
            windSpeedKmh = 9.0,
            wmoCode = 2,
            observedAtIso = "2026-11-17T15:00",
            lastUpdatedEpochMillis = fixedNowMillis
        ),
        hourlyForecasts = listOf(
            HourlyForecastEntity(
                timeIso = "2026-11-17T15:00",
                orderIndex = 0,
                temperatureCelsius = 31.5,
                relativeHumidityPercent = 69,
                precipitationProbabilityPercent = 25,
                precipitationMm = 0.0,
                windSpeedKmh = 9.0,
                wmoCode = 2
            )
        )
    )

    private fun createSampleResponseDto(): OpenMeteoResponseDto = OpenMeteoResponseDto(
        latitude = -3.10,
        longitude = -60.02,
        timezone = "America/Manaus",
        current = CurrentWeatherDto(
            time = "2026-11-17T15:00",
            temperature2m = 32.0,
            relativeHumidity2m = 65,
            apparentTemperature = 36.2,
            precipitation = 0.0,
            weatherCode = 1,
            windSpeed10m = 8.0
        ),
        hourly = HourlyForecastDto(
            time = listOf("2026-11-17T15:00", "2026-11-17T16:00"),
            temperature2m = listOf(32.0, 30.5),
            relativeHumidity2m = listOf(65, 72),
            precipitationProbability = listOf(15, 50),
            precipitation = listOf(0.0, 1.2),
            weatherCode = listOf(1, 80),
            windSpeed10m = listOf(8.0, 11.5)
        )
    )
}
