package com.cloneUser.client.drawer.rating

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.BaseResponse

interface RatingNavigator : BaseViewOperator {
    fun goToHome()
    fun loadList(list:ArrayList<BaseResponse.InvoiceQuestionsList>)
    fun updateRating(questionsList: BaseResponse.InvoiceQuestionsList,list:ArrayList<BaseResponse.InvoiceQuestionsList>)
}