package com.cloneUser.client.drawer.complaint.history.suggestionHistory

import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.SuggestionHistoryModel
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class SuggestionHistoryVM @Inject constructor(
    val session: SessionMaintainence,
    connection: ConnectionHelper
) : BaseVM<BaseResponse, SuggestionHistoryNav>(session, connection) {
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.postValue(false)
        response?.let { res ->
            val data = Gson().fromJson(Gson().toJson(res.data), SuggestionHistoryModel::class.java)
            data.suggestion?.let { getNavigator().setData(it) }
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.postValue(false)
    }

    fun getRegisteredSug() {
        if (getNavigator().isNetworkConnected())
            suggestionHistoryBase()
        else
            getNavigator().showNetworkUnAvailable()
    }
}