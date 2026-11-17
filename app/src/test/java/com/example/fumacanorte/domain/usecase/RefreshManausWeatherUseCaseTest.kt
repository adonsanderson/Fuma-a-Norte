package com.example.fumacanorte.domain.usecase

import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.DomainResult
import com.example.fumacanorte.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RefreshManausWeatherUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var useCase: RefreshManausWeatherUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RefreshManausWeatherUseCase(repository)
    }

    @Test
    fun `invoke returns Success when repository refresh succeeds`() = runTest {
        coEvery { repository.refreshManausWeather() } returns DomainResult.Success(Unit)

        val result = useCase()

        assertEquals(DomainResult.Success(Unit), result)
        coVerify(exactly = 1) { repository.refreshManausWeather() }
    }

    @Test
    fun `invoke returns Failure when repository refresh fails due to no internet`() = runTest {
        val failure = DomainResult.Failure(DomainError.NoInternet)
        coEvery { repository.refreshManausWeather() } returns failure

        val result = useCase()

        assertEquals(failure, result)
        coVerify(exactly = 1) { repository.refreshManausWeather() }
    }
}
