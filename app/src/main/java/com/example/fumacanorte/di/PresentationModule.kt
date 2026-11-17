package com.example.fumacanorte.di

import com.example.fumacanorte.presentation.detail.HourlyDetailViewModel
import com.example.fumacanorte.presentation.weather.WeatherViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        WeatherViewModel(
            observeManausWeatherUseCase = get(),
            refreshManausWeatherUseCase = get()
        )
    }

    viewModel { (timeIso: String) ->
        HourlyDetailViewModel(
            timeIso = timeIso,
            observeHourlyDetailUseCase = get()
        )
    }
}
