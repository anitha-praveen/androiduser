package com.cloneUser.client.dialogs.waitingProgress

import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class WaitingProgressVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) : BaseVM<BaseResponse, WaitingProgressNavigator>(session, mConnect) {

    var hashMap = HashMap<String,String>()
    var requestId = ""
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if(response!!.success==true)
            getNavigator().close()
    }
    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun cancelRequest(){
        if(getNavigator().isNetworkConnected()){
            hashMap.clear()
            hashMap[Config.request_id] = requestId
            cancelRequestBase(hashMap)
        }else
            getNavigator().showNetworkUnAvailable()
    }


}