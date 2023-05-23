package com.cloneUser.client.drawer.outStation.listOutStation

import android.text.Editable
import androidx.databinding.ObservableBoolean
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.SessionMaintainence
import com.google.gson.Gson
import retrofit2.Call
import javax.inject.Inject

class OutStationListVm @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, OutStationListNavigator>(session, mConnect) {


    val showNoResultsFound = ObservableBoolean(false)
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (response != null && response.data != null) {
            val json = Gson().toJson(response)
            val obj = Gson().fromJson(json, BaseResponse.DataObjectsAllApi::class.java)
            getNavigator().loadList(obj.outStationModel)
        }

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showMessage(e.exception!!)
    }

    fun getOutstaionApi() {
        getOutstationListApiBase()
    }

    fun onSearch(editable: Editable) {
        getNavigator().filterList(editable)
    }

}