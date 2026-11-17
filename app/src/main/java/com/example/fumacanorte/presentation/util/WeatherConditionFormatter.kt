package com.example.fumacanorte.presentation.util

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Dehaze
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.fumacanorte.domain.model.DomainError
import com.example.fumacanorte.domain.model.WeatherCondition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object WeatherConditionFormatter {

    @StringRes
    fun labelRes(condition: WeatherCondition): Int = when (condition) {
        WeatherCondition.CLEAR_SKY -> R.string.wmo_clear_sky
        WeatherCondition.MAINLY_CLEAR -> R.string.wmo_mainly_clear
        WeatherCondition.PARTLY_CLOUDY -> R.string.wmo_partly_cloudy
        WeatherCondition.OVERCAST -> R.string.wmo_overcast
        WeatherCondition.FOG -> R.string.wmo_fog
        WeatherCondition.DRIZZLE_LIGHT -> R.string.wmo_drizzle_light
        WeatherCondition.DRIZZLE_MODERATE -> R.string.wmo_drizzle_moderate
        WeatherCondition.DRIZZLE_DENSE -> R.string.wmo_drizzle_dense
        WeatherCondition.FREEZING_DRIZZLE -> R.string.wmo_freezing_drizzle
        WeatherCondition.RAIN_SLIGHT -> R.string.wmo_rain_slight
        WeatherCondition.RAIN_MODERATE -> R.string.wmo_rain_moderate
        WeatherCondition.RAIN_HEAVY -> R.string.wmo_rain_heavy
        WeatherCondition.FREEZING_RAIN -> R.string.wmo_freezing_rain
        WeatherCondition.SNOW_FALL -> R.string.wmo_snow_fall
        WeatherCondition.RAIN_SHOWERS_SLIGHT -> R.string.wmo_rain_showers_slight
        WeatherCondition.RAIN_SHOWERS_MODERATE -> R.string.wmo_rain_showers_moderate
        WeatherCondition.RAIN_SHOWERS_VIOLENT -> R.string.wmo_rain_showers_violent
        WeatherCondition.THUNDERSTORM -> R.string.wmo_thunderstorm
        WeatherCondition.THUNDERSTORM_WITH_HAIL -> R.string.wmo_thunderstorm_hail
        WeatherCondition.UNKNOWN -> R.string.wmo_unknown
    }

    fun icon(condition: WeatherCondition): ImageVector = when (condition) {
        WeatherCondition.CLEAR_SKY,
        WeatherCondition.MAINLY_CLEAR -> Icons.Rounded.WbSunny
        WeatherCondition.PARTLY_CLOUDY -> Icons.Rounded.WbCloudy
        WeatherCondition.OVERCAST -> Icons.Rounded.Cloud
        WeatherCondition.FOG -> Icons.Rounded.Dehaze
        WeatherCondition.DRIZZLE_LIGHT,
        WeatherCondition.DRIZZLE_MODERATE,
        WeatherCondition.DRIZZLE_DENSE,
        WeatherCondition.FREEZING_DRIZZLE -> Icons.Rounded.Grain
        WeatherCondition.RAIN_SLIGHT,
        WeatherCondition.RAIN_MODERATE,
        WeatherCondition.RAIN_HEAVY,
        WeatherCondition.FREEZING_RAIN,
        WeatherCondition.RAIN_SHOWERS_SLIGHT,
        WeatherCondition.RAIN_SHOWERS_MODERATE,
        WeatherCondition.RAIN_SHOWERS_VIOLENT -> Icons.Rounded.WaterDrop
        WeatherCondition.SNOW_FALL -> Icons.Rounded.AcUnit
        WeatherCondition.THUNDERSTORM,
        WeatherCondition.THUNDERSTORM_WITH_HAIL -> Icons.Rounded.Thunderstorm
        WeatherCondition.UNKNOWN -> Icons.Rounded.Cloud
    }

    @Composable
    fun errorMessage(error: DomainError): String = when (error) {
        DomainError.NoInternet -> stringResource(R.string.error_no_internet)
        DomainError.Timeout -> stringResource(R.string.error_timeout)
        is DomainError.ServerError -> stringResource(R.string.error_server, error.httpCode)
        DomainError.DataParsingError -> stringResource(R.string.error_parsing)
        is DomainError.Unknown -> stringResource(R.string.error_unknown)
    }

    fun formatHourShort(timeIso: String): String {
        val timePart = timeIso.substringAfter('T', missingDelimiterValue = timeIso)
        return if (timePart.length >= 5) timePart.take(5) else timeIso
    }

    fun formatDateTimeReadable(timeIso: String): String {
        val datePart = timeIso.substringBefore('T', missingDelimiterValue = "")
        val hourPart = formatHourShort(timeIso)
        if (datePart.length != 10) return hourPart
        val segments = datePart.split("-")
        if (segments.size != 3) return hourPart
        return "${segments[2]}/${segments[1]} · $hourPart"
    }

    fun formatEpochMillisManaus(epochMillis: Long): String {
        val formatter = SimpleDateFormat("dd/MM · HH:mm", Locale("pt", "BR")).apply {
            timeZone = TimeZone.getTimeZone("America/Manaus")
        }
        return formatter.format(Date(epochMillis))
    }
}
