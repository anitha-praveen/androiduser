package com.cloneUser.client.drawer.faq

import android.app.Application
import androidx.databinding.ObservableBoolean
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.FaqModel
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import java.util.ArrayList
import javax.inject.Inject

class FaqVm @Inject constructor(
    val application: Application,
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, FaqNavigator>(session, mConnect) {

    var list = ArrayList<FaqModel>()
    var isFaqAvailable = ObservableBoolean(true)
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (response!!.data != null) {
            val json = Gson().toJson(response.data)
            val obj = Gson().fromJson(json, BaseResponse::class.java)
            list.addAll(obj.faq!!)
            getNavigator().setFaqAdapter(list)
            isFaqAvailable.set(obj?.faq?.size!!>0)
        }else
            isFaqAvailable.set(false)


    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showMessage(e.exception!!)
    }

    fun getFaqApiVm() {
        getFaqApiBase()
    }


}