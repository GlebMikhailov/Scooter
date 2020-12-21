package com.development.sota.scooter.ui.map.data

import com.squareup.moshi.Json

data class Rate(
    val id: Long,
    val minute: String,
    val hour: String,
    @Json(name = "free_book_minutes") val freeBookMinutes: Int,
    @Json(name = "pause_rate") val pauseRate: String
)

enum class RateType(val value: String) {
    MINUTE("Minute"), HOUR("Hour")
}