package com.cloneUser.client.drawer.rental

import android.app.Activity
import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.PackageModel
import com.cloneUser.client.connection.responseModels.RentalPackageTypes
import com.cloneUser.client.databinding.FragmentRentalBinding

interface RentalNavigator : BaseViewOperator{
    fun addList(packages : List< PackageModel.Package>)
    fun addSelectedPackageDetails(type : List<RentalPackageTypes.Type>)
    fun setSelectedPackage(pkg : PackageModel.Package)
    fun setSelectedType(type : RentalPackageTypes.Type,slug:String?)
    fun pickupAddressChange()
    fun getAct(): Activity?
    fun getGMap() : GoogleMap?
    fun setETA()
    fun openPromoDialog()
    fun openScheduleBottomSheet()
    fun runProgressBar()
    fun stopProgressBar()
    fun openSuccess()
    fun getBind() : FragmentRentalBinding
    fun getCtx():Context
    fun showConfirmAlert()
    fun showRentalEta()
}