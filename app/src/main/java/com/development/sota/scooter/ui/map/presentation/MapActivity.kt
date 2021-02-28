package com.development.sota.scooter.ui.map.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.development.sota.scooter.BASE_URL
import com.development.sota.scooter.R
import com.development.sota.scooter.WifiReceiver
import com.development.sota.scooter.api.ClientService
import com.development.sota.scooter.api.client.RetrofitClient
import com.development.sota.scooter.api.client.UserClass
import com.development.sota.scooter.api.client.WebResponse
import com.development.sota.scooter.databinding.ActivityMapBinding
import com.development.sota.scooter.ui.drivings.DrivingsActivity
import com.development.sota.scooter.ui.drivings.DrivingsStartTarget
import com.development.sota.scooter.ui.drivings.domain.entities.Order
import com.development.sota.scooter.ui.drivings.domain.entities.OrderStatus
import com.development.sota.scooter.ui.help.HelpActivity
import com.development.sota.scooter.ui.map.data.Rate
import com.development.sota.scooter.ui.map.data.Scooter
import com.development.sota.scooter.ui.profile.ProfileActivity
import com.development.sota.scooter.ui.promo.PromoActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.*
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.item_scooter_driving.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moxy.MvpAppCompatActivity
import moxy.MvpView
import moxy.ktx.moxyPresenter
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEnd
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit


interface MapView : MvpView {


    @AddToEnd
    fun updateScooterMarkers(scootersFeatures: List<Feature>)

    @AddToEnd
    fun initLocationRelationships()

    @AddToEnd
    fun drawRoute(polyline: String)

    @AddToEnd
    fun showScooterCard(scooter: Scooter, status: OrderStatus)

    @AddToEnd
    fun setRateForScooterCard(rate: Rate, scooterId: Long)

    @AddToEnd
    fun setDialogBy(type: MapDialogType)

    @AddToEnd
    fun showToast(text: String)

    @AddToEnd
    fun setLoading(by: Boolean)

    @AddToEnd
    fun initPopupMapView(orders: List<Order>, bookCount: Int, rentCount: Int)

    @AddToEnd
    fun sendToDrivingsList()

    @AddToEnd
    fun sendToPromoList()

    @AddToEnd
    fun sendToHelpActivity()

    @AddToEnd
    fun drawGeoZones(feauters: ArrayList<Feature>)

    @AddToEnd
    fun sendToTheProfileActivity()
}


class MapActivity : MvpAppCompatActivity(), MapView {
    private var _binding: ActivityMapBinding? = null
    private val binding get() = _binding!!

    private val presenter by moxyPresenter { MapPresenter(this) }
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var map: MapboxMap? = null
    private lateinit var markerManager: MarkerViewManager
    private lateinit var localizationPlugin: LocalizationPlugin
    private lateinit var geoJsonSource: GeoJsonSource

    private val disposableJobsBag = hashSetOf<Job>()

