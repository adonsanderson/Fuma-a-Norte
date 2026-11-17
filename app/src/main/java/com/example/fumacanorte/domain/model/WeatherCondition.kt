package com.example.fumacanorte.domain.model

enum class WeatherCondition {
    CLEAR_SKY,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE_LIGHT,
    DRIZZLE_MODERATE,
    DRIZZLE_DENSE,
    FREEZING_DRIZZLE,
    RAIN_SLIGHT,
    RAIN_MODERATE,
    RAIN_HEAVY,
    FREEZING_RAIN,
    SNOW_FALL,
    RAIN_SHOWERS_SLIGHT,
    RAIN_SHOWERS_MODERATE,
    RAIN_SHOWERS_VIOLENT,
    THUNDERSTORM,
    THUNDERSTORM_WITH_HAIL,
    UNKNOWN;

    val isRainy: Boolean
        get() = when (this) {
            DRIZZLE_LIGHT,
            DRIZZLE_MODERATE,
            DRIZZLE_DENSE,
            FREEZING_DRIZZLE,
            RAIN_SLIGHT,
            RAIN_MODERATE,
            RAIN_HEAVY,
            FREEZING_RAIN,
            RAIN_SHOWERS_SLIGHT,
            RAIN_SHOWERS_MODERATE,
            RAIN_SHOWERS_VIOLENT,
            THUNDERSTORM,
            THUNDERSTORM_WITH_HAIL -> true
            else -> false
        }

    val isSevere: Boolean
        get() = when (this) {
            RAIN_HEAVY,
            FREEZING_RAIN,
            RAIN_SHOWERS_VIOLENT,
            THUNDERSTORM,
            THUNDERSTORM_WITH_HAIL -> true
            else -> false
        }

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR_SKY
            1 -> MAINLY_CLEAR
            2 -> PARTLY_CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            51 -> DRIZZLE_LIGHT
            53 -> DRIZZLE_MODERATE
            55 -> DRIZZLE_DENSE
            56, 57 -> FREEZING_DRIZZLE
            61 -> RAIN_SLIGHT
            63 -> RAIN_MODERATE
            65 -> RAIN_HEAVY
            66, 67 -> FREEZING_RAIN
            71, 73, 75, 77, 85, 86 -> SNOW_FALL
            80 -> RAIN_SHOWERS_SLIGHT
            81 -> RAIN_SHOWERS_MODERATE
            82 -> RAIN_SHOWERS_VIOLENT
            95 -> THUNDERSTORM
            96, 99 -> THUNDERSTORM_WITH_HAIL
            else -> UNKNOWN
        }
    }
}
