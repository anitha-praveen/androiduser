package com.cloneUser.client.loginOrSignup.getStarteedScrn

import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class GetStartedScreenVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, GetStartedScreenNavigator>(session, mConnect) {
    var selectedLangCode = ObservableField("")
    val gson = Gson()
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (response!!.data != null) {
            session.saveString(SessionMaintainence.TRANSLATED_DATA, gson.toJson(response.data!!))
            getNavigator().initiateNavigation()

        }
    }

    /*  Calling selected language api */
    fun onClickSetLanguage() {
        if(selectedLangCode.get()!!.isEmpty()){
            getNavigator().showMessage("Select any language")
        }else{
            getSelectedLangBase(selectedLangCode.get()!!)
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }


}