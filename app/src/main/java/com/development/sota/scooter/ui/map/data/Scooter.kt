package com.development.sota.scooter.ui.map.data

import com.development.sota.scooter.R
import com.mapbox.mapboxsdk.geometry.LatLng
import com.squareup.moshi.Json


data class Scooter(
    val id: Long,
    @Json(name = "scooter_name") val name: String,
    val status: String,
    @Json(name = "alert_status") val alertStatus: String,
    val battery: Double, // Max: 60000.0
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val photo: String?,
    @Json(name = "tracker_id") val trackerId: String,
    @Json(name = "speed_limit") val speedLimit: Double,
    val lamp: Boolean,
    val engine: Boolean,
    @Json(name = "scooter_group") val scooterGroup: List<Int>,
    val rate: Int
) {
    fun getScooterIcon(): Int {
        return when {
            battery <= 20000.0 -> R.drawable.ic_icon_scooter_third
            battery in 20000.0..40000.00 -> R.drawable.ic_icon_scooter_second
            else -> R.drawable.ic_icon_scooter_first
        }
    }

    fun getLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    fun getBatteryPercentage(): String {
        val percents = battery / 60000 * 100

        return "${percents.toInt()} %"
    }
}

data class ScooterResponse(val data: List<Scooter>)

/**
 *  ('ON', 'Online'),
 *  ('UR', 'Under repair'),
 *  ('RT', 'Rented'),
 *  ('BK', 'Booked')
 * */

enum class ScooterStatus(val value: String) {
    ONLINE("ON"), UNDER_REPAIR("UR"), RENTED("RT"), BOOKED("BK")
}
