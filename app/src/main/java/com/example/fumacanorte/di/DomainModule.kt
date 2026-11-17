package com.example.fumacanorte.di

import com.example.fumacanorte.domain.usecase.ObserveHourlyDetailUseCase
import com.example.fumacanorte.domain.usecase.ObserveManausWeatherUseCase
import com.example.fumacanorte.domain.usecase.RefreshManausWeatherUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { ObserveManausWeatherUseCase(repository = get()) }
    factory { RefreshManausWeatherUseCase(repository = get()) }
    factory { ObserveHourlyDetailUseCase(repository = get()) }
}
