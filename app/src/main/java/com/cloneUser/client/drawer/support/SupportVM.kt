package com.cloneUser.client.drawer.support

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.AdminContact
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class SupportVM @Inject constructor(
    val application: Application,
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, SupportNavigator>(session, mConnect) {

    var customerCareNumber = ObservableField("")
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        val res = Gson().fromJson(Gson().toJson(response),AdminContact::class.java)
        customerCareNumber.set(res?.data?.customerCareNumber)
    }
    
    fun openComplaints(){
        getNavigator().complaintsHistory()
    }
    fun openSOS(){
        getNavigator().sos()
    }
    fun openFAQ(){
        getNavigator().faq()
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }


    fun onclickCall(view : View){
        if(!customerCareNumber.get().isNullOrEmpty()){
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:${customerCareNumber.get()}")
            view.context.startActivity(callIntent)

        }
    }
}