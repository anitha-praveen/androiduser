package com.cloneUser.client.drawer.complaint.history.complaintsHistory

import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.ComplaintHistoryModel
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class ComplaintsHistoryVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, ComplaintsHistoryNav>(session, mConnect) {


    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.postValue(false)
        response?.let { res ->
            val data = Gson().fromJson(Gson().toJson(res.data), ComplaintHistoryModel::class.java)
            data.complaints?.let { getNavigator().setData(it) }
        }

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false

    }

    fun getComplaints() {
        if (getNavigator().isNetworkConnected())
            complaintsHistoryBase()
        else
            getNavigator().showNetworkUnAvailable()
    }

}
