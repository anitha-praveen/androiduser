package com.cloneUser.client.drawer.rideConfirm.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.TypesModel
import com.cloneUser.client.databinding.ChildTypesBinding

class ChildTypesVM(
    mBinding: ChildTypesBinding,
    types: TypesModel.ZoneTypePrice,
    adapterLister: ChildTypesItemListener
) {
    var zoneTypePrice: TypesModel.ZoneTypePrice = types
    var adapterlister: ChildTypesItemListener = adapterLister
    var IsSelected =
        ObservableBoolean(!(zoneTypePrice.isselected == null || zoneTypePrice.isselected == false))
    var name = ObservableField(zoneTypePrice.typeName)
    var etaTime = if(zoneTypePrice.eta.isNullOrEmpty()) ObservableField("N/A") else ObservableField(zoneTypePrice.eta)
    var carurl =
        ObservableField(if (IsSelected.get()) zoneTypePrice.typeImageSelect else zoneTypePrice.typeImage)
    var typeName = ObservableField(types.typeName ?: "")


    fun onItemSelected() {
        zoneTypePrice.isselected = true
        adapterlister.itemSelected(zoneTypePrice)
    }

    interface ChildTypesItemListener : BaseViewOperator {
        fun itemSelected(zoneTypePrice: TypesModel.ZoneTypePrice)
    }

}

@BindingAdapter("childcarimageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url).into(imageView)
}