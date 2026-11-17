package com.example.fumacanorte.data.remote.source

import com.example.fumacanorte.data.remote.api.OpenMeteoApiService
import com.example.fumacanorte.data.remote.dto.OpenMeteoResponseDto
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.DomainResult
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

interface WeatherRemoteDataSource {
    suspend fun fetchManausForecast(): DomainResult<OpenMeteoResponseDto>
}

class WeatherRemoteDataSourceImpl(
    private val apiService: OpenMeteoApiService
) : WeatherRemoteDataSource {

    override suspend fun fetchManausForecast(): DomainResult<OpenMeteoResponseDto> {
        return try {
            val response = apiService.getForecast()
            DomainResult.Success(response)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (timeout: SocketTimeoutException) {
            DomainResult.Failure(DomainError.Timeout)
        } catch (unknownHost: UnknownHostException) {
            DomainResult.Failure(DomainError.NoInternet)
        } catch (ioException: IOException) {
            DomainResult.Failure(DomainError.NoInternet)
        } catch (httpException: HttpException) {
            DomainResult.Failure(
                DomainError.ServerError(
                    httpCode = httpException.code(),
                    serverMessage = httpException.message()
                )
            )
        } catch (jsonException: JsonDataException) {
            DomainResult.Failure(DomainError.DataParsingError)
        } catch (encodingException: JsonEncodingException) {
            DomainResult.Failure(DomainError.DataParsingError)
        } catch (throwable: Throwable) {
            DomainResult.Failure(DomainError.Unknown(throwable.message))
        }
    }
}
