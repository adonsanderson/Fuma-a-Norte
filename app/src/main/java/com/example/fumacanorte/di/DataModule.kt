package com.example.fumacanorte.di

import com.example.fumacanorte.data.repository.WeatherRepositoryImpl
import com.example.fumacanorte.domain.repository.WeatherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

val IO_DISPATCHER_QUALIFIER = named("ioDispatcher")

val dataModule = module {
    single<CoroutineDispatcher>(IO_DISPATCHER_QUALIFIER) {
        Dispatchers.IO
    }

    single<WeatherRepository> {
        WeatherRepositoryImpl(
            weatherDao = get(),
            remoteDataSource = get(),
            ioDispatcher = get(IO_DISPATCHER_QUALIFIER)
        )
    }
}
