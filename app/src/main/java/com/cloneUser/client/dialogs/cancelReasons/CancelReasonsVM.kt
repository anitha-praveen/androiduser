package com.cloneUser.client.dialogs.cancelReasons

import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.CancelReason
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class CancelReasonsVM@Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) : BaseVM<BaseResponse, CancelReasonsNavigator>(session, mConnect) {
    var reason : CancelReason.Reason? = null
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun onclickDontCancel(){
        getNavigator().close()
    }

    fun onClickCancel(){
        if(reason != null){
            getNavigator().sendCancelReason(reason!!)

        }else
            getNavigator().showMessage("Please select any reason")
    }
}