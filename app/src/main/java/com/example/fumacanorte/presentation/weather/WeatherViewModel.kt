package com.example.fumacanorte.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.DomainResult
import com.example.fumacanorte.domain.usecase.ObserveManausWeatherUseCase
import com.example.fumacanorte.domain.usecase.RefreshManausWeatherUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeatherViewModel(
    observeManausWeatherUseCase: ObserveManausWeatherUseCase,
    private val refreshManausWeatherUseCase: RefreshManausWeatherUseCase
) : ViewModel() {

    private val isRefreshingFlow = MutableStateFlow(false)
    private val syncErrorFlow = MutableStateFlow<DomainError?>(null)
    private val hasAttemptedSyncFlow = MutableStateFlow(false)

    val uiState: StateFlow<WeatherUiState> = combine(
        observeManausWeatherUseCase(),
        isRefreshingFlow,
        syncErrorFlow,
        hasAttemptedSyncFlow
    ) { snapshot, isRefreshing, syncError, hasAttemptedSync ->
        when {
            snapshot != null -> WeatherUiState.Success(
                snapshot = snapshot,
                isRefreshing = isRefreshing,
                syncError = syncError
            )
            isRefreshing || !hasAttemptedSync -> WeatherUiState.Loading
            syncError != null -> WeatherUiState.Error(syncError)
            else -> WeatherUiState.Empty
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = WeatherUiState.Loading
    )

    init {
        refreshWeather()
    }

    fun onEvent(event: WeatherUiEvent) {
        when (event) {
            WeatherUiEvent.Refresh,
            WeatherUiEvent.Retry -> refreshWeather()
            WeatherUiEvent.DismissSyncError -> {
                syncErrorFlow.value = null
            }
        }
    }

    private fun refreshWeather() {
        if (isRefreshingFlow.value) return
        viewModelScope.launch {
            isRefreshingFlow.value = true
            syncErrorFlow.value = null
            when (val result = refreshManausWeatherUseCase()) {
                is DomainResult.Success -> Unit
                is DomainResult.Failure -> {
                    syncErrorFlow.value = result.error
                }
            }
            hasAttemptedSyncFlow.value = true
            isRefreshingFlow.value = false
        }
    }
}
