package com.example.mapmarkeranimation

import android.animation.ValueAnimator
import com.google.android.gms.maps.model.Marker

object AnchorAnimation {

    fun animate(
        sourceMarker: Marker,
        destinationMarker: Marker,
        sourceAnchors: Pair<FloatArray, FloatArray>,
        destinationAnchors: Pair<FloatArray, FloatArray>
    ) {
        ValueAnimator.ofObject(AnchorEvaluator(), sourceAnchors.first, sourceAnchors.second).apply {
            addUpdateListener {
                val animatedValues = it.animatedValue as FloatArray
                sourceMarker.setAnchor(animatedValues[0], animatedValues[1])
            }
            start()
        }

        ValueAnimator.ofObject(
            AnchorEvaluator(),
            destinationAnchors.first,
            destinationAnchors.second
        ).apply {
            addUpdateListener {
                val animatedValues = it.animatedValue as FloatArray
                destinationMarker.setAnchor(animatedValues[0], animatedValues[1])
            }
            start()
        }
    }
}