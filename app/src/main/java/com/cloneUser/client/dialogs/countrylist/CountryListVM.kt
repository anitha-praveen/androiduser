package com.cloneUser.client.dialogs.countrylist

import androidx.databinding.ObservableBoolean
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class CountryListVM  @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) : BaseVM<BaseResponse, CountryListNavigator>(session, mConnect) {
    var showNoItem = ObservableBoolean(false)
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
    }
    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }


   fun onClickBack(){
        getNavigator().close()
   }

}