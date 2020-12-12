package com.development.sota.scooter.ui.map.domain.entities

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class EndLocation(@Json(name = "lng")
                       val lng: Double = 0.0,
                       @Json(name = "lat")
                       val lat: Double = 0.0)