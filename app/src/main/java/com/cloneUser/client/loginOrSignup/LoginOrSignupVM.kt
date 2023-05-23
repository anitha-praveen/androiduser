package com.cloneUser.client.loginOrSignup

import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class LoginOrSignupVM  @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
) :
    BaseVM<BaseResponse, LoginOrSignupNavigator>(session, mConnect ) {

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (response!!.data != null) {
            session.saveString(SessionMaintainence.TRANSLATED_DATA, Gson().toJson(response.data!!))
            session.saveBoolean(SessionMaintainence.FirstTimeEnglishCalled, true)
            session.saveString(SessionMaintainence.CURRENT_LANGUAGE,"en")
            getNavigator().initiateNavigation()
        }

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }


}