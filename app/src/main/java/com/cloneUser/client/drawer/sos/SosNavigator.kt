package com.cloneUser.client.drawer.sos

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.SosModel

interface SosNavigator : BaseViewOperator {
    fun onPhoneClick(number : String)
    fun addList(sos : List<SosModel.Sos>?)
    fun showSosAdd()
    fun deleteSosNav(slug : String)

}