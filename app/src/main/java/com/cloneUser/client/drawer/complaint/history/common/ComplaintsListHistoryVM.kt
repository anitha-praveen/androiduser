package com.cloneUser.client.drawer.complaint.history.common

import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class ComplaintsListHistoryVM @Inject constructor(
    val session: SessionMaintainence,
    connection: ConnectionHelper
) : BaseVM<BaseResponse, ComplaintsListHistoryNav>(session, connection) {
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.postValue(false)
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.postValue(false)
    }
}