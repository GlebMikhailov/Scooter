package com.development.sota.scooter.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.libraries.maps.model.BitmapDescriptor
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.lang.String


class LoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val t1 = System.nanoTime()
        Log.d(
            "OkHttp",
            String.format(
                "--> Sending request %s on %s%n%s",
                request.url,
                chain.connection(),
                request.headers
            )
        )
        val requestBuffer = Buffer()
        request.body?.writeTo(requestBuffer)
        Log.d("OkHttp", requestBuffer.readUtf8())
        val response: Response = chain.proceed(request)
        val t2 = System.nanoTime()
        Log.d(
            "OkHttp",
            String.format(
                "<-- Received response for %s in %.1fms%n%s",
                response.request.url,
                (t2 - t1) / 1e6,
                response.headers
            )
        )
        val contentType = response.body!!.contentType()
        val content = response.body!!.string()
        Log.d("OkHttp", content)
        val wrappedBody: ResponseBody = ResponseBody.create(contentType, content)
        return response.newBuilder().body(wrappedBody).build()
    }
}

