package com.cloneUser.client.drawer.myRides.cancelled

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.HistoryModel
import com.cloneUser.client.connection.responseModels.RequestData

interface CancelledHistoryNavigator : BaseViewOperator {
        fun loadData(data: MutableList<HistoryModel.History>)
        fun invoice(data : RequestData.Data)
        fun startSimmer()
        fun stopSimmer()
        fun showText()
        fun hideText()
        fun getTranslation() : TranslationModel
        fun stopLoader()
        fun mentionLastPage()
}