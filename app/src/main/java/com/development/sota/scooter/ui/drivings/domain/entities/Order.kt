package com.development.sota.scooter.ui.drivings.domain.entities

import android.annotation.SuppressLint
import com.development.sota.scooter.ui.map.data.Rate
import com.development.sota.scooter.ui.map.data.Scooter

import com.squareup.moshi.Json
import java.text.SimpleDateFormat
import java.util.*

data class Order(
    val id: Long,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "finish_time") val finishTime: String,
    @Json(name = "is_paid") val isPaid: Boolean,
    @Json(name = "activation_time") val activationTime: String = "",
    val status: String,
    val scooter: Long,
    val cost: Double,
    val rate: Long
) {
    companion object {
        @SuppressLint("ConstantLocale")
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val decodeDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SSSS'Z'", Locale.getDefault())
    }

    fun parseStartTime(): Date {
        return decodeDateFormatter.parse(startTime) ?: Date()
    }

    fun parseEndTime(): Date {
        return decodeDateFormatter.parse(finishTime) ?: Date()
    }

    fun parseActivationTime(): Date {
        return decodeDateFormatter.parse(activationTime) ?: Date()
    }

}

data class OrderWithStatus(val order: Order, val scooter: Scooter, var status: OrderStatus)

data class AddOrderResponse(val id: Long)

enum class OrderStatus(val value: String) {
    CANDIDIATE("CA"), BOOKED("BK"), CHOOSE_RATE("CR"), ACTIVATED("AC"), CLOSED("CD"), CANCELED("CCD")
}