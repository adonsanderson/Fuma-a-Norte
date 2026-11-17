package com.example.fumacanorte.data.remote.source

import com.example.fumacanorte.data.remote.api.OpenMeteoApiService
import com.example.fumacanorte.data.remote.dto.CurrentWeatherDto
import com.example.fumacanorte.data.remote.dto.HourlyForecastDto
import com.example.fumacanorte.data.remote.dto.OpenMeteoResponseDto
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.DomainResult
import com.squareup.moshi.JsonDataException
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class WeatherRemoteDataSourceImplTest {

    private lateinit var apiService: OpenMeteoApiService
    private lateinit var dataSource: WeatherRemoteDataSourceImpl

    @Before
    fun setUp() {
        apiService = mockk()
        dataSource = WeatherRemoteDataSourceImpl(apiService)
    }

    @Test
    fun `fetchManausForecast returns Success when api returns payload`() = runTest {
        val dto = OpenMeteoResponseDto(
            latitude = -3.10,
            longitude = -60.02,
            timezone = "America/Manaus",
            current = CurrentWeatherDto("2026-11-17T15:00", 31.0, 70, 35.0, 0.0, 1, 7.0),
            hourly = HourlyForecastDto(
                time = listOf("2026-11-17T15:00"),
                temperature2m = listOf(31.0),
                relativeHumidity2m = listOf(70),
                precipitationProbability = listOf(10),
                precipitation = listOf(0.0),
                weatherCode = listOf(1),
                windSpeed10m = listOf(7.0)
            )
        )
        coEvery { apiService.getForecast(any(), any(), any(), any(), any(), any()) } returns dto

        val result = dataSource.fetchManausForecast()

        assertEquals(DomainResult.Success(dto), result)
    }

    @Test
    fun `fetchManausForecast maps UnknownHostException and IOException to NoInternet`() = runTest {
        coEvery { apiService.getForecast(any(), any(), any(), any(), any(), any()) } throws UnknownHostException()
        assertEquals(DomainResult.Failure(DomainError.NoInternet), dataSource.fetchManausForecast())

        coEvery { apiService.getForecast(any(), any(), any(), any(), any(), any()) } throws IOException("offline")
        assertEquals(DomainResult.Failure(DomainError.NoInternet), dataSource.fetchManausForecast())
    }

    @Test
    fun `fetchManausForecast maps SocketTimeoutException to Timeout`() = runTest {
        coEvery { apiService.getForecast(any(), any(), any(), any(), any(), any()) } throws SocketTimeoutException()

        val result = dataSource.fetchManausForecast()

        assertEquals(DomainResult.Failure(DomainError.Timeout), result)
    }

    @Test
    fun `fetchManausForecast maps HttpException to ServerError`() = runTest {
        val httpException = HttpException(Response.error<Any>(503, "Service Unavailable".toResponseBody()))
        coEvery { apiService.getForecast(any(), any(), any(), any(), any(), any()) } throws httpException

        val result = dataSource.fetchManausForecast()

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.ServerError)
        assertEquals(503, (error as DomainError.ServerError).httpCode)
    }

    @Test
    fun `fetchManausForecast maps JsonDataException to DataParsingError`() = runTest {
        coEvery { apiService.getForecast(any(), any(), any(), any(), any(), any()) } throws JsonDataException("malformed")

        val result = dataSource.fetchManausForecast()

        assertEquals(DomainResult.Failure(DomainError.DataParsingError), result)
    }

    @Test(expected = CancellationException::class)
    fun `fetchManausForecast rethrows CancellationException`() = runTest {
        coEvery { apiService.getForecast(any(), any(), any(), any(), any(), any()) } throws CancellationException("cancelled")

        dataSource.fetchManausForecast()
    }
}
