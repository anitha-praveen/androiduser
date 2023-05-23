package com.cloneUser.client.drawer.suggestion

import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.SuggestionModel
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import java.util.HashMap
import javax.inject.Inject

class SuggestionVm @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, SuggestionNavigator>(session, mConnect) {

    /*
    api - 0  get suggestion list
    api - 1 post suggestion
     */
    var suggestionList: BaseResponse? = null
    var list: ArrayList<SuggestionModel>? = ArrayList()
    var text_cmts = ObservableField("")
    var selectedSlug = ""
    var apiCode = -1
    var map = HashMap<String, String>()
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCode == 0 && response!!.data != null) {
            suggestionList = Gson().fromJson(Gson().toJson(response.data), BaseResponse::class.java)
            if (suggestionList != null) {
                list!!.addAll(suggestionList!!.suggestion!!)
                getNavigator().addItems(list)
            }

        } else {
            text_cmts.set("")
            translationModel.txt_suggestion_added_success?.let { getNavigator().showMessage(it) }
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }


    fun getSuggestionApi() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 0
            getSuggestionListBase()
        } else
            getNavigator().showNetworkUnAvailable()
    }

    fun OnlickSend() {
        if (selectedSlug.isNotEmpty() && text_cmts.get().toString().isNotEmpty() && text_cmts.get()?.trim()?.length != 0) {
            apiCode = 1
            map.clear()
            map[Config.complaint_id] = selectedSlug
            map[Config.answer] = text_cmts.get().toString()
            submitSuggestionBase(map,getNavigator().getCtx())
        }else if(selectedSlug.isEmpty()){
            translationModel.text_no_suggestionType?.let { getNavigator().showMessage(it) }
        }else{
            translationModel.txt_suggestion_empty?.let { getNavigator().showMessage(it) }
        }
    }
}