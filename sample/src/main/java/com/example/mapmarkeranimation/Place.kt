package com.example.mapmarkeranimation

import com.google.android.gms.maps.model.LatLng

data class Place(
    val location: LatLng,
    val name: String
)