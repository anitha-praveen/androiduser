package com.cloneUser.client.drawer.suggestion

import android.content.Context
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.ComplaintsModel
import com.cloneUser.client.connection.responseModels.SuggestionModel

interface SuggestionNavigator : BaseViewOperator {
    fun addItems(data:ArrayList<SuggestionModel>?)
    fun setSelected(data: SuggestionModel)
    fun getCtx():Context
}