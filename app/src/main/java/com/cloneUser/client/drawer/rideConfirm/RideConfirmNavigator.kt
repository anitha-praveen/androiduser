package com.cloneUser.client.drawer.rideConfirm

import android.app.Activity
import android.content.Context
import androidx.navigation.NavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.connection.responseModels.TypesModel
import com.cloneUser.client.databinding.FragmentRideConfirmationBinding
import kotlinx.coroutines.CompletableJob

interface
RideConfirmNavigator : BaseViewOperator{
    fun addList(types : List< TypesModel.ZoneTypePrice>?)
    fun showSelectedPriceDetails()
    fun getCtx() : Context
    fun getAct(): Activity?
    fun isAddedInAct() : Boolean
    fun openPromoDialog()
    fun bind() : FragmentRideConfirmationBinding
    fun selectedSlug(zoneTypePrice: TypesModel.ZoneTypePrice)
    fun openPaymentMethodDialog(paymentTypes: MutableList<String>)
    fun getGMap() : GoogleMap?
    fun openWaitingProgressDialog(reqId : String)
    fun getNavControl() : NavController
    fun openTripPage(data : RequestData.Data)
    fun openScheduleBottomSheet()
    fun navigateToMap()
    fun onClickBack()
    fun setETA()
    fun getJob() : CompletableJob
    fun runProgressBar()
    fun stopProgressBar()
    fun showErrorDialog()
    fun getMarkerIcon(drawable: Int): BitmapDescriptor?
    fun closeFragment()
    fun openCallBottomSheet()
    fun showApiWaiting()
    fun closeApiWaiting()
    fun openSuccess()
    fun gotoSearchplace()
    fun goToNodriverFound()
    fun openRiderSelection()
}