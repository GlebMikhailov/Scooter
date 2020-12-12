package com.development.sota.scooter.ui.map.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.development.sota.scooter.R
import com.development.sota.scooter.ui.map.data.Scooter
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlin.math.max

class ScooterClusterItem(var scooter: Scooter): ClusterItem {
    override fun getSnippet(): String? {
        return ""
    }

    override fun getTitle(): String? {
        return ""
    }

    override fun getPosition(): LatLng {
        return scooter.getLatLng()
    }
}

class MarkerClusterRenderer(val context: Context, map: GoogleMap, clusterManager: ClusterManager<ScooterClusterItem>?):
    DefaultClusterRenderer<ScooterClusterItem>(context, map, clusterManager) {
    private val MARKER_DIMENSION = 48
    private var iconGenerator: IconGenerator? = null
    private var markerImageView: ImageView? = null

    init {
        iconGenerator = IconGenerator(context)
        markerImageView = ImageView(context)
        markerImageView!!.setLayoutParams(ViewGroup.LayoutParams(MARKER_DIMENSION, MARKER_DIMENSION))
        iconGenerator!!.setContentView(markerImageView)
    }

    override fun onBeforeClusterItemRendered(item: ScooterClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)

        markerOptions.icon(bitmapDescriptorFromVector(item.scooter.getScooterIcon()));  // 8
        markerOptions.title(item.title);
    }

    private fun bitmapDescriptorFromVector(
        @DrawableRes vectorResId: Int
    ): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        val backgroundDrawable =
            ContextCompat.getDrawable(context, R.drawable.ic_purple_circle_with_white_corner)

        vectorDrawable!!.setBounds(
            (((max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt() - vectorDrawable.intrinsicWidth * 1.5 ) / 1.5).toInt(),
            (((max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt() - vectorDrawable.intrinsicHeight * 1.5) / 1.5).toInt(),
            (vectorDrawable.intrinsicWidth * 1.5 + 0.5 * (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8 - vectorDrawable.intrinsicWidth * 1.5) / 1.5).toInt(),
            (vectorDrawable.intrinsicHeight * 1.5 + 0.5 * (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8 - vectorDrawable.intrinsicHeight * 1.5) / 1.5).toInt()
        )
        backgroundDrawable!!.setBounds(
            0,
            0,
            (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt(),
            (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt()
        )

        val bitmap = Bitmap.createBitmap(
            (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt(),
            (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt(),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        backgroundDrawable.draw(canvas)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


}