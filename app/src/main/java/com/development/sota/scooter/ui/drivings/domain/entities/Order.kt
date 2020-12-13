package com.development.sota.scooter.ui.drivings.domain.entities

import android.annotation.SuppressLint
import com.development.sota.scooter.ui.map.data.Scooter
import com.development.sota.scooter.ui.map.domain.entities.Rate
import com.squareup.moshi.Json
import java.text.SimpleDateFormat
import java.util.*

data class Order(
    val id: Long,
    val date: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "finish_time") val finishTime: String,
    val scooter: Scooter,
    val cost: Int,
    val rate: Rate?
) {
    companion object {
        @SuppressLint("ConstantLocale")
        private val timeFormatter = SimpleDateFormat("H:mm:ss", Locale.getDefault())
        @SuppressLint("ConstantLocale")
        private val dateFormatter = SimpleDateFormat("dd MM yyyy", Locale.getDefault())

        fun makePreOrder(scooter: Scooter): Order {
            return Order(
                -1,
                    dateFormatter.format(GregorianCalendar.getInstance().time),
                    timeFormatter.format(GregorianCalendar.getInstance().time),
                    timeFormatter.format(GregorianCalendar.getInstance().time),
                    scooter,
                    1,
                null
                )
        }
    }
    fun parseStartTime(): Date {
        return timeFormatter.parse(startTime) ?: Date()
    }

    fun parseEndTime(): Date {
        return timeFormatter.parse(finishTime) ?: Date()
    }


}

data class OrderWithStatus(val order: Order, var status: OrderStatus)

enum class OrderStatus {
    ACTIVATE, CHOOSE_RATE, IN_WORK, CLOSED
}