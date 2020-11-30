package com.example.mapmarkeranimation

import android.animation.FloatEvaluator
import android.animation.TypeEvaluator

class AnchorEvaluator : TypeEvaluator<FloatArray> {
    private val evaluator = FloatEvaluator()

    override fun evaluate(fraction: Float, xAnchor: FloatArray, yAnchor: FloatArray): FloatArray {
        if (xAnchor.isEmpty()) error("xAnchor must contain 2 or more elements")
        if (yAnchor.isEmpty()) error("yAnchor must contain 2 or more elements")

        return floatArrayOf(
            evaluator.evaluate(fraction, xAnchor[0], xAnchor[1]) as Float,
            evaluator.evaluate(fraction, yAnchor[0], yAnchor[1]) as Float
        )
    }
}