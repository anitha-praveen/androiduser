package com.cloneUser.client.drawer.myRides.completed

import androidx.databinding.ObservableBoolean
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.HistoryModel
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class CompletedHistoryListVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, CompletedHistoryNavigator>(session, mConnect) {
    var hashMap = HashMap<String, String>()
    var previousPage = 1

    var currentPage: Int = 1
    var totalPages = 1000
    var isLastPage = false
    var noItemFound = ObservableBoolean(true)

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        getNavigator().stopSimmer()
        if (response != null) {
            val historyModel: HistoryModel =
                Gson().fromJson(Gson().toJson(response.data), HistoryModel::class.java)
            if (historyModel.data!!.size > 0) {
                noItemFound.set(false)
                if (currentPage > previousPage) {
                    getNavigator().stopLoader()
                }
                historyModel.lastPage?.let {
                    totalPages = it
                }
                getNavigator().loadData(historyModel.data)
            } else {
                historyModel.lastPage?.let {
                    if (currentPage >= it)
                        getNavigator().mentionLastPage()
                }
            }
        }

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        getNavigator().stopSimmer()
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun getHistoryList() {
        if (getNavigator().isNetworkConnected()) {
            getNavigator().startSimmer()
            hashMap.clear()
            hashMap[Config.ride_type] = "RIDE NOW"
            hashMap[Config.trip_type] = "COMPLETED"
            getHistoryListBase(hashMap, "$currentPage")
        }
    }

}