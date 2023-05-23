package com.cloneUser.client.drawer.adapters.rentalPackageAdapter

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.PackageModel
import java.util.*

class RentalPackageAdapterVM(
    var pkg: PackageModel.Package, adapterLister: RentalPackageAdapterListener
) {
    var adapterlister: RentalPackageAdapterListener = adapterLister
    var isSelected = ObservableBoolean(pkg.isSelected)
    var hrs = ObservableField("${pkg.hours} ${pkg.timeCostType}")
    var kms = ObservableField("${pkg.km} KM")

    fun onItemSelected() {
        adapterlister.itemSelected(pkg)
    }

    val map: HashMap<String, String>?
        get() = null

    interface RentalPackageAdapterListener : BaseViewOperator {
        fun itemSelected(pkg: PackageModel.Package)
    }
}