package com.example.fumacanorte.presentation.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.minimumInteractiveComponentSize
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
                            .minimumInteractiveComponentSize()
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val isExpandedLayout = maxWidth >= 840.dp

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
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isExpandedLayout) {
                            ExpandedDashboardContent(
                                state = uiState,
                                onEvent = onEvent,
                                onHourlyClick = onHourlyClick
                            )
                        } else {
                            CompactDashboardContent(
                                state = uiState,
                                onEvent = onEvent,
                                onHourlyClick = onHourlyClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDashboardContent(
    state: WeatherUiState.Success,
    onEvent: (WeatherUiEvent) -> Unit,
    onHourlyClick: (timeIso: String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .testTag("weather_dashboard_list"),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isRefreshing) {
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
                    syncError = state.syncError,
                    onRetry = { onEvent(WeatherUiEvent.Refresh) },
                    onDismiss = { onEvent(WeatherUiEvent.DismissSyncError) }
                )
            }

            item(key = "hero_card") {
                CurrentWeatherHeroCard(snapshot = state.snapshot)
            }

            item(key = "hourly_section_header") {
                Text(
                    text = stringResource(R.string.section_hourly_forecast),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(
                items = state.snapshot.hourlyForecasts,
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

@Composable
private fun ExpandedDashboardContent(
    state: WeatherUiState.Success,
    onEvent: (WeatherUiEvent) -> Unit,
    onHourlyClick: (timeIso: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_progress_indicator")
                )
            }

            SyncStatusBanner(
                syncError = state.syncError,
                onRetry = { onEvent(WeatherUiEvent.Refresh) },
                onDismiss = { onEvent(WeatherUiEvent.DismissSyncError) }
            )

            CurrentWeatherHeroCard(snapshot = state.snapshot)
        }

        LazyColumn(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .testTag("weather_dashboard_list"),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hourly_section_header") {
                Text(
                    text = stringResource(R.string.section_hourly_forecast),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(
                items = state.snapshot.hourlyForecasts,
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
