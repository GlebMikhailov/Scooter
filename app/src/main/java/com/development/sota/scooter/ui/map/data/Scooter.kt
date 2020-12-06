package com.development.sota.scooter.ui.map.data

import androidx.annotation.IdRes
import com.development.sota.scooter.R
import com.google.android.libraries.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class Scooter(
    val id: Long,
    @SerializedName("scooter_name") val name: String,
    val status: String,
    @SerializedName("alert_status") val alertStatus: String,
    val battery: Double, // Max: 60000.0
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val photo: String,
    @SerializedName("tracker_id") val trackerId: String,
    @SerializedName("speed_limit") val speedLimit: Double,
    val lamp: Boolean,
    val engine: Boolean,
    @SerializedName("scooter_group") val scooterGroup: ArrayList<Int>
) {
    fun getScooterIcon(): Int {
        return when {
            battery <= 20000.0 ->  R.drawable.ic_icon_scooter__third
            battery in 20000.0..40000.00 -> R.drawable.ic_icon_scooter_second
            else -> R.drawable.ic_icon_scooter_first
        }
    }

    fun getLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}

data class ScooterResponse(val data: ArrayList<Scooter>)