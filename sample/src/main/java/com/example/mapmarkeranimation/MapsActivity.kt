package com.example.mapmarkeranimation

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private var startMarker: Marker? = null
    private var endMarker: Marker? = null

    private var startInfoWindow: Marker? = null
    private var endInfoWindow: Marker? = null
    private var origin = LatLng(45.036357, 38.978945)
    private var destination = LatLng(45.023510, 38.936913)
    private lateinit var sourceInfoView: View
    private lateinit var destinationInfoView: View

    private var sourceAnchorX = floatArrayOf()
    private var sourceAnchorY = floatArrayOf()
    private var destinationAnchorX = floatArrayOf()
    private var destinationAnchorY = floatArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        initMap()

        addSourcePoint(Place(origin, "Start point"))
        addEndPoint(Place(destination, "End point"))

        map.addPolyline(
            PolylineOptions()
                .add(
                    LatLng(45.036357, 38.978945),
                    LatLng(45.033931, 38.977965),
                    LatLng(45.034908, 38.971678),
                    LatLng(45.036326, 38.963431),
                    LatLng(45.037293, 38.957590),
                    LatLng(45.037967, 38.957314),
                    LatLng(45.039669, 38.957716),
                    LatLng(45.039978, 38.955613),
                    LatLng(45.037698, 38.954862),
                    LatLng(45.034413, 38.952208),
                    LatLng(45.021643, 38.940342),
                    LatLng(45.023510, 38.936913)
                )
        )

        map.setOnMapLoadedCallback {
            zoomToPoints(listOf(origin, destination))
        }
    }


    private fun initMap() {
        map.run {
            setOnCameraIdleListener {

                val space =
                    resources.displayMetrics.widthPixels - resources.getDimensionPixelSize(R.dimen.info_window_space)
                val margin = resources.getDimensionPixelSize(R.dimen.horizontal_margin)

                AnchorMarkerAnimation.Builder()
                    .markers(
                        startInfoWindow ?: return@setOnCameraIdleListener,
                        endInfoWindow ?: return@setOnCameraIdleListener
                    )
                    .pickupPoints(
                        this.projection?.toScreenLocation(origin) ?: Point(),
                        this.projection?.toScreenLocation(destination) ?: Point()
                    )
                    .horizontalSpace(space)
                    .verticalSpace(space)
                    .windowSize(
                        listOf(sourceInfoView.width, sourceInfoView.height),
                        listOf(destinationInfoView.width, destinationInfoView.height)
                    )
                    .setMargin(margin)
                    .sourceMarkerAnchors(sourceAnchorX, sourceAnchorY)
                    .destinationMarkerAnchors(destinationAnchorX, destinationAnchorY)
                    .build()
            }
        }
    }

    private fun addSourcePoint(point: Place) {
        origin = LatLng(point.location.latitude, point.location.longitude)
        sourceInfoView = LayoutInflater.from(baseContext).inflate(R.layout.info_window, null)

        with(sourceInfoView) {
            findViewById<TextView>(R.id.pointName).text = point.name

            //set maxWidth of infoView like 48% of deviceWidth, point_name_margin - space from end of textView to infoView end
            findViewById<TextView>(R.id.pointName).maxWidth =
                (resources.displayMetrics.widthPixels * 0.48 - resources.getDimensionPixelSize(R.dimen.point_name_margin)).toInt()
        }

        startMarker = map.addMarker(
            MarkerOptions()
                .icon(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_start_point, null)
                        ?.toBitmapDescriptor()
                )
                .anchor(0.5f, 0.5f)
                .snippet("start")
                .position(origin)
        )

        startInfoWindow = map.addMarker(
            MarkerOptions().position(origin)
                .zIndex(1f)
                .snippet("start")
                .icon(BitmapDescriptorFactory.fromBitmap(sourceInfoView.getBitmap()))
        )?.apply {
            // horizontal_margin - margin from center of marker position to infoView according to design.
            // Anchor value calculated in relation to the actual size of infoWindow. See MarkerOptions anchor documentation
            val calculatedAnchorX =
                resources.getDimensionPixelSize(R.dimen.horizontal_margin) / sourceInfoView.width.toFloat()
            val calculatedAnchorY =
                resources.getDimensionPixelSize(R.dimen.horizontal_margin) / sourceInfoView.height.toFloat()
            sourceAnchorX = floatArrayOf(-calculatedAnchorX, calculatedAnchorX + 1f)
            sourceAnchorY = floatArrayOf(-calculatedAnchorY, calculatedAnchorY + 1f)
            setAnchor(sourceAnchorX[0], sourceAnchorY[1])
            tag = floatArrayOf(sourceAnchorX[0], sourceAnchorY[1])
        }
    }

    private fun addEndPoint(point: Place) {
        destination = LatLng(point.location.latitude, point.location.longitude)
        destinationInfoView = LayoutInflater.from(baseContext).inflate(R.layout.info_window, null)

        with(destinationInfoView) {
            findViewById<TextView>(R.id.pointName).text = point.name

            //set maxWidth of infoView like 48% of deviceWidth, point_name_margin - space from end of textView to infoView end
            findViewById<TextView>(R.id.pointName).maxWidth =
                (resources.displayMetrics.widthPixels * 0.48 - resources.getDimensionPixelSize(R.dimen.point_name_margin)).toInt()
        }

        endMarker = map.addMarker(
            MarkerOptions()
                .icon(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_end_point, null)
                        ?.toBitmapDescriptor()
                )
                .anchor(0.5f, 0.5f)
                .snippet("end")
                .position(destination)
        )

        endInfoWindow = map.addMarker(
            MarkerOptions().position(destination)
                .zIndex(1f)
                .snippet("end")
                .icon(BitmapDescriptorFactory.fromBitmap(destinationInfoView.getBitmap()))
        )?.apply {
            // horizontal_margin - margin from center of marker position to infoView according to design.
            // Anchor value calculated in relation to the actual size of infoWindow. See MarkerOptions anchor documentation
            val calculatedAnchorX =
                resources.getDimensionPixelSize(R.dimen.horizontal_margin) / destinationInfoView.width.toFloat()
            val calculatedAnchorY =
                resources.getDimensionPixelSize(R.dimen.horizontal_margin) / destinationInfoView.height.toFloat()
            destinationAnchorX = floatArrayOf(-calculatedAnchorX, calculatedAnchorX + 1f)
            destinationAnchorY = floatArrayOf(-calculatedAnchorY, calculatedAnchorY + 1f)
            setAnchor(destinationAnchorX[0], destinationAnchorY[1])
            tag = floatArrayOf(destinationAnchorX[0], destinationAnchorY[1])
        }
    }

    private fun zoomToPoints(points: List<LatLng>) {
        val boundsBuilder = LatLngBounds.Builder()
        points.forEach {
            boundsBuilder.include(it)
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
    }

}