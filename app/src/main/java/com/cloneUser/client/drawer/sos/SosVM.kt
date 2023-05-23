package com.cloneUser.client.drawer.sos

import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.SosModel
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class SosVM  @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, SosNavigator>(session, mConnect) {
    var apiCode = -1

    val mode = ObservableField(0)
    /*
    apiCode 0 -> get sos list
            1 -> save sos
     */
    var hashMap = HashMap<String,String>()

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if(apiCode == 0  && response!!.data!= null){
            val sosModel: SosModel =
                Gson().fromJson(Gson().toJson(response.data), SosModel::class.java)
            if (sosModel.sos!!.isNotEmpty()) {
                getNavigator().addList(sosModel.sos)
            }
        }else if(apiCode ==1 || apiCode==2){
            getSosList()
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun onClickAdd(){
        getNavigator().showSosAdd()
    }

    fun getSosList(){
        if(getNavigator().isNetworkConnected()){
            apiCode = 0
            getSOSListBase()
        }else
            getNavigator().showNetworkUnAvailable()
    }

    fun saveSos(name: String , phone : String){
        if(getNavigator().isNetworkConnected()){
            apiCode =1
            hashMap.clear()
            hashMap[Config.title] = name
            hashMap[Config.phone_number] = phone
            saveSosBase(hashMap)
        }
    }

    fun deleteSos(slug : String){
        if(getNavigator().isNetworkConnected()){
            apiCode = 2
            deleteSosBase(slug)
        }
    }

}