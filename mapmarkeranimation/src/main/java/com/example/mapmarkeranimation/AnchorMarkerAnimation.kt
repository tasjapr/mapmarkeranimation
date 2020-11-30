package com.example.mapmarkeranimation

import android.graphics.Point
import android.graphics.Rect
import com.example.mapmarkeranimation.AnchorMarkerAnimation.Direction.*
import com.google.android.gms.maps.model.Marker

class AnchorMarkerAnimation private constructor() {

    data class Builder(
        private var sourceMarker: Marker? = null,
        private var destinationMarker: Marker? = null,
        private var pickupPoint: Point = Point(0, 0),
        private var pickupPointSecond: Point = Point(0, 0),
        private var horizontalSpace: Int = 0,
        private var verticalSpace: Int = 0,
        private var sourceViewSize: List<Int> = listOf(),
        private var destinationViewSize: List<Int> = listOf(),
        private var sourceAnchor: List<FloatArray> = listOf(),
        private var destinationAnchor: List<FloatArray> = listOf(),
        private var margin: Int = 0,

        private var sourceViewInfo: InfoViewData? = null,
        private var destinationViewInfo: InfoViewData? = null
    ) {
        fun markers(sourceMarker: Marker, destinationMarker: Marker) = apply {
            this.sourceMarker = sourceMarker
            this.destinationMarker = destinationMarker
        }

        /** @param source coordinates of point on the device screen, which corresponds to current position of source marker
         * @param destination coordinates of second info view point on the device screen which corresponds to current position of destination marker
         */
        fun pickupPoints(source: Point, destination: Point) = apply {
            this.pickupPoint = source
            this.pickupPointSecond = destination
        }

        /** @param sourceViewSize pair of width and height of source marker infoWindow
         *  @param destinationViewSize pair of width and height of destination marker infoWindow
         */
        fun windowSize(sourceViewSize: List<Int>, destinationViewSize: List<Int>) = apply {
            this.sourceViewSize = sourceViewSize
            this.destinationViewSize = destinationViewSize
        }

        /** @param horizontalSpace the max horizontal space for infoView rendering */
        fun horizontalSpace(horizontalSpace: Int) = apply { this.horizontalSpace = horizontalSpace }

        /** @param verticalSpace the max vertical space for infoView rendering */
        fun verticalSpace(verticalSpace: Int) = apply { this.verticalSpace = verticalSpace }

        /** @param x FloatArray with 2 elements - right and left values of source marker anchor
         *  @param y FloatArray with 2 elements - top and bottom values of source marker anchor
         */
        fun sourceMarkerAnchors(x: FloatArray, y: FloatArray) = apply {
            this.sourceAnchor = listOf(x, y)
        }

        /** @param x FloatArray with 2 elements - right and left values of destination marker anchor
         *  @param y FloatArray with 2 elements - top and bottom values of destination marker anchor
         */
        fun destinationMarkerAnchors(x: FloatArray, y: FloatArray) = apply {
            this.destinationAnchor = listOf(x, y)
        }

        /** @param margin - margin in dp between [pickupPoint] and nearest infoView corner */
        fun setMargin(margin: Int) = apply {
            this.margin = margin
        }

        private var p0: Pair<FloatArray, FloatArray> = Pair(floatArrayOf(), floatArrayOf())
        private var p1: Pair<FloatArray, FloatArray> = Pair(floatArrayOf(), floatArrayOf())

        private var sourceBounds = Rect()
        private var destBounds = Rect()

        /** Play [markers] anchor animation when theirs pickupPoints will be changed */
        fun build() =
            run {
                val source = sourceMarker ?: error("sourceMarker is required")
                val destination = destinationMarker ?: error("destinationMarker is required")


                sourceViewInfo = InfoViewData(
                    currentAnchor = source.tag as FloatArray,
                    anchorRange = sourceAnchor,
                    locationOnScreen = listOf(pickupPoint.x, pickupPoint.y),
                    size = sourceViewSize
                )
                destinationViewInfo = InfoViewData(
                    currentAnchor = destination.tag as FloatArray,
                    anchorRange = destinationAnchor,
                    locationOnScreen = listOf(pickupPointSecond.x, pickupPointSecond.y),
                    size = destinationViewSize
                )

                sourceBounds = getRect(sourceViewInfo as InfoViewData, margin)
                destBounds = getRect(destinationViewInfo as InfoViewData, margin)

                calcAnchor()
                AnchorAnimation.animate(source, destination, p0, p1)
                (sourceMarker as Marker).tag = floatArrayOf(p0.first[1], p0.second[1])
                (destinationMarker as Marker).tag = floatArrayOf(p1.first[1], p1.second[1])
            }


        private fun calcAnchor() {
            val v: InfoViewData = sourceViewInfo ?: InfoViewData()
            val ov: InfoViewData = destinationViewInfo ?: InfoViewData()

            val vUpdated = v.copy()
            val ovUpdated = ov.copy()

            var vCalc = Pair(preCalculation(v, X), preCalculation(v, Y))
            var ovCalc = Pair(preCalculation(ov, X), preCalculation(ov, Y))

            vUpdated.currentAnchor =
                floatArrayOf(preCalculation(vUpdated, X)[1], preCalculation(vUpdated, Y)[1])
            ovUpdated.currentAnchor =
                floatArrayOf(preCalculation(ovUpdated, X)[1], preCalculation(ovUpdated, Y)[1])

            // x=0 - right; y=0 - top
            var moved = false
            if (getRect(vUpdated, margin / 2).intersect(getRect(ovUpdated, margin / 2))) {
                canMoveToOppositeSide(vUpdated, margin).forEach {
                    when (it.key) {
                        TO_TOP ->
                            if (it.value) {
                                vCalc =
                                    Pair(
                                        vCalc.first,
                                        floatArrayOf(
                                            vUpdated.currentAnchor[Y],
                                            vUpdated.anchorRange[Y][1]
                                        )
                                    )
                                moved = true
                                return@forEach
                            }

                        TO_BOTTOM ->
                            if (it.value) {
                                vCalc =
                                    Pair(
                                        vCalc.first,
                                        floatArrayOf(
                                            vUpdated.currentAnchor[Y],
                                            vUpdated.anchorRange[Y][0]
                                        )
                                    )
                                moved = true
                                return@forEach
                            }

                        TO_LEFT ->
                            if (it.value) {
                                vCalc =
                                    Pair(
                                        floatArrayOf(
                                            vUpdated.currentAnchor[X],
                                            vUpdated.anchorRange[X][1]
                                        ), vCalc.second
                                    )
                                moved = true
                                return@forEach
                            }

                        TO_RIGHT ->
                            if (it.value) {
                                vCalc =
                                    Pair(
                                        floatArrayOf(
                                            vUpdated.currentAnchor[X],
                                            vUpdated.anchorRange[X][0]
                                        ), vCalc.second
                                    )
                                moved = true
                                return@forEach
                            }
                    }
                }
                if (!moved) canMoveToOppositeSide(ovUpdated, margin / 2).forEach {
                    when (it.key) {
                        TO_TOP ->
                            if (it.value) {
                                ovCalc =
                                    Pair(
                                        ovCalc.first,
                                        floatArrayOf(
                                            ovUpdated.currentAnchor[Y],
                                            ovUpdated.anchorRange[Y][1]
                                        )
                                    )
                                return@forEach
                            }

                        TO_BOTTOM ->
                            if (it.value) {
                                ovCalc =
                                    Pair(
                                        ovCalc.first,
                                        floatArrayOf(
                                            ovUpdated.currentAnchor[Y],
                                            ovUpdated.anchorRange[Y][0]
                                        )
                                    )
                                return@forEach
                            }
                        TO_LEFT ->
                            if (it.value) {
                                ovCalc =
                                    Pair(
                                        floatArrayOf(
                                            ovUpdated.currentAnchor[X],
                                            ovUpdated.anchorRange[X][1]
                                        ), ovCalc.second
                                    )
                                return@forEach
                            }
                        TO_RIGHT ->
                            if (it.value) {
                                ovCalc =
                                    Pair(
                                        floatArrayOf(
                                            ovUpdated.currentAnchor[X],
                                            ovUpdated.anchorRange[X][0]
                                        ), ovCalc.second
                                    )
                                return@forEach
                            }
                    }
                }
            }

            p0 = vCalc
            p1 = ovCalc
        }


        private fun preCalculation(v: InfoViewData, anchorType: Int): FloatArray {
            val unchangedAnchor = v.currentAnchor[anchorType]

            return when {
                unchangedAnchor == v.anchorRange[anchorType][1] && v.locationOnScreen[anchorType] < v.size[anchorType] + margin -> {
                    v.currentAnchor[anchorType] = v.anchorRange[anchorType][0]
                    floatArrayOf(unchangedAnchor, v.currentAnchor[anchorType])
                }
                unchangedAnchor == v.anchorRange[anchorType][1] -> floatArrayOf(
                    unchangedAnchor,
                    unchangedAnchor
                )

                v.locationOnScreen[anchorType] + v.size[anchorType] - margin > if (anchorType == X) horizontalSpace else verticalSpace -> {
                    v.currentAnchor[anchorType] = v.anchorRange[anchorType][1]
                    floatArrayOf(unchangedAnchor, v.currentAnchor[anchorType])
                }
                else -> {
                    v.currentAnchor[anchorType] = v.anchorRange[anchorType][0]
                    floatArrayOf(unchangedAnchor, v.currentAnchor[anchorType])
                }
            }
        }

        companion object {
            const val X = 0
            const val Y = 1
        }

        data class InfoViewData(
            var currentAnchor: FloatArray = floatArrayOf(),
            var anchorRange: List<FloatArray> = listOf(),
            var locationOnScreen: List<Int> = listOf(),
            var size: List<Int> = listOf()
        ) {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as InfoViewData

                if (!currentAnchor.contentEquals(other.currentAnchor)) return false

                return true
            }

            override fun hashCode(): Int {
                return currentAnchor.contentHashCode()
            }
        }

        //      Rect(left, top, right, bottom)
        private fun getRect(v: InfoViewData, margin: Int) =
            Rect().apply {
                if (v.currentAnchor[Y] == v.anchorRange[Y][1]) {
                    top = v.locationOnScreen[Y] - v.size[Y] - margin
                    bottom = v.locationOnScreen[Y] - margin
                } else {
                    top = v.locationOnScreen[Y] + margin
                    bottom = v.locationOnScreen[Y] + margin + v.size[Y]
                }
                if (v.currentAnchor[X] == v.anchorRange[X][1]) {
                    left = v.locationOnScreen[X] - margin - v.size[X]
                    right = v.locationOnScreen[X] - margin
                } else {
                    left = v.locationOnScreen[X] + margin
                    right = v.locationOnScreen[X] + margin + v.size[X]
                }
            }

        // x=0 - right; y=0 - top
        private fun canMoveToOppositeSide(
            v: InfoViewData,
            margin: Int
        ): MutableMap<Direction, Boolean> {
            val result = mutableMapOf<Direction, Boolean>()

            if (v.currentAnchor[X] == v.anchorRange[X][1]) result[TO_RIGHT] =
                v.locationOnScreen[X] + v.size[X] + margin <= horizontalSpace
            else result[TO_LEFT] = v.locationOnScreen[X] - v.size[X] - margin >= 0

            if (v.currentAnchor[Y] == v.anchorRange[Y][1]) result[TO_BOTTOM] =
                v.locationOnScreen[Y] + margin + v.size[Y] <= verticalSpace
            else result[TO_TOP] = v.locationOnScreen[Y] - v.size[Y] - margin >= 0

            return result
        }
    }

    enum class Direction {
        TO_LEFT,
        TO_RIGHT,
        TO_TOP,
        TO_BOTTOM
    }
}