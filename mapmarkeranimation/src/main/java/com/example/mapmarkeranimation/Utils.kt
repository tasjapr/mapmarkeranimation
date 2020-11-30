package com.example.mapmarkeranimation

import android.content.Context
import android.util.TypedValue

fun dpToPx(dpValue: Int, context: Context?): Int {
    val displayMetrics = context?.resources?.displayMetrics?: return 0
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dpValue.toFloat(),
        displayMetrics).toInt()
}