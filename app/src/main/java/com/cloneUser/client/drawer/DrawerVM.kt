package com.cloneUser.client.drawer

import android.content.Intent
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.localbroadcastmanager.content.LocalBroadcastManager
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.regions.Region
//import com.amazonaws.services.s3.AmazonS3
//import com.amazonaws.services.s3.AmazonS3Client
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.RequestInProgress
import com.cloneUser.client.connection.responseModels.UserModel
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.SocketHelper
import kotlinx.coroutines.*
import retrofit2.Call
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class DrawerVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) : BaseVM<BaseResponse, DrawerNavigator>(session, mConnect),
    SocketHelper.Companion.SocketListener {
    companion object {
        const val TAG = "DrawerVM"
    }

    var isRatingCalled = ObservableBoolean(false)
    var Phone = ObservableField("")
    var countryCode = ObservableField("")
    var firstName = ObservableField("")
    var map = HashMap<String, String>()
    lateinit var requestInProgress: RequestInProgress
    val gson = Gson()
    var showProfilePic = ObservableBoolean(true)
    var versionCode = ObservableField("")


    /*
    apiCall value 0 -> logout api
                  1 -> requestInProgress

     */
    var apiCall = -1


    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCall == 1 && response!!.data != null) {
            requestInProgress =
                gson.fromJson(gson.toJson(response.data), RequestInProgress::class.java)
            requestInProgress.user?.let {
                setUpProfileDetails(it)
                socketInitiation()
                session.saveString(SessionMaintainence.SEARCH_RADIUS, it.searchRadius ?: "")
            }
            requestInProgress.trips?.let {
                when {
                    it.data?.isCompleted == 1 -> {
                        getNavigator().openInvoice(it.data)
                    }
                    it.data?.isDriverStarted == 1 -> {
                        Config.isReqCalled = true
                        getNavigator().openTrip(it.data)
                        it.data.id?.let { it1 ->
                            session.saveString(
                                SessionMaintainence.REQ_ID,
                                it1
                            )
                        }
                    }
                    it.data?.isDriverStarted == 0 -> {
                        val id = it.data.id
                        it.data.id?.let { i ->
                            getNavigator().goToNodriverFound(i)
                        }
                    }
                    else -> {}
                }
            }
        } else if (apiCall == 0) {
            logout()
        }

    }


    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        if (e.exception.equals("Token Expired")) {
            translationModel.txt_token_expired_alert?.let { getNavigator().showMessage(it) }
            logout()
        }
        else getNavigator().showCustomDialog(e.exception!!)
    }

    private fun setUpProfileDetails(user: UserModel.User) {
        session.saveString(SessionMaintainence.USER_ID, user.slug!!)
        firstName.set(user.firstname)
        Phone.set(user.phoneNumber)
        /*
         set image in side menu
        */

        user.profile_pic?.let {

            getNavigator().setImage(user.profile_pic)
        }
    }

