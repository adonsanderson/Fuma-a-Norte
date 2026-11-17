package com.example.fumacanorte.presentation.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.fumacanorte.presentation.weather.components.CurrentWeatherHeroCard
import com.example.fumacanorte.presentation.weather.components.HourlyForecastItem
import com.example.fumacanorte.presentation.weather.components.SyncStatusBanner
import com.example.fumacanorte.presentation.weather.components.WeatherEmptyView
import com.example.fumacanorte.presentation.weather.components.WeatherErrorView
import com.example.fumacanorte.presentation.weather.components.WeatherLoadingView
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherDashboardRoute(
    onNavigateToHourlyDetail: (timeIso: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeatherDashboardScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onHourlyClick = onNavigateToHourlyDetail,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDashboardScreen(
    uiState: WeatherUiState,
    onEvent: (WeatherUiEvent) -> Unit,
    onHourlyClick: (timeIso: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isRefreshing = (uiState as? WeatherUiState.Success)?.isRefreshing == true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(WeatherUiEvent.Refresh) },
                        enabled = !isRefreshing && uiState !is WeatherUiState.Loading,
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.action_refresh)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (uiState) {
                WeatherUiState.Loading -> {
                    WeatherLoadingView()
                }

                WeatherUiState.Empty -> {
                    WeatherEmptyView(
                        onRetry = { onEvent(WeatherUiEvent.Retry) }
                    )
                }

                is WeatherUiState.Error -> {
                    WeatherErrorView(
                        error = uiState.error,
                        onRetry = { onEvent(WeatherUiEvent.Retry) }
                    )
                }

                is WeatherUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { onEvent(WeatherUiEvent.Refresh) },
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 600.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("weather_dashboard_list"),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (uiState.isRefreshing) {
                                item(key = "sync_progress") {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sync_progress_indicator")
                                    )
                                }
                            }

                            item(key = "sync_banner") {
                                SyncStatusBanner(
                                    syncError = uiState.syncError,
                                    onRetry = { onEvent(WeatherUiEvent.Refresh) },
                                    onDismiss = { onEvent(WeatherUiEvent.DismissSyncError) }
                                )
                            }

                            item(key = "hero_card") {
                                CurrentWeatherHeroCard(snapshot = uiState.snapshot)
                            }

                            item(key = "hourly_section_header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.section_hourly_forecast),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            items(
                                items = uiState.snapshot.hourlyForecasts,
                                key = { forecast -> forecast.timeIso }
                            ) { forecast ->
                                HourlyForecastItem(
                                    forecast = forecast,
                                    onClick = { onHourlyClick(forecast.timeIso) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
