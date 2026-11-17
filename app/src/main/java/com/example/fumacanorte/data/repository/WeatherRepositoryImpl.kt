package com.example.fumacanorte.data.repository

import com.example.fumacanorte.data.local.dao.WeatherDao
import com.example.fumacanorte.data.mapper.toDomain
import com.example.fumacanorte.data.mapper.toEntities
import com.example.fumacanorte.data.remote.source.WeatherRemoteDataSource
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.DomainResult
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.domain.model.WeatherSnapshot
import com.example.fumacanorte.domain.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WeatherRepositoryImpl(
    private val weatherDao: WeatherDao,
    private val remoteDataSource: WeatherRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val currentTimeMillisProvider: () -> Long = { System.currentTimeMillis() }
) : WeatherRepository {

    override fun observeManausWeather(): Flow<WeatherSnapshot?> =
        weatherDao.observeWeatherSnapshot().map { relation ->
            relation?.toDomain()
        }

    override fun observeHourlyForecast(timeIso: String): Flow<HourlyForecast?> =
        weatherDao.observeHourlyForecast(timeIso).map { entity ->
            entity?.toDomain()
        }

    override suspend fun refreshManausWeather(): DomainResult<Unit> = withContext(ioDispatcher) {
        when (val remoteResult = remoteDataSource.fetchManausForecast()) {
            is DomainResult.Failure -> remoteResult
            is DomainResult.Success -> {
                val bundle = remoteResult.data.toEntities(
                    lastUpdatedEpochMillis = currentTimeMillisProvider()
                ) ?: return@withContext DomainResult.Failure(DomainError.DataParsingError)

                try {
                    weatherDao.replaceWeatherData(
                        current = bundle.current,
                        forecasts = bundle.hourlyForecasts
                    )
                    DomainResult.Success(Unit)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    DomainResult.Failure(DomainError.Unknown(throwable.message))
                }
            }
        }
    }
}
