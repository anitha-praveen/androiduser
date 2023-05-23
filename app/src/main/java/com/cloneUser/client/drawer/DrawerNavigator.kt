package com.cloneUser.client.drawer

import android.app.Activity
import android.content.Context
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.RequestData

interface DrawerNavigator : BaseViewOperator {
    fun getCtx():Context
    fun setImage(imageURL : String)
    fun openSideMenu()
    fun logoutNav()
    fun openTrip(data : RequestData.Data)
    fun openInvoice(data: RequestData.Data)
    fun closeDrawer()
    fun openSos()
    fun getAct():Activity
    fun openProfile()
    fun showErrorDialog()
    fun goToNodriverFound(id:String)
    fun showMeterPic(url : String , string: String)
    fun hideMeterDialog()
}