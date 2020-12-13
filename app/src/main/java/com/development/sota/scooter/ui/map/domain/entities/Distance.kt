package com.development.sota.scooter.ui.map.domain.entities

import com.squareup.moshi.Json

data class Distance(
    @Json(name = "text")
    val text: String = "",
    @Json(name = "value")
    val value: Int = 0
)