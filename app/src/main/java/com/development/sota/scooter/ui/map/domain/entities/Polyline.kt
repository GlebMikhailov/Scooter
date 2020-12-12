package com.development.sota.scooter.ui.map.domain.entities

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class Polyline(@Json(name = "points")
                    val points: String = "")