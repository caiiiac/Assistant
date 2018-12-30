package org.andcreator.assistant.util

import org.andcreator.assistant.R

enum class WeatherIcon(private val iconId: Int) {
    BIG_SNOW(R.drawable.weather_big_snow),
    CLEAR(R.drawable.weather_clear),
    CLEAR_NIGHT(R.drawable.weather_clear_night),
    CLOUD(R.drawable.weather_clouds),
    CLOUD_NIGHT(R.drawable.weather_clouds_night),
    DRIZZLE_DAY(R.drawable.weather_drizzle_day),
    DRIZZLE_NIGHT(R.drawable.weather_drizzle_night),
    FEW_CLOUDS(R.drawable.weather_few_clouds),
    FEW_CLOUDS_NIGHT(R.drawable.weather_few_clouds_night),
    FOG(R.drawable.weather_fog),
    HAIL(R.drawable.weather_hail),
    HAZE(R.drawable.weather_haze),
    MIST(R.drawable.weather_mist),
    NONE_AVAILABLE(R.drawable.weather_none_available),
    RAIN_DAY(R.drawable.weather_rain_day),
    RAIN_NIGHT(R.drawable.weather_rain_night),
    SHOWERS_DAY(R.drawable.weather_showers_day),
    SHOWERS_NIGHT(R.drawable.weather_showers_night),
    SHOWERS_SCATTERED_DAY(R.drawable.weather_showers_scattered_day),
    SHOWERS_SCATTERED_NIGHT(R.drawable.weather_showers_scattered_night),
    SNOW(R.drawable.weather_snow),
    SNOW_RAIN(R.drawable.weather_snow_rain),
    SNOW_SCATTERED_DAY(R.drawable.weather_snow_scattered_day),
    SNOW_SCATTERED_NIGHT(R.drawable.weather_snow_scattered_night),
    STORM(R.drawable.weather_storm),
    STORM_DAY(R.drawable.weather_storm_day),
    STORM_NIGHT(R.drawable.weather_storm_night),
    WIND(R.drawable.weather_wind);

    fun getIcon(): Int{
        return iconId
    }
}