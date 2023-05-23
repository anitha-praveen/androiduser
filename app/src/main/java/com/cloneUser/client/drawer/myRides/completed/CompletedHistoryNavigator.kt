package com.cloneUser.client.drawer.myRides.completed

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.HistoryModel
import com.cloneUser.client.connection.responseModels.RequestData

interface CompletedHistoryNavigator : BaseViewOperator{
    fun loadData(data: MutableList<HistoryModel.History>)
    fun invoice(data : RequestData.Data)
    fun startSimmer()
    fun stopSimmer()
    fun getTranslation() : TranslationModel
    fun stopLoader()
    fun mentionLastPage()
    fun openDispute(history: RequestData.Data)
}