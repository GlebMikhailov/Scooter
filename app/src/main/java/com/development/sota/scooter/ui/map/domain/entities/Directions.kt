package com.development.sota.scooter.ui.map.domain.entities

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class Directions(@Json(name = "routes")
                      val routes: List<RoutesItem>,
                      @Json(name = "geocoded_waypoints")
                      val geocodedWaypoints: List<GeocodedWaypointsItem>?,
                      @Json(name = "status")
                      val status: String = "")