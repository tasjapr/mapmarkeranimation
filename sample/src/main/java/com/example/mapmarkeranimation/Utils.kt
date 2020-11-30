package com.example.mapmarkeranimation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun Drawable.toBitmapDescriptor(): BitmapDescriptor {
    val canvas = Canvas()
    val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    canvas.setBitmap(bitmap)
    this.setBounds(0, 0, this.intrinsicWidth, this.intrinsicHeight)
    this.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun View.getBitmap(): Bitmap? {
    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight,
        Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    layout(0, 0, measuredWidth, measuredHeight)
    draw(canvas)
    return bitmap
}