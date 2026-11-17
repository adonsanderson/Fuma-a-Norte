package com.example.fumacanorte.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun `fromWmoCode maps known WMO codes accurately`() {
        assertEquals(WeatherCondition.CLEAR_SKY, WeatherCondition.fromWmoCode(0))
        assertEquals(WeatherCondition.MAINLY_CLEAR, WeatherCondition.fromWmoCode(1))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCondition.fromWmoCode(2))
        assertEquals(WeatherCondition.OVERCAST, WeatherCondition.fromWmoCode(3))
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(45))
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(48))
        assertEquals(WeatherCondition.DRIZZLE_LIGHT, WeatherCondition.fromWmoCode(51))
        assertEquals(WeatherCondition.DRIZZLE_MODERATE, WeatherCondition.fromWmoCode(53))
        assertEquals(WeatherCondition.DRIZZLE_DENSE, WeatherCondition.fromWmoCode(55))
        assertEquals(WeatherCondition.RAIN_SLIGHT, WeatherCondition.fromWmoCode(61))
        assertEquals(WeatherCondition.RAIN_MODERATE, WeatherCondition.fromWmoCode(63))
        assertEquals(WeatherCondition.RAIN_HEAVY, WeatherCondition.fromWmoCode(65))
        assertEquals(WeatherCondition.RAIN_SHOWERS_SLIGHT, WeatherCondition.fromWmoCode(80))
        assertEquals(WeatherCondition.RAIN_SHOWERS_MODERATE, WeatherCondition.fromWmoCode(81))
        assertEquals(WeatherCondition.RAIN_SHOWERS_VIOLENT, WeatherCondition.fromWmoCode(82))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(95))
        assertEquals(WeatherCondition.THUNDERSTORM_WITH_HAIL, WeatherCondition.fromWmoCode(96))
        assertEquals(WeatherCondition.THUNDERSTORM_WITH_HAIL, WeatherCondition.fromWmoCode(99))
    }

    @Test
    fun `fromWmoCode returns UNKNOWN for unmapped codes`() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(-1))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(999))
    }

    @Test
    fun `isRainy and isSevere reflect condition severity correctly`() {
        assertFalse(WeatherCondition.CLEAR_SKY.isRainy)
        assertFalse(WeatherCondition.CLEAR_SKY.isSevere)

        assertTrue(WeatherCondition.RAIN_SLIGHT.isRainy)
        assertFalse(WeatherCondition.RAIN_SLIGHT.isSevere)

        assertTrue(WeatherCondition.THUNDERSTORM.isRainy)
        assertTrue(WeatherCondition.THUNDERSTORM.isSevere)

        assertTrue(WeatherCondition.RAIN_HEAVY.isRainy)
        assertTrue(WeatherCondition.RAIN_HEAVY.isSevere)
    }
}
