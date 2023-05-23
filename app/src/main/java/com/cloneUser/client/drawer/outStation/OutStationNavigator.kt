package com.cloneUser.client.drawer.outStation

import android.content.Context
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.OutStationTypes
import com.google.android.gms.maps.GoogleMap

interface OutStationNavigator : BaseViewOperator {
    fun getCtx():Context?
    fun getMap():GoogleMap
    fun gotoSearchPlace()
    fun goSelectOutStation()
    fun typesSelected(types: OutStationTypes)
    fun loadTypesAdapter(types:MutableList<OutStationTypes>)
    fun openScheduleBottomSheet(type:Boolean)
    fun openPromoDialog()
    fun openSuccess()
    fun toCallAdmin()
    fun showOutstationEta()
    fun showConfirmation()
    fun setCurrentTime()
}