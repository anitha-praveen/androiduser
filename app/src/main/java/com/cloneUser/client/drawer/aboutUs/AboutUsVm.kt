package com.cloneUser.client.drawer.aboutUs

import android.view.View
import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.WalletResponsModel
import com.cloneUser.client.drawer.wallet.WalletNavigator
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class AboutUsVm  @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, AboutUsNavigator>(session, mConnect) {

    val appVersion = ObservableField("")

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false

    }
    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showMessage(e.exception!!)
    }

    fun onFbClicked(view:View){
        getNavigator().openFb()
    }
    fun onRateClikced(view:View){
        getNavigator().openPlayStore()
    }
    fun onLegalClick(view:View){
        getNavigator().openRodaWeb()
    }

}