package com.development.sota.scooter.ui.map.data

import com.development.sota.scooter.R
import com.development.sota.scooter.api.Wrapped
import com.google.android.libraries.maps.model.LatLng
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson


class ScootersJsonConverter {
    @Wrapped
    @FromJson
    fun fromJson(json: ScooterResponse): List<Scooter> {
        return json.data
    }

    @ToJson
    fun toJson(@Wrapped value: List<Scooter?>?): ScooterResponse {
        throw UnsupportedOperationException()
    }
}


data class Scooter(
    val id: Long,
    @Json(name = "scooter_name") val name: String,
    val status: String,
    @Json(name = "alert_status") val alertStatus: String,
    val battery: Double, // Max: 60000.0
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val photo: String,
    @Json(name = "tracker_id") val trackerId: String,
    @Json(name = "speed_limit") val speedLimit: Double,
    val lamp: Boolean,
    val engine: Boolean,
    @Json(name = "scooter_group") val scooterGroup: List<Int>
) {
    fun getScooterIcon(): Int {
        return when {
            battery <= 20000.0 ->  R.drawable.ic_icon_scooter_third
            battery in 20000.0..40000.00 -> R.drawable.ic_icon_scooter_second
            else -> R.drawable.ic_icon_scooter_first
        }
    }

    fun getLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}

data class ScooterResponse(val data: List<Scooter>)