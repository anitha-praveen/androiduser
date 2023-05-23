package com.cloneUser.client.drawer.adapters.recent_loc_adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.ut.SessionMaintainence
import java.util.HashMap

class ChildRecentLocVM (
    var lastTrip: FavPlace.LastTripHistory, adapterLister: RecentLocItemListener
) {
    var place: ObservableField<String> = ObservableField(lastTrip.dropAddress)
    var adapterlister: RecentLocItemListener = adapterLister

    /** favourite icon tapped on places search result  */
    fun onItemSelected() {
        adapterlister.itemSelected(lastTrip)
    }

    fun onclickHeart(){
        adapterlister.heartClicked(lastTrip)
    }

    val map: HashMap<String, String>?
        get() = null

    /** interface for places search result  */
    interface RecentLocItemListener : BaseViewOperator {
        fun itemSelected(lastTrip: FavPlace.LastTripHistory)
        fun heartClicked(lastTrip: FavPlace.LastTripHistory)
    }

}

@BindingAdapter("android:src")
fun setImageViewResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}