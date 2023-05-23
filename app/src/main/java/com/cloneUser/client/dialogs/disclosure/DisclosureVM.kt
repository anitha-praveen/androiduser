package com.cloneUser.client.dialogs.disclosure

import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class DisclosureVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) : BaseVM<BaseResponse, DisclosureNavigator>(session, mConnect) {

    var heading = ObservableField("Welcome to Roda")
    var subHeading = ObservableField("Have a hassle-free booking experience by giving us permission to get your location (for finding available rides)")
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
    }
    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun onClickAllow(){
        getNavigator().requestPermission()
    }


}