    private var currentShowingScooter = -1L

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mabox_access_token))

        _binding = ActivityMapBinding.inflate(layoutInflater)

        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("account", Context.MODE_PRIVATE)
        val headerView: View = binding.navView.getHeaderView(0)
        val navUsername: TextView = headerView.findViewById<View>(R.id.text_name_header) as TextView
        val navUserBalance: TextView = headerView.findViewById<View>(R.id.text_balance_header) as TextView
        var balance = sharedPreferences.getString("balance", "")
        var name = sharedPreferences.getString("name", "")

        changePBSTextColor()

        if (balance.isNullOrEmpty()) {
              RetrofitClient().getContentData(RetrofitClient().service(this@MapActivity).getUser("json", sharedPreferences.getLong("id", -1).toString()), object : WebResponse {
                override fun onResponseSuccess(result: Response<List<UserClass>>) {
                    result.body()?.forEach { data ->
                        navUsername.text = name

                        navUserBalance.text = "${data.balance} ₽ "
                        try {
                            sharedPreferences.edit().putString("balance", data.balance.toString())
                        } catch (e: Exception){
                            Log.d("us_user_data_error", "error = $e")
                        }
                    }


                }

                override fun onResponseFailed(error: String?) {
                }

            })
        } else {
            navUsername.text = name
            navUserBalance.text = "$balance ₽ "
        }

        binding.contentOfMap.imageButtonMapQr.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(this, DrivingsActivity::class.java)
                intent.putExtra("aim", DrivingsStartTarget.QRandCode)

                startActivityForResult(intent, 42)
            } else {
                getCameraPermission()
            }
        }

        binding.contentOfMap.imageButtonMapMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.contentOfMap.imageButtonMapPromo.setOnClickListener {
            presenter.sendToThePromoList()
        }
        binding.contentOfMap.imageButtonMapLocation.setOnClickListener {
            Timber.w("Geoposition")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                initLocationRelationships()
            } else {
                getLocationPermission()
            }
        }

        binding.contentOfMap.mapPopupItem.constraintLayoutPopupMap.clipToOutline = true
        binding.contentOfMap.mapPopupItem.imageViewMapPopupScooterIcon.clipToOutline = true
        binding.contentOfMap.mapPopupItem.constraintLayoutParentPopupMap.visibility = View.GONE

        binding.contentOfMap.mapScooterItem.constraintLayoutItemScooterParent.clipToOutline = true
        binding.contentOfMap.mapScooterItem.constraintLayoutItemScooterParent.visibility = View.GONE

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menuMapItemDrivings -> presenter.sendToTheDrivingsList()
                R.id.menuMapItemPromo -> presenter.sendToThePromoList()
                R.id.menuMapItemHelp -> presenter.sendToHelpActivity()
                R.id.menuMapItemProfile -> presenter.sendToTheProfileActivity()
            }

            return@setNavigationItemSelectedListener true
        }

        binding.contentOfMap.mapPopupItem.cardViewPopupMap.setOnClickListener {
            presenter.sendToTheDrivingsList()
        }

        fusedLocationProviderClient = FusedLocationProviderClient(this)

        binding.contentOfMap.mapView.getMapAsync { map ->
            this.map = map
            map.uiSettings.isCompassEnabled = false
            map.setStyle(Style.LIGHT) { style ->

                style.addImageAsync(
                    SCOOTERS_ICON_THIRD,
                    bitmapIconFromVector(R.drawable.ic_icon_scooter_third)
                )
                style.addImageAsync(
                    SCOOTERS_ICON_SECOND,
                    bitmapIconFromVector(R.drawable.ic_icon_scooter_second)
                )
                style.addImageAsync(
                    SCOOTERS_ICON_FIRST,
                    bitmapIconFromVector(R.drawable.ic_icon_scooter_first)
                )
                style.addImageAsync(
                    SCOOTERS_ICON_CHOSEN,
                    getBitmapFromVectorDrawable(R.drawable.ic_chosen_scooter_icon)!!
                )
                style.addImageAsync(
                    MY_MARKER_IMAGE, getBitmapFromVectorDrawable(R.drawable.ic_user_marker)!!
                )

                localizationPlugin = LocalizationPlugin(binding.contentOfMap.mapView, map, style)
                localizationPlugin.matchMapLanguageWithDeviceDefault()

                val polygon = Polygon.fromLngLats(
                    arrayListOf(
                        arrayListOf(
                            Point.fromLngLat(-180.0, -89.999999999999),
                            Point.fromLngLat(-180.0, 89.99999999999),
                            Point.fromLngLat(179.99999999, 89.99999999999),
                            Point.fromLngLat(179.99999999, -89.99999999999),
                            Point.fromLngLat(0.0, -89.99999999999),
                            Point.fromLngLat(0.0, 89.99999999999)
                        )
                    ) as List<MutableList<Point>>
                )

                style.addSource(
                    GeoJsonSource(
                        GEOZONE_BACKGROUND_SOURCE,
                        Feature.fromGeometry(polygon),
                        GeoJsonOptions()
                    )
                )

                val layer = FillLayer(
                    GEOZONE_BACKGROUND_LAYER, GEOZONE_BACKGROUND_SOURCE
                ).withProperties(
                    fillColor(GEOZONE_COLOR)
                )

                style.addLayer(layer)
            }

            markerManager = MarkerViewManager(binding.contentOfMap.mapView, map)

            map.addOnMapClickListener {
                binding.contentOfMap.mapScooterItem.cardViewScooterItem.visibility = View.GONE

                val pointf: PointF = map.projection.toScreenLocation(it)
                val rectF = RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10)
                val mapClickFeatureList: List<Feature> =
                    map.queryRenderedFeatures(rectF, CIRCLES_LAYER)

                val mapClickScootersList = map.queryRenderedFeatures(rectF, SCOOTERS_LAYER)

                if (mapClickFeatureList.isNotEmpty()) {
                    val clusterLeavesFeatureCollection: FeatureCollection =
                        geoJsonSource.getClusterLeaves(
                            mapClickFeatureList[0],
                            8000, 0
                        )
                    moveCameraToLeavesBounds(clusterLeavesFeatureCollection)
                }

                if (mapClickScootersList.size == 1) {
                    val scooter = mapClickScootersList.first().getProperty("id").asLong

                    if (currentShowingScooter != scooter) {
                        presenter.clickedOnScooterWith(id = scooter)
                    } else {
                        currentShowingScooter = -1L
                    }
                } else {
                    currentShowingScooter = -1L

                    presenter.scooterUnselected()

                    map.style?.removeLayer(ROUTE_LAYER)
                    map.style?.removeSource(ROUTE_SOURCE)

                }

                return@addOnMapClickListener true
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(44.8789802569887, 37.3763933050872,), 12.0))
            //getLocationPermission()
            //initLocationRelationships()

        }

        mNetworkReceiver = WifiReceiver()
        registerNetworkBroadcastForNougat()


    }
    private var mNetworkReceiver: BroadcastReceiver? = null
    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    protected fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChanges()
    }

    private fun moveCameraToLeavesBounds(featureCollectionToInspect: FeatureCollection) {
        val latLngList: MutableList<LatLng> = ArrayList()
        if (featureCollectionToInspect.features() != null) {
            for (singleClusterFeature in featureCollectionToInspect.features()!!) {
                val clusterPoint: Point? = singleClusterFeature.geometry() as Point?

                if (clusterPoint != null) {
                    latLngList.add(LatLng(clusterPoint.latitude(), clusterPoint.longitude()))
                }
            }
            if (latLngList.size > 1) {
                val latLngBounds = LatLngBounds.Builder()
                    .includes(latLngList)
                    .build()
                map?.easeCamera(
                    CameraUpdateFactory.newLatLngBounds(latLngBounds, 230),
                    1300
                )
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "TimberArgCount")
    override fun updateScooterMarkers(scootersFeatures: List<Feature>) {
        runOnUiThread {
            map?.style?.removeLayer(CLUSTERS_LAYER)
            map?.style?.removeLayer(SCOOTERS_LAYER)
            map?.style?.removeLayer(CIRCLES_LAYER)
            map?.style?.removeLayer(COUNT_LAYER)

            map?.style?.removeSource(SCOOTERS_SOURCE)

            geoJsonSource = GeoJsonSource(
                SCOOTERS_SOURCE,
                FeatureCollection.fromFeatures(scootersFeatures),
                GeoJsonOptions()
                    .withCluster(true)
                    .withClusterMaxZoom(MAX_CLUSTER_ZOOM_LEVEL)
                    .withClusterRadius(CLUSTER_RADIUS)
            )

            map?.style?.addSource(
                geoJsonSource
            )

            val scootersLayer = SymbolLayer(SCOOTERS_LAYER, SCOOTERS_SOURCE)
            scootersLayer.setProperties(
                iconImage(get(SCOOTER_ICON_SOURCE)),
            )

            map?.style?.addLayer(scootersLayer)

            val layer = intArrayOf(1, ContextCompat.getColor(this, R.color.purple_text))

            val circles = CircleLayer(CLUSTERS_LAYER, SCOOTERS_SOURCE)
            circles.setProperties(
                circleColor(layer[1]),
                circleRadius(18f)
            )
            val pointCount = toNumber(get("point_count"))


            circles.setFilter(
                all(
                    has("point_count"),
                    gte(pointCount, 1)
                )
            )

            map?.style?.addLayer(circles)

            val count = SymbolLayer(COUNT_LAYER, SCOOTERS_SOURCE)
            count.setProperties(
                textField(toString(get("point_count"))),
                textSize(12f),
                textColor(Color.WHITE),
                textIgnorePlacement(true),
                textAllowOverlap(true)
            )

            map?.style?.removeLayer(COUNT_LAYER)
            map?.style?.addLayer(count)
        }

    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            presenter.updateLocationPermission(true)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun getCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.CAMERA
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            presenter.updateLocationPermission(true)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                PERMISSIONS_REQUEST_ACCESS_CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        presenter.updateLocationPermission(false)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    presenter.updateLocationPermission(true)
                }else{
                    val alert = AlertDialog.Builder(this)
                        .setTitle("Предоставьте разрешение")
                        .setMessage("Приложение не может корректно рабоатать без этого разрешения")
                        .create()
                    alert.show()
                }
            }

            PERMISSIONS_REQUEST_ACCESS_CAMERA -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent(this, DrivingsActivity::class.java)
                    intent.putExtra("aim", DrivingsStartTarget.QRandCode)

                    startActivity(intent)
                }else{
                    val alert = AlertDialog.Builder(this)
                        .setTitle("Предоставьте разрешение")
                        .setMessage("Приложение не может корректно рабоатать без этого разрешения")
                        .create()
                    alert.show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.onStartEmitted()
    }

    override fun initLocationRelationships() {
        runOnUiThread {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                        if (it != null) {
                            val latLng = LatLng(it.latitude, it.longitude)
                            presenter.position = latLng

                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0))

                            map?.style?.removeLayer(MY_MARKER_LAYER)
                            map?.style?.removeSource(MY_MARKER_SOURCE)

                            map?.style?.addSource(
                                GeoJsonSource(
                                    MY_MARKER_SOURCE,
                                    presenter.makeFeatureFromLatLng(latLng)
                                )
                            )
                            map?.style?.addLayer(
                                SymbolLayer(MY_MARKER_LAYER, MY_MARKER_SOURCE)
                                    .withProperties(
                                        iconImage(MY_MARKER_IMAGE),
                                        iconAllowOverlap(true),
                                        iconIgnorePlacement(true)
                                    )
                            )


                        }
                    }
                } catch (e: Exception) {
                    Log.w("map? location error", e.localizedMessage)
                }
            }
        }
    }

    override fun drawRoute(polyline: String) {
        runOnUiThread {
            map?.style?.removeLayer(ROUTE_LAYER)
            map?.style?.removeSource(ROUTE_SOURCE)

            val source = GeoJsonSource(ROUTE_SOURCE)
            source.setGeoJson(LineString.fromPolyline(polyline, Constants.PRECISION_6))
            map?.style?.addSource(source)

            map?.style?.addLayerBelow(
                LineLayer(ROUTE_LAYER, ROUTE_SOURCE)
                    .withProperties(
                        lineCap(Property.LINE_CAP_ROUND),
                        lineJoin(Property.LINE_JOIN_ROUND),
                        lineWidth(5f),
                        lineColor(Color.parseColor("#0565D7"))
                    ),
                SCOOTERS_LAYER
            )
        }
    }

    override fun showScooterCard(scooter: Scooter, status: OrderStatus) {
        runOnUiThread {
            if (currentShowingScooter != scooter.id) {
                binding.contentOfMap.mapScooterItem.constraintLayoutItemScooterParent.visibility =
                    View.VISIBLE
                binding.contentOfMap.mapScooterItem.cardViewScooterItem.visibility = View.VISIBLE

                binding.contentOfMap.mapScooterItem.cardViewScooterItem.linnearLayoutScooterItemFinishButtons.visibility =
                    View.GONE
                binding.contentOfMap.mapScooterItem.cardViewScooterItem.linnearLayoutScooterItemBookingButtons.visibility =
                    View.GONE
                binding.contentOfMap.mapScooterItem.cardViewScooterItem.linnearLayoutScooterItemRentButtons.visibility =
                    View.GONE
                binding.contentOfMap.mapScooterItem.cardViewScooterItem.linnearLayoutScooterItemFirstBookButtons.visibility =
                    View.GONE

                binding.contentOfMap.mapScooterItem.cardViewScooterItem.constraintLayoutScooterItemPopupRoute.visibility =
                    View.GONE
                binding.contentOfMap.mapScooterItem.cardViewScooterItem.constraintLayoutScooterItemPopupLock.visibility =
                    View.GONE
                binding.contentOfMap.mapScooterItem.cardViewScooterItem.constraintLayoutScooterItemPopupSignal.visibility =
                    View.GONE

                binding.contentOfMap.mapScooterItem.cardViewScooterItem.linnearLayoutScooterItemFirstBookButtons.visibility =
                    View.VISIBLE

                when (status) {
                    OrderStatus.CANDIDIATE -> {
                        binding.contentOfMap.mapScooterItem.textViewItemScooterStateLabel.visibility =
                            View.GONE
                        binding.contentOfMap.mapScooterItem.textViewItemScooterStateValue.visibility =
                            View.GONE

                        binding.contentOfMap.mapScooterItem.cardViewScooterItem.constraintLayoutScooterItemPopupRouteBackgroundless.visibility =
                            View.VISIBLE
                        binding.contentOfMap.mapScooterItem.cardViewScooterItem.constraintLayoutScooterItemPopupRouteBackgroundless.clipToOutline =
                            true
                        binding.contentOfMap.mapScooterItem.cardViewScooterItem.constraintLayoutScooterItemPopupRouteBackgroundless.imageView2Backgroundless.clipToOutline =
                            true

                        binding.contentOfMap.mapScooterItem.cardViewScooterItem.textViewItemScooterMinutePricing.text =
                            ""
                        binding.contentOfMap.mapScooterItem.cardViewScooterItem.textViewtextViewItemScooterHourPricing.text =
                            ""


                        val scooterPercentage = scooter.getBatteryPercentage()
                        val scooterInfo = scooter.getScooterRideInfo()

                        val spannable: Spannable =
                            SpannableString("$scooterPercentage $scooterInfo")

                        spannable.setSpan(
                            ForegroundColorSpan(Color.BLACK),
                            0,
                            scooterPercentage.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        spannable.setSpan(
                            ForegroundColorSpan(Color.GRAY),
                            scooterPercentage.length,
                            "$scooterPercentage $scooterInfo".length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        binding.contentOfMap.mapScooterItem.cardViewScooterItem.textViewItemScooterId.text =
                            "#${scooter.id}"
                        binding.contentOfMap.mapScooterItem.cardViewScooterItem.textViewItemScooterBatteryPercent.text =
                            spannable

                        currentShowingScooter = scooter.id

                        //TODO: remake activate button

                        findViewById<Button>(R.id.buttonItemScooterFirstActivate).setOnClickListener {
                            presenter.clickedOnBookButton(scooter.id)

                            currentShowingScooter = -1
                        }

                        findViewById<Button>(R.id.buttonItemScooterBookFirst).setOnClickListener {
                            presenter.clickedOnBookButton(scooter.id)

                            currentShowingScooter = -1
                        }


                    }

                    else -> presenter.sendToTheDrivingsList()
                }
            }
        }
    }

    override fun setRateForScooterCard(rate: Rate, scooterId: Long) {
        runOnUiThread {
            if (currentShowingScooter == scooterId) {
                val perMinuteLabel = getString(R.string.scooter_per_minute)
                val spannableMinute: Spannable =
                    SpannableString("${rate.minute}₽ $perMinuteLabel")

                spannableMinute.setSpan(
                    ForegroundColorSpan(Color.BLACK),
                    0,
                    rate.minute.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannableMinute.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    rate.minute.length,
                    "${rate.minute}₽ $perMinuteLabel".length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )


                val perHourLabel = getString(R.string.scooter_per_hour)
                val spannableHours: Spannable = SpannableString("${rate.hour}₽ $perHourLabel")

                spannableHours.setSpan(
                    ForegroundColorSpan(Color.BLACK),
                    0,
                    rate.hour.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannableHours.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    rate.hour.length,
                    "${rate.hour}₽ $perHourLabel".length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding.contentOfMap.mapScooterItem.linearLayoutScooterItemInfoTextView.textViewItemScooterMinutePricing.text =
                    spannableMinute
                binding.contentOfMap.mapScooterItem.linearLayoutScooterItemInfoTextView.textViewtextViewItemScooterHourPricing.text =
                    spannableHours
            }
        }
    }

    override fun setDialogBy(type: MapDialogType) {
        runOnUiThread {
            when (type) {
                MapDialogType.NO_MONEY_FOR_START ->
                    AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_balance)
                        .setMessage(R.string.dialog_balance_data)
                        .setNegativeButton(R.string.dialog_cancel) { dialogInterface: DialogInterface, _ ->
                            presenter.cancelDialog(
                                MapDialogType.NO_MONEY_FOR_START
                            ); dialogInterface.dismiss()
                        }
                        .setPositiveButton(R.string.dialog_add) { dialogInterface: DialogInterface, _ -> presenter.purchaseToBalance(); dialogInterface.dismiss() }
                        .create()
                        .show()

                MapDialogType.BANNED_FOR_BOOKING ->
                    AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_attention)
                        .setMessage(R.string.dialog_block_book)
                        .setNegativeButton(R.string.dialog_ok) { dialogInterface: DialogInterface, _ ->
                            presenter.cancelDialog(
                                MapDialogType.BANNED_FOR_BOOKING
                            ); dialogInterface.dismiss()
                        }
                        .create()
                        .show()
            }
        }
    }

    override fun showToast(text: String) {
        runOnUiThread {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun setLoading(by: Boolean) {
        runOnUiThread {
            binding.contentOfMap.progressBarMap.visibility = if (by) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initPopupMapView(orders: List<Order>, bookCount: Int, rentCount: Int) {
        runOnUiThread {
            disposableJobsBag.forEach{
                it.cancel()
            }
            disposableJobsBag.clear()

            if (bookCount == 0 && rentCount == 0) {
                binding.contentOfMap.mapPopupItem.constraintLayoutParentPopupMap.visibility =
                    View.GONE
            } else {
                binding.contentOfMap.mapPopupItem.constraintLayoutParentPopupMap.visibility =
                    View.VISIBLE

                if (bookCount > 0 && rentCount == 0 || bookCount == 0 && rentCount > 0) {
                    binding.contentOfMap.mapPopupItem.textViewPopupMenuDownBordered.visibility =
                        View.GONE
                    binding.contentOfMap.mapPopupItem.textViewPopupMenuDownValue.visibility =
                        View.GONE

                    binding.contentOfMap.mapPopupItem.spacerPopupMap1.visibility = View.GONE
                    binding.contentOfMap.mapPopupItem.spacerPopupMap2.visibility = View.GONE

                    if (bookCount == 1) {
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuUpBordered.text =
                            "${getString(R.string.map_book)} 1"

                        val bookOrder = orders.first { it.status == OrderStatus.BOOKED.value }

                        disposableJobsBag.add(
                            GlobalScope.launch {
                                val orderTime = bookOrder.parseStartTime()

                                while (true) {

                                    if (orderTime != null) {
                                        val time =
                                            LocalDateTime.now().atZone(ZoneId.systemDefault())
                                                .toInstant().toEpochMilli() - orderTime
                                        val rawMinutes = TimeUnit.MILLISECONDS.toMinutes(time)

                                        val hours = rawMinutes / 60
                                        val minutes = rawMinutes % 60
                                        val seconds = time / 1000 - minutes * 60 - hours * 3600

                                        runOnUiThread {
                                            binding.contentOfMap.mapPopupItem.textViewPopupMenuUpValue.text =
                                                String.format(
                                                    "%d:%02d:%02d",
                                                    hours,
                                                    minutes,
                                                    seconds
                                                )
                                        }
                                    }
                                    delay(1000)

                                }
                                //Server check
                            }
                        )
                    } else if (bookCount > 1) {
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuUpBordered.text =
                            "${getString(R.string.map_book)} $bookCount"
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuUpValue.text =
                            orders.filter { it.status == OrderStatus.BOOKED.value }.map { it.cost }
                                .sum().toString() + " ₽"
                    }

                    if (rentCount >= 1) {
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuUpBordered.text =
                            "${getString(R.string.map_rent)} $rentCount"
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuUpValue.text =
                            orders.filter { it.status == OrderStatus.ACTIVATED.value }
                                .map { it.cost }
                                .sum().toString() + " ₽"
                    }
                } else {
                    binding.contentOfMap.mapPopupItem.textViewPopupMenuDownBordered.visibility =
                        View.VISIBLE
                    binding.contentOfMap.mapPopupItem.textViewPopupMenuDownValue.visibility =
                        View.VISIBLE

                    binding.contentOfMap.mapPopupItem.spacerPopupMap1.visibility = View.VISIBLE
                    binding.contentOfMap.mapPopupItem.spacerPopupMap2.visibility = View.VISIBLE

                    if (bookCount == 1) {
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuUpBordered.text =
                            "${getString(R.string.map_book)} 1"

                        val bookOrder = orders.first { it.status == OrderStatus.BOOKED.value }

                        disposableJobsBag.add(
                            GlobalScope.launch {
                                val orderTime = bookOrder.parseStartTime()

                                while (true) {

                                    if (orderTime != null) {
                                        val time =
                                            LocalDateTime.now().atZone(ZoneId.systemDefault())
                                                .toInstant().toEpochMilli() - orderTime
                                        val rawMinutes = TimeUnit.MILLISECONDS.toMinutes(time)

                                        val hours = rawMinutes / 60
                                        val minutes = rawMinutes % 60
                                        val seconds = time / 1000 - minutes * 60 - hours * 3600

                                        runOnUiThread {
                                            binding.contentOfMap.mapPopupItem.textViewPopupMenuUpValue.text =
                                                String.format(
                                                    "%d:%02d:%02d",
                                                    hours,
                                                    minutes,
                                                    seconds
                                                )
                                        }
                                    }
                                    delay(1000)

                                }
                                //Server check
                            }
                        )
                    } else if (bookCount > 1) {
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuUpBordered.text =
                            "${getString(R.string.map_book)} $bookCount"
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuUpValue.text =
                            orders.filter { it.status == OrderStatus.BOOKED.value }.map { it.cost }
                                .sum().toString() + " ₽"
                    }

                    if (rentCount >= 1) {
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuDownBordered.text =
                            "${getString(R.string.map_rent)} $rentCount"
                        binding.contentOfMap.mapPopupItem.textViewPopupMenuDownValue.text =
                            orders.filter { it.status == OrderStatus.ACTIVATED.value }
                                .map { it.cost }
                                .sum().toString() + " ₽"
                    }
                }
            }
        }
    }

    override fun drawGeoZones(feauters: ArrayList<Feature>) {
        runOnUiThread {
            map?.style?.removeLayer(GEOZONE_LAYER)
            map?.style?.removeLayer(PARKING_LAYER)
            map?.style?.removeLayer(PARKING_BONUS_LAYER)
            map?.style?.removeLayer(PARKING_SHTRAF_LAYER)

            map?.style?.removeSource(GEOZONE_SOURCE)
            map?.style?.removeSource(PARKING_SOURCE)
            map?.style?.removeSource(PARKING_BONUS_SOURCE)
            map?.style?.removeSource(PARKING_SHTRAF_SOURCE)

            val geozoneJsonSource = GeoJsonSource(
                GEOZONE_SOURCE,
                FeatureCollection.fromFeatures(
                    feauters.filter { it.getStringProperty("geoType") == GEOZONE_LABEL }
                ),
                GeoJsonOptions()
            )

            val parkingJsonSource = GeoJsonSource(
                PARKING_SOURCE,
                FeatureCollection.fromFeatures(
                    feauters.filter { it.getStringProperty("geoType") == PARKING_LABEL }
                ),
                GeoJsonOptions()
            )

            val parkingBonusJsonSource = GeoJsonSource(
                PARKING_BONUS_SOURCE,
                FeatureCollection.fromFeatures(
                    feauters.filter { it.getStringProperty("geoType") == PARKING_BONUS_LABEL }
                ),
                GeoJsonOptions()
            )

            val parkingShtrafJsonSource = GeoJsonSource(
                PARKING_SHTRAF_SOURCE,
                FeatureCollection.fromFeatures(
                    feauters.filter { it.getStringProperty("geoType") == PARKING_SHTRAF_LABEL }
                ),
                GeoJsonOptions()
            )

            map?.style?.addSource(geozoneJsonSource)
            map?.style?.addSource(parkingJsonSource)
            map?.style?.addSource(parkingBonusJsonSource)
            map?.style?.addSource(parkingShtrafJsonSource)

            val geoZoneLayer = FillLayer(GEOZONE_LAYER, GEOZONE_SOURCE)
            geoZoneLayer.setProperties(
                fillColor(TRANSPARENT_COLOR)
            )

            val parkingZoneLayer = FillLayer(PARKING_LAYER, PARKING_SOURCE)
            parkingZoneLayer.setProperties(
                fillColor(PARKING_LINE_COLOR),
                visibility(Property.NONE)
            )

            val parkingBonusZoneLayer = FillLayer(PARKING_BONUS_LAYER, PARKING_BONUS_SOURCE)
            parkingZoneLayer.setProperties(
                fillColor(PARKING_BONUS_COLOR),
                visibility(Property.NONE)
            )

            val parkingShtrafZoneLayer = FillLayer(PARKING_SHTRAF_LAYER, PARKING_SHTRAF_SOURCE)
            parkingZoneLayer.setProperties(
                fillColor(PARKING_SHTRAF_COLOR),
                visibility(Property.NONE)
            )


            map?.style?.addLayerAbove(geoZoneLayer, GEOZONE_BACKGROUND_LAYER)
            map?.style?.addLayerAbove(parkingZoneLayer, GEOZONE_BACKGROUND_LAYER)
            map?.style?.addLayerAbove(parkingBonusZoneLayer, GEOZONE_BACKGROUND_LAYER)
            map?.style?.addLayerAbove(parkingShtrafZoneLayer, GEOZONE_BACKGROUND_LAYER)

        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStartEmitted()
    }

    override fun sendToTheProfileActivity() {
        runOnUiThread {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun sendToPromoList() {
        runOnUiThread {
            val intent = Intent(this, PromoActivity::class.java)
            Log.d("active_activity_intent", "Promo")
            startActivity(intent)
        }
    }

    override fun sendToHelpActivity() {
        runOnUiThread {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun sendToDrivingsList() {
        runOnUiThread {
            val intent = Intent(this, DrivingsActivity::class.java)

            intent.putExtra("aim", DrivingsStartTarget.DrivingList)
            startActivity(intent)
        }
    }

    private fun bitmapIconFromVector(
        @DrawableRes vectorResId: Int
    ): Bitmap {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        val backgroundDrawable =
            ContextCompat.getDrawable(this, R.drawable.ic_purple_circle_with_white_corner)

        vectorDrawable!!.setBounds(
            (((kotlin.math.max(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            ) * 1.35).toInt() - vectorDrawable.intrinsicWidth * 1.125) / 1.125).toInt(),
            (((kotlin.math.max(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            ) * 1.35).toInt() - vectorDrawable.intrinsicHeight * 1.125) / 1.125).toInt(),
            (vectorDrawable.intrinsicWidth * 1.125 + 0.375 * (kotlin.math.max(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            ) * 1.35 - vectorDrawable.intrinsicWidth * 1.125) / 1.125).toInt(),
            (vectorDrawable.intrinsicHeight * 1.125 + 0.375 * (kotlin.math.max(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            ) * 1.35 - vectorDrawable.intrinsicHeight * 1.125) / 1.125).toInt()
        )
        backgroundDrawable!!.setBounds(
            0,
            0,
            (kotlin.math.max(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            ) * 1.35).toInt(),
            (kotlin.math.max(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            ) * 1.35).toInt()
        )

        val bitmap = Bitmap.createBitmap(
            (kotlin.math.max(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            ) * 1.35).toInt(),
            (kotlin.math.max(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            ) * 1.35).toInt(),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        backgroundDrawable.draw(canvas)
        vectorDrawable.draw(canvas)

        return Bitmap.createBitmap(bitmap)
    }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        var drawable = ContextCompat.getDrawable(this, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /* fun onResponse(call: Call<JsonObject?>?, response: Response<JsonObject>) {
         if (response.isSuccessful()) {
             try {
                 val jsonObject = JSONObject(Gson().toJson(response.body()))
                 msg = jsonObject.getString("msg")
                 status = jsonObject.getBoolean("status")
                 msg = jsonObject.getString("msg")
                 status = jsonObject.getBoolean("status")
             } catch (e: JSONException) {
                 e.printStackTrace()
             }
             Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
             Log.e("cvbnop", response.body().toString())
         } else {
             Toast.makeText(this@MainActivity, "Some error occurred...", Toast.LENGTH_LONG).show()
         }
     }

     */
    fun fetchData() {

        val requestQueue = Volley.newRequestQueue(this)
        Log.d(
            "user_response_url",
            "url = ${BASE_URL}getClient/?format=json&id=${
                getSharedPreferences(
                    "account",
                    Context.MODE_PRIVATE
                ).getLong("id", 0)
            }"
        )
        val request = JsonArrayRequest(
            Request.Method.GET,
            "${BASE_URL}getClient/?format=json&id=${
                getSharedPreferences(
                    "account",
                    MODE_PRIVATE
                ).getLong("id", 0)
            }",
            null,
            { response ->

                Log.d("user_response", "response = $response")


            },
            { error ->
                error.printStackTrace()
                Log.d("user_response", "err = ${error.message}")
            })
        requestQueue?.add(request)


    }

    /**************  Added by kon3gor   *******************/

    private var isParkingsVisible = false

    /**
     * Method for making "Powered by SOTA" string a spannable string
     */
    private fun changePBSTextColor(){
        val text = resources.getString(R.string.powered_by_sota)
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.qr_purple)),
            11,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.poweredBy.text = spannable

    }

    fun toggleParkings(view: View) {
        map?.getStyle {
            val pB = it.getLayer(PARKING_BONUS_LABEL)
            val p = it.getLayer(PARKING_LAYER)
            val pP = it.getLayer(PARKING_SHTRAF_LAYER)
            val count = it.getLayer(COUNT_LAYER)
            val scooter = it.getLayer(SCOOTERS_LAYER)
            val clusters = it.getLayer(CLUSTERS_LAYER)

            if (isParkingsVisible){
                pB?.setProperties(visibility(Property.NONE))
                p?.setProperties(visibility(Property.NONE))
                pP?.setProperties(visibility(Property.NONE))
                count?.setProperties(visibility(Property.VISIBLE))
                scooter?.setProperties(visibility(Property.VISIBLE))
                clusters?.setProperties(visibility(Property.VISIBLE))
            }else{
                pB?.setProperties(visibility(Property.VISIBLE))
                p?.setProperties(visibility(Property.VISIBLE))
                pP?.setProperties(visibility(Property.VISIBLE))
                count?.setProperties(visibility(Property.NONE))
                scooter?.setProperties(visibility(Property.NONE))
                clusters?.setProperties(visibility(Property.NONE))
            }

            isParkingsVisible = !isParkingsVisible
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 42 && resultCode == Activity.RESULT_OK){
            val id = data!!.extras!!.getLong("scooter_id")
            presenter.clickedOnScooterWith(id)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**************  Added by kon3gor   *******************/

    override fun onBackPressed() {}

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99
        const val PERMISSIONS_REQUEST_ACCESS_CAMERA = 100
        const val MAX_CLUSTER_ZOOM_LEVEL = 14
        const val CLUSTER_RADIUS = 50
        const val MY_MARKER_SOURCE = "my-marker-source"
        const val MY_MARKER_LAYER = "my-marker-layer"
        const val MY_MARKER_IMAGE = "my-marker-image"
        const val SCOOTER_ICON_SOURCE = "scooter-image"
        const val CIRCLES_LAYER = "circles"
        const val CLUSTERS_LAYER = "clusters"
        const val SCOOTERS_SOURCE = "scooters"
        const val SCOOTERS_LAYER = "scooters-layer"
        const val SCOOTERS_ICON_THIRD = "scooter-third"
        const val SCOOTERS_ICON_SECOND = "scooter-second"
        const val SCOOTERS_ICON_FIRST = "scooter-first"
        const val SCOOTERS_ICON_CHOSEN = "scooter-chosen"
        const val COUNT_LAYER = "count"
        const val ROUTE_LAYER = "route"
        const val ROUTE_SOURCE = "route-source"
        const val GEOZONE_BACKGROUND_LAYER = "geozone-background"
        const val GEOZONE_BACKGROUND_SOURCE = "geozone-background-source"
        const val GEOZONE_LAYER = "geozone"
        const val GEOZONE_SOURCE = "geozone-source"
        const val PARKING_LAYER = "parking"
        const val PARKING_SOURCE = "parking-source"
        const val PARKING_BONUS_LAYER = "parking-bonus"
        const val PARKING_BONUS_SOURCE = "parking-bonus-source"

        const val PARKING_SHTRAF_LAYER = "parking-shtraf"
        const val PARKING_SHTRAF_SOURCE = "parking-shtraf-source"

        val GEOZONE_COLOR = Color.parseColor("#40FF453A")
        val GEOZONE_LINE_COLOR = Color.parseColor("#FF453A")
        val PARKING_LINE_COLOR = Color.parseColor("#402F80ED")
        val PARKING_BONUS_COLOR = Color.parseColor("#4014D53D")
        val PARKING_SHTRAF_COLOR = Color.parseColor("#402F80ED")

        val GEOZONE_LABEL = "Allowed"
        val PARKING_LABEL = "Parking"
        val PARKING_BONUS_LABEL = "BParking"
        val PARKING_SHTRAF_LABEL = "PParking"

        val TRANSPARENT_COLOR = Color.parseColor("#40FFFFFF")

        val MAX_BOOK_TIME = 900000L

        const val SCOOTER_ROUTE_LAYER = "scooter-route-layer"
        const val SCOOTER_ROUTE_SOURCE = "scooter-route-source"
    }


}

enum class MapDialogType {
    NO_MONEY_FOR_START, BANNED_FOR_BOOKING
}