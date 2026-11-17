package com.example.fumacanorte.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fumacanorte.domain.usecase.ObserveHourlyDetailUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HourlyDetailViewModel(
    timeIso: String,
    observeHourlyDetailUseCase: ObserveHourlyDetailUseCase
) : ViewModel() {

    val uiState: StateFlow<HourlyDetailUiState> = observeHourlyDetailUseCase(timeIso)
        .map { forecast ->
            if (forecast != null) {
                HourlyDetailUiState.Success(forecast)
            } else {
                HourlyDetailUiState.NotFound
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HourlyDetailUiState.Loading
        )
}
