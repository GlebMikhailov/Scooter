package com.development.sota.scooter.ui.map.data

import com.squareup.moshi.Json

data class Client(
    val id: Long,
    @Json(name = "client_name") val clientName: String,
    val surname: String,
    val status: String,
    val balance: String,
    @Json(name = "client_photo") val clientPhoto: String?,
    val phone: String,
    @Json(name = "failed_books") val failedBooks: Long
)
