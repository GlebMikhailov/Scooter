package com.development.sota.scooter.ui.map.domain.entities

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class GeocodedWaypointsItem(@Json(name ="types")
                                 val types: List<String>?,
                                 @Json(name = "geocoder_status")
                                 val geocoderStatus: String = "",
                                 @Json(name = "place_id")
                                 val placeId: String = "")