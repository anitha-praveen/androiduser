package com.cloneUser.client.drawer.notificationList

import android.app.Application
import androidx.databinding.ObservableBoolean
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.NotificationData
import com.cloneUser.client.connection.responseModels.NotificationsModel
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject
import kotlin.collections.ArrayList

class NotificationVM @Inject constructor(
    val application: Application,
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, NotificationNavigator>(session, mConnect) {
    var notificationList = ArrayList<NotificationData>()
    var isLastPage = false
    var totalPages = 1000
    var currentPage = 1
    var previousPage = 1
    val showShimmer = ObservableBoolean(false)
    val showNoNotificaionTxt = ObservableBoolean(false)

    override fun onSuccessfulApi(response: BaseResponse?) {
      //  isLoading.value = false
        if (response != null) {
            val notificationResponse: NotificationsModel =
                Gson().fromJson(Gson().toJson(response.data), NotificationsModel::class.java)
            if (notificationResponse.data!!.size > 0) {
                if (currentPage > previousPage) {
                    getNavigator().stopLoader()
                }
                totalPages = notificationResponse.lastPage!!
                notificationList.clear()
                notificationList.addAll(notificationResponse.data!!)
                getNavigator().stopLoader()
                getNavigator().stopSimmer()
                getNavigator().addList(notificationList)

            } else
                if (currentPage >= notificationResponse.lastPage!!) {
                    getNavigator().stopSimmer()
                    getNavigator().mentionLastPage()
                }
            if (notificationList.size == 0)
                showNoNotificaionTxt.set(true)
            else
                showNoNotificaionTxt.set(false)
        }

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
       // isLoading.value = false
        showShimmer.set(false)
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun getApi() {
        getNotifactionBase(currentPage.toString())
    }
}