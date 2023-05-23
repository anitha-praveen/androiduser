package com.cloneUser.client.drawer.trip

import android.app.Activity
import android.content.Context
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.CancelReason
import com.cloneUser.client.connection.responseModels.RequestData

interface TripNavigator : BaseViewOperator {
    fun getCtx(): Context
    fun isAddedInAct(): Boolean
    fun invoice(data: RequestData.Data)
    fun cancelReasonDialog(reasonList: MutableList<CancelReason.Reason>)
    fun showTripCancelled()
    fun getAct(): Activity
    fun openSideMenu()
    fun close()
    fun goToSearchPlace(mode: String)
    fun openApproveAlert()
    fun openReqProgrss()
    fun openSos()
    fun showTakePhotoDialog()
}