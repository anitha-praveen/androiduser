package com.cloneUser.client.drawer.adapters.fav_adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace


class ChildFavoritePlaceVM(
    var favPlace: FavPlace.Favourite, adapterLister: ChildSearchPlaceItemListener, mode: Int
) {
    var place: ObservableField<String> = ObservableField(favPlace.address)
    var title: ObservableField<String> = ObservableField(favPlace.title)
    var adapterlister: ChildSearchPlaceItemListener = adapterLister
    var showDelete = ObservableField(mode == 1)

    /** favourite icon tapped on places search result  */
    fun onFavSelected() {
        adapterlister.itemSelected(favPlace)
    }

    fun deleteFavClicked() {
        adapterlister.itemDeleted(favPlace)
    }


    val map: HashMap<String, String>?
        get() = null

    /** interface for places search result  */
    interface ChildSearchPlaceItemListener : BaseViewOperator {
        fun itemSelected(favPlace: FavPlace.Favourite)
        fun itemDeleted(favPlace: FavPlace.Favourite)
    }

}

@BindingAdapter("android:src")
fun setImageViewResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}