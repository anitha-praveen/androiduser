package com.cloneUser.client.drawer.outStation.listOutStation

import android.text.Editable
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.OutstationModel

interface OutStationListNavigator : BaseViewOperator {
    fun placeSelected(outStationModel: OutstationModel)
    fun loadList(list: MutableList<OutstationModel>)
    fun filterList(string: Editable)
    fun showNoDataFound(show: Boolean)

}