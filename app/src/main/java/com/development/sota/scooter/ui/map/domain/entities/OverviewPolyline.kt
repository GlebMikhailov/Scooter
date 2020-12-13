package com.development.sota.scooter.ui.map.domain.entities

import com.squareup.moshi.Json

data class OverviewPolyline(
    @Json(name = "points")
    val points: String = ""
)