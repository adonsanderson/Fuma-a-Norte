package com.example.fumacanorte.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.presentation.util.WeatherConditionFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HourlyDetailRoute(
    timeIso: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HourlyDetailViewModel = koinViewModel(
        parameters = { parametersOf(timeIso) }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HourlyDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourlyDetailScreen(
    uiState: HourlyDetailUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.hourly_detail_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
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
                HourlyDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                HourlyDetailUiState.NotFound -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("hourly_detail_not_found"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.widthIn(max = 420.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudQueue,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = stringResource(R.string.hourly_detail_not_found_title),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.hourly_detail_not_found_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is HourlyDetailUiState.Success -> {
                    HourlyDetailContent(
                        forecast = uiState.forecast,
                        modifier = Modifier.widthIn(max = 600.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyDetailContent(
    forecast: HourlyForecast,
    modifier: Modifier = Modifier
) {
    val conditionLabel = stringResource(WeatherConditionFormatter.labelRes(forecast.condition))
    val conditionIcon = remember(forecast.condition) {
        WeatherConditionFormatter.icon(forecast.condition)
    }
    val readableDateTime = remember(forecast.timeIso) {
        WeatherConditionFormatter.formatDateTimeReadable(forecast.timeIso)
    }

    val headerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.primaryContainer
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("hourly_detail_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "detail_hero") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBrush)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.location_manaus),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = readableDateTime,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = conditionIcon,
                            contentDescription = conditionLabel,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(
                            R.string.format_temperature_decimal,
                            forecast.temperatureCelsius
                        ),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = conditionLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item(key = "metrics_row_1") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailMetricCard(
                    icon = Icons.Rounded.Thermostat,
                    label = stringResource(R.string.section_current_conditions),
                    value = stringResource(
                        R.string.format_temperature_decimal,
                        forecast.temperatureCelsius
                    ),
                    modifier = Modifier.weight(1f)
                )
                DetailMetricCard(
                    icon = Icons.Rounded.WaterDrop,
                    label = stringResource(R.string.metric_humidity),
                    value = stringResource(
                        R.string.format_percent,
                        forecast.relativeHumidityPercent
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "metrics_row_2") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailMetricCard(
                    icon = Icons.Rounded.Umbrella,
                    label = stringResource(R.string.metric_rain_probability),
                    value = stringResource(
                        R.string.format_percent,
                        forecast.precipitationProbabilityPercent
                    ),
                    modifier = Modifier.weight(1f)
                )
                DetailMetricCard(
                    icon = Icons.Rounded.Grain,
                    label = stringResource(R.string.metric_precipitation),
                    value = stringResource(
                        R.string.format_precipitation_mm,
                        forecast.precipitationMm
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "metrics_row_3") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailMetricCard(
                    icon = Icons.Rounded.Air,
                    label = stringResource(R.string.metric_wind),
                    value = stringResource(
                        R.string.format_wind_kmh,
                        forecast.windSpeedKmh
                    ),
                    modifier = Modifier.weight(1f)
                )
                DetailMetricCard(
                    icon = Icons.Rounded.Info,
                    label = stringResource(R.string.metric_wmo_code),
                    value = stringResource(R.string.format_wmo_code, forecast.wmoCode),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
