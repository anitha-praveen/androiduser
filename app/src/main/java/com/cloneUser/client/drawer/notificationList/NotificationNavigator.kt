package com.cloneUser.client.drawer.notificationList

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.NotificationData


interface NotificationNavigator : BaseViewOperator {
    fun addList(list: ArrayList<NotificationData>)
    fun stopLoader()
    fun mentionLastPage()
    fun startSimmer()
    fun stopSimmer()
}