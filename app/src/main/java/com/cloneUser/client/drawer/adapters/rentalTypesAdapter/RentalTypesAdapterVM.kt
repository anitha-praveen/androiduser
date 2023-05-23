package com.cloneUser.client.drawer.adapters.rentalTypesAdapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.RentalPackageTypes
import com.bumptech.glide.Glide
import java.util.*

class RentalTypesAdapterVM(
    var rentalType: RentalPackageTypes.Type, adapterLister: RentalTypesAdapterItemListener
) {
    var adapterlister: RentalTypesAdapterItemListener = adapterLister
    var isSelected  = ObservableBoolean(rentalType.isSelected)
    var imageURL = ObservableField(if(isSelected.get()) rentalType.vehicle?.highlightImage?:"" else rentalType.vehicle?.image?:"" )
    var typeName = ObservableField(rentalType.vehicle?.vehicleName?:"")
    var eta = ObservableField(rentalType.eta)
    var cost = ObservableField(rentalType.totalAmount)
    fun onItemSelected() {
        adapterlister.itemSelected(rentalType)
    }

    val map: HashMap<String, String>?
        get() = null

    interface RentalTypesAdapterItemListener : BaseViewOperator {
        fun itemSelected(type: RentalPackageTypes.Type)
    }
}

@BindingAdapter("rentalTypesImage")
fun setImageUrl(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url).into(imageView)
}