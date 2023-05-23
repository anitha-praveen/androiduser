package com.cloneUser.client.drawer.faq

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.FaqModel

interface FaqNavigator:BaseViewOperator {
    fun setFaqAdapter(list: ArrayList<FaqModel>)

}