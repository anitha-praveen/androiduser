package com.cloneUser.client.drawer.rideConfirm.adapter

import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace
import kotlin.math.pow
import kotlin.math.roundToInt

class RideChildSearchVm(
    var favPlace: FavPlace.Favourite, adapterLister: ChildSearchPlaceItemListener,
) {
    var place: ObservableField<String> = ObservableField(favPlace.address)
    var title: ObservableField<String> = ObservableField(favPlace.title)
    var adapterlister: ChildSearchPlaceItemListener = adapterLister
    /** favourite icon tapped on places search result  */
    fun onItemSelected() {
        adapterlister.itemSelected(favPlace)
    }


    val map: HashMap<String, String>?
        get() = null

    /** interface for places search result  */
    interface ChildSearchPlaceItemListener : BaseViewOperator {
        fun itemSelected(place: FavPlace.Favourite)
    }

    fun round(value: Double, precision: Int): Double {
        val scale = 10.0.pow(precision.toDouble()).toInt()
        return (value * scale).roundToInt().toDouble() / scale
    }
}
