package com.cloneUser.client.drawer.myRides.scheduled

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.HistoryModel
import com.cloneUser.client.connection.responseModels.RequestData

interface ScheduledHistoryNavigator : BaseViewOperator {
    fun loadData(data: MutableList<HistoryModel.History>)
    fun invoice(data : RequestData.Data)
    fun startSimmer()
    fun stopSimmer()
    fun getTranslation() : TranslationModel
    fun stopLoader()
    fun mentionLastPage()
    fun cancelTrip(reqId : String)
    fun refresh()
}