package com.development.sota.scooter.ui.map.domain.entities

import com.development.sota.scooter.ui.map.domain.entities.Bounds
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class RoutesItem(@Json(name = "summary")
                      val summary: String? = "",
                      @Json(name = "copyrights")
                      val copyrights: String? = "",
                      @Json(name = "legs")
                      val legs: List<LegsItem>,
                      @Json(name = "bounds")
                      val bounds: Bounds?,
                      @Json(name = "overview_polyline")
                      val overviewPolyline: OverviewPolyline)