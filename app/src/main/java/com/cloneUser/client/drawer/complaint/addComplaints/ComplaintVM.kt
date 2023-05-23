package com.cloneUser.client.drawer.complaint.addComplaints

import android.view.View
import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.ComplaintsModel
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import retrofit2.Call
import javax.inject.Inject

class ComplaintVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, ComplaintNavigator>(session, mConnect) {

    var map = HashMap<String, String>()
    var text_cmts_comp = ObservableField("")
    var selectedSlug = ""
    var list: ArrayList<ComplaintsModel>? = ArrayList()
    var apiCode = -1
    var mode = 0
    var reqId = ""

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        Utilz.dismissProgress()
        if (apiCode == 1 || apiCode == 2 && response!!.data != null) {
            val data = Gson().fromJson(Gson().toJson(response?.data), BaseResponse::class.java)
            list!!.clear()
            list!!.addAll(data!!.complaint!!)
            getNavigator().addItems(list)
        } else {
            text_cmts_comp.set("")
            translationModel.txt_complaint_added_success?.let { getNavigator().showMessage(it) }
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        Utilz.dismissProgress()
        getNavigator().showCustomDialog(e.exception!!)
    }


    /** called when any of the complaint is selected from the spinner  */


    fun OnlickSend(view: View) {

        if (selectedSlug.isNotEmpty() && text_cmts_comp.get().toString()
                .isNotEmpty() && text_cmts_comp.get()?.trim()?.length != 0
        ) {
            apiCode = 0
            map.clear()
            map[Config.complaint_id] = selectedSlug
            map[Config.answer] = text_cmts_comp.get().toString()
            if (mode == 1)
                map[Config.request_id] = reqId
            submitSuggestionBase(map,getNavigator().getCtx())
        } else if (selectedSlug.isEmpty()) {
            getNavigator().showMessage(translationModel.text_Please_Select_an_Option ?: "")
        } else if (text_cmts_comp.get().isNullOrEmpty()) {
            getNavigator().showMessage(translationModel.noComplaintsError)
        }

    }

    fun getComplaintsList() {
        apiCode = 1
        getComplaintsListBase()
    }

    fun getComplaintsListOfTrip() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 2
            getTripComplaintsListBase()
        } else {
            getNavigator().showNetworkUnAvailable()
        }
    }


}