//    private fun getImageFromS3(profilePic: String?) {
//        val viewModelJob = Job()
//        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
//
//        uiScope.launch {
//            withContext(Dispatchers.IO) {
//                val cal = Calendar.getInstance()
//                cal.time = Date()
//                cal.add(Calendar.HOUR, +1)
//                val oneHourLater: Date = cal.time
//                val s3: AmazonS3 = AmazonS3Client(
//                    BasicAWSCredentials(
//                        session.getString(SessionMaintainence.AWS_ACCESS_KEY_ID),
//                        session.getString(SessionMaintainence.AWS_SECRET_ACCESS_KEY)
//                    )
//                )
//                s3.setRegion(Region.getRegion(session.getString(SessionMaintainence.AWS_DEFAULT_REGION)))
//                val url: URL = s3.generatePresignedUrl(
//                    session.getString(SessionMaintainence.AWS_BUCKET),
//                    profilePic,
//                    oneHourLater
//                )
//                //setImage(url.toString())
//                //vm.refURL.set(url.toString())
//                withContext(Dispatchers.Main) {
//                    getNavigator().setImage(url.toString())
//                }
//            }
//        }
//
//    }


    fun onMenuClick() {
        getNavigator().openSideMenu()
    }


    fun requestInProgress() {
        if (getNavigator().isNetworkConnected()) {
            apiCall = 1
            requestInProgressBase()
        } else
            getNavigator().showNetworkUnAvailable()
    }

    private fun logout() {
        getNavigator().logoutNav()
    }

    private fun socketInitiation() {
        if (SocketHelper.mSocket == null)
            SocketHelper.init(session, this, TAG)
        else {
            SocketHelper.disconnectSocket()
            SocketHelper.init(session, this, TAG)
        }
    }

    override fun onConnect() {

    }

    override fun onDisconnect() {
    }

    override fun onConnectError() {
    }

    override fun isNetworkConnected(): Boolean {
        return getNavigator().isNetworkConnected()
    }

    var response: BaseResponse? = null
    override fun onRequest(request: String) {
        response = Gson().fromJson(request, BaseResponse::class.java)
        printE("OnRequest", "sucess_msg : " + response?.success_message)
        printE("OnRequest", request)
        when (response?.success_message) {
            "no_driver_found" -> {
                val id = response?.result?.data?.id
                val intent = Intent(Config.NO_DRIVER_FOUND)
                LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
                //getNavigator().showCustomDialog("No driver found")
                //getNavigator().showErrorDialog()
                if (id != null) {
                    getNavigator().goToNodriverFound(id)
                }
            }
            "trip_accepted" -> {
                getNavigator().getAct().runOnUiThread {
                    Config.isTripRefreshed = false
                    response?.result?.data?.let { getNavigator().openTrip(it) }
                }
            }
            "driver_arrived" -> {
                val intent = Intent(Config.TRIP_ARRIVED)
                response?.result?.data?.let {
                    val data: String = Gson().toJson(it)
                    intent.putExtra("TRIP_DATA", data)
                    LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
                }
            }
            "driver_started_the_trip" -> {
                val intent = Intent(Config.TRIP_STARTED)
                response?.result?.data?.let {
                    val data: String = Gson().toJson(it)
                    intent.putExtra("TRIP_DATA", data)
                    LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
                }
            }
            "driver_end_the_trip" -> {
                getNavigator().hideMeterDialog()
                val intent = Intent(Config.TRIP_COMPLETED)
                response?.result?.data?.let {
                    val data: String = Gson().toJson(it)
                    intent.putExtra("TRIP_DATA", data)
                    LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
                }
            }
            "request_cancelled_by_driver" -> {
                getNavigator().hideMeterDialog()
                val intent = Intent(Config.TRIP_CANCEL)
                response?.result?.data?.let {
                    val data: String = Gson().toJson(it)
                    intent.putExtra("TRIP_DATA", data)
                    LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
                }
            }
            "request_cancelled_by_dispatcher" -> {
                getNavigator().hideMeterDialog()
                val intent = Intent(Config.TRIP_CANCEL)
                response?.result?.data?.let {
                    val data: String = Gson().toJson(it)
                    intent.putExtra("TRIP_DATA", data)
                    LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
                }
            }
        }

    }

    override fun locationChanged(request: String) {
        Log.e("locChan", request)
        val intent = Intent(Config.ADDRESS_CHANGE)
        LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
    }

    override fun packageChanged(data: String) {
        val intent = Intent(Config.TRIP_EVENTS)
        response?.result?.data?.let {
            val data: String = Gson().toJson(it)
            intent.putExtra("TRIP_DATA", data)
            LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
        }
    }

    override fun photoUploaded(data: String) {
        Log.e("onPhotoUpload","vm: "+data)
        val mData = Gson().fromJson(data , BaseResponse::class.java)
        val intent = Intent(Config.DRIVER_NIGHT_IMAGE)
        mData.result?.uploadImageUrl?.let {intent.putExtra(Config.images,it)}
        mData.result?.uploadStatus?.let {intent.putExtra(Config.upload_status,it)}
        mData.result?.retake_image?.let { intent.putExtra(Config.retake_image,it) }
        LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
    }

    override fun skipNightPhoto(data: String) {
        val intent = Intent(Config.CLOSE_NIGHT_IMG)
        LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
    }

    override fun meterUploaded(data: String) {
        Log.e("meterUploaded","meterUploaded"+data)
        val mData = Gson().fromJson(data,BaseResponse::class.java)
        mData.result?.tripStartKm?.let { getNavigator().showMeterPic(it,mData.result?.trip_km?:"") }
    }


    fun getLogutApiVm() {
        apiCall = 0
        getLogOutApiBase()
    }

    fun checkZone() {
        apiCall = 2
    }

    fun close() {
        getNavigator().closeDrawer()
    }

    fun opensos() {
        getNavigator().openSos()
    }

    fun onClickProfile() {
        getNavigator().openProfile()
    }

}


