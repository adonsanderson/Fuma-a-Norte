package com.example.fumacanorte.presentation.weather.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.fumacanorte.domain.model.HourlyForecast
import com.example.fumacanorte.presentation.util.WeatherConditionFormatter
import kotlin.math.roundToInt

@Composable
fun HourlyForecastItem(
    forecast: HourlyForecast,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conditionLabel = stringResource(WeatherConditionFormatter.labelRes(forecast.condition))
    val conditionIcon = remember(forecast.condition) {
        WeatherConditionFormatter.icon(forecast.condition)
    }
    val formattedTime = remember(forecast.timeIso) {
        WeatherConditionFormatter.formatDateTimeReadable(forecast.timeIso)
    }
    val roundedTemp = forecast.temperatureCelsius.roundToInt()
    val itemContentDescription = stringResource(
        R.string.hourly_item_content_description,
        formattedTime,
        conditionLabel,
        roundedTemp,
        forecast.precipitationProbabilityPercent
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = itemContentDescription
            }
            .testTag("hourly_item_${forecast.timeIso}"),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = conditionIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = conditionLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            if (forecast.precipitationProbabilityPercent > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = stringResource(
                                R.string.format_percent,
                                forecast.precipitationProbabilityPercent
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.format_temperature, roundedTemp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
