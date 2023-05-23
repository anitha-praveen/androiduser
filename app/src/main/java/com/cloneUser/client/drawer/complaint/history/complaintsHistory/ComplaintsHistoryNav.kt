package com.cloneUser.client.drawer.complaint.history.complaintsHistory

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.ComplaintHistoryModel

interface ComplaintsHistoryNav : BaseViewOperator {
    fun getTranslation() : TranslationModel
    fun setData(list: List<ComplaintHistoryModel.Complaints>)
}