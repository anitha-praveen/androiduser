package com.cloneUser.client.drawer.complaint.addComplaints

import android.content.Context
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.ComplaintsModel

interface ComplaintNavigator : BaseViewOperator {
    fun addItems(data:ArrayList<ComplaintsModel>?)
    fun setSelected(data:ComplaintsModel)
    fun getCtx():Context
}