package com.cloneUser.client.ut

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class GradientPolyLine(
    private val map: GoogleMap,
    private val job: CompletableJob
) : CoroutineScope {

    private var startColor = 0

    private var endColor = 0

    private var delayTime = 10L

    private var polylineOptions = PolylineOptions()
        .width(10F)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    /**
     * @param polylineOptions that the integrator wants to use it to draw the gradient on
     * the [GoogleMap]
     */
    fun setPolylineOptions(polylineOptions: PolylineOptions): GradientPolyLine {
        this.polylineOptions = polylineOptions
        return this
    }

    /**
     * @param delayTime that the integrator wants to see when the class is going to draw the
     * gradient on [GoogleMap]
     */
    fun setDelayTime(delayTime: Long): GradientPolyLine {
        this.delayTime = delayTime
        return this
    }

    /**
     * @param startColor of the gradient since any gradient has at least start and end color to
     * draw the gradient line.
     */
    fun setStartColor(startColor: Int): GradientPolyLine {
        this.startColor = startColor
        return this
    }

    /**
     * @param endColor of the gradient since any gradient has at least start and end color to
     * draw the gradient line.
     */
    fun setEndColor(endColor: Int): GradientPolyLine {
        this.endColor = endColor
        return this
    }

    /**
     * Our main function that responsible for drawing the gradient on [GoogleMap] polyline by
     * making some calculation for generating the gradient line from the start and end color.
     *
     * @param latLngRouteList is the route coordinates list that we will use to draw the gradient
     * polyline on [GoogleMap]
     * @param onDrawFinished is a delegate/callback that will be called once the gradient polyline
     * is drawn successfully on [GoogleMap]
     */
    var ready = true

    fun drawPolyline(latLngRouteList: List<LatLng>, onDrawFinished: () -> (Unit)) {

        val gradientPoly: ArrayList<PolylineOptions> = ArrayList()

        /**
         * Extract the RGD colors from the [startColor]
         */
        val startRed = Color.red(startColor)
        val startGreen = Color.green(startColor)
        val startBlue = Color.blue(startColor)

        /**
         * Extract the RGD colors from the [endColor]
         */
        val endRed = Color.red(endColor)
        val endGreen = Color.green(endColor)
        val endBlue = Color.blue(endColor)

        if(ready){
            ready =false
            launch {

                /**
                 * PolyUtil.simplify() function is a utility function that returned
                 * simplified LatLng array with the same criteria and the same behaviour of
                 * the original array just for making the drawing of the points much faster
                 * in time and in processing.
                 *
                 * https://developers.google.com/maps/documentation/android-sdk/utility
                 */

                var simplifiedList: List<LatLng> = ArrayList()
                if (latLngRouteList.isNotEmpty())
                    simplifiedList = PolyUtil.simplify(latLngRouteList, 15.0)
                val simplifiedListSize = simplifiedList.size.toFloat()

                /**
                 * Calculate the steps for each RGB color.
                 */
                val redSteps = (endRed - startRed).toFloat() / 255 / simplifiedListSize
                val greenSteps = (endGreen - startGreen).toFloat() / 255 / simplifiedListSize
                val blueSteps = (endBlue - startBlue).toFloat() / 255 / simplifiedListSize

                val builder = LatLngBounds.Builder()

                /**
                 * Iterate over the simplifiedList to include each point on the predefined
                 * LatLngBounds builder and to start extract for each point its gradient color point.
                 */
                for (index in 0 until simplifiedList.size - 1) {

                    builder.include(simplifiedList[index])

                    /**
                     * Generate the RGB colors for gradient by getting each start RGB color and
                     * dividing them by 255. Then getting the result and plus it to
                     * the multiplication of the RGB step colors with the for loop index of
                     * the simplifiedList.
                     */
                    val redGradientColor = (startRed.toFloat() / 255) + (redSteps * index)
                    val greenGradientColor = (startGreen.toFloat() / 255) + (greenSteps * index)
                    val blueGradientColor = (startBlue.toFloat() / 255) + (blueSteps * index)

                    /**
                     * Then generate the full color.
                     */
                    val gradientColor = getRGBColor(
                        red = redGradientColor,
                        green = greenGradientColor,
                        blue = blueGradientColor
                    )

                    /**
                     * And add it to the gradientPoly array of [PolylineOptions]
                     */
                    gradientPoly.add(
                        copyPolylineOptions(polylineOptions)
                            .color(gradientColor)
                            .add(simplifiedList[index])
                            .add(simplifiedList[index + 1])
                    )

                }

                /**
                 * Then add each [polylineOptions] that are stored in the gradientPoly on the
                 * [map] with the delay that the integrator set.
                 */
                withContext(Dispatchers.Main) {
                    setZoomingOnMap(false)
                    gradientPoly.forEach { polylineOption ->
                        map.addPolyline(polylineOption)
                        delay(delayTime)
                    }
                    setZoomingOnMap(true)
                    onDrawFinished()
                }
                ready = true
            }

        }

    }

    /**
     * getRGBColor() is the same function as [Color.rgb].
     * I execrate it here because it is added in API level 26.
     *
     * @param red component of the color [0..1].
     * @param green component of the color [0..1].
     * @param blue component of the color [0..1].
     *
     * @return a color-int from red, green, blue float components in the range [0..1].
     */
    private fun getRGBColor(red: Float, green: Float, blue: Float): Int {
        return -0x1000000 or
                ((red * 255.0f + 0.5f).toInt() shl 16) or
                ((green * 255.0f + 0.5f).toInt() shl 8) or
                (blue * 255.0f + 0.5f).toInt()
    }

    /**
     * Create an object from the [PolylineOptions] that the integrator set to be added in the
     * gradientPoly array.
     *
     * @param originPolylineOptions that the integrator to be added in the
     * gradientPoly array.
     *
     * @return the copy instance from the [originPolylineOptions].
     */
    private fun copyPolylineOptions(originPolylineOptions: PolylineOptions): PolylineOptions {
        with(originPolylineOptions) {
            val copyPolylineOptions = PolylineOptions()
            copyPolylineOptions.width(width)
            copyPolylineOptions.color(color)
            copyPolylineOptions.zIndex(zIndex)
            copyPolylineOptions.visible(isVisible)
            copyPolylineOptions.geodesic(isGeodesic)
            copyPolylineOptions.clickable(isClickable)
            copyPolylineOptions.startCap(startCap)
            copyPolylineOptions.endCap(endCap)
            copyPolylineOptions.jointType(jointType)
            copyPolylineOptions.pattern(pattern)
            return copyPolylineOptions
        }

    }

    /**
     * It is an optional method that makes the user interaction with the [map] is disabled
     * until the gradient is drawn successfully on the [map] and then all the user interaction
     * will return to be enabled.
     *
     * @param isEnabled to set the user interaction on the [map] from disable to enable
     * and vice versa.
     */
    private fun setZoomingOnMap(isEnabled: Boolean) {
        map.uiSettings.apply {
            isScrollGesturesEnabled = isEnabled
            isScrollGesturesEnabledDuringRotateOrZoom = isEnabled
            isZoomGesturesEnabled = isEnabled
        }

    }

}