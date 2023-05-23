package com.cloneUser.client.base

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.gson.Gson
import com.cloneUser.client.R
import com.cloneUser.client.connection.*
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import com.cloneUser.client.ut.Utilz.convertToException
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.math.roundToInt


/**
 * This activity to be extended to All ViewModel
 * it acts as a Repository in MVVM Model
 * */
abstract class BaseVM<T : BaseResponse, N : BaseViewOperator> constructor(
    session: SessionMaintainence,
    private val mConnection: ConnectionHelper
) : ViewModel(), Basecallback<T> {
    lateinit var translationModel: TranslationModel
    var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    private var map: HashMap<String, String> = HashMap()
    private lateinit var mNavigator: N

    var requestbody = HashMap<String, RequestBody>()
    var body: MultipartBody.Part? = null

    init {
        map["Accept"] = "application/json"
        if (session.getString(SessionMaintainence.AccessToken) != null && session.getString(
                SessionMaintainence.TokenType
            ) != null
        )
            map["Authorization"] =
                session.getString(SessionMaintainence.TokenType) + " " + session.getString(
                    SessionMaintainence.AccessToken
                )
        if (session.getString(SessionMaintainence.CURRENT_LANGUAGE) != null)
            map["Content-Language"] = session.getString(SessionMaintainence.CURRENT_LANGUAGE) + ""
        if (!session.getString(SessionMaintainence.TRANSLATED_DATA).isNullOrEmpty())
            translationModel = Gson().fromJson(
                session.getString(SessionMaintainence.TRANSLATED_DATA),
                TranslationModel::class.java
            )
    }

    fun printE(tag: String, value: String) {
        Log.e(tag, value)
    }

    fun printD(tag: String, value: String) {
        Log.d(tag, value)
    }

    fun printV(tag: String, value: String) {
        Log.v(tag, value)
    }

    fun printI(tag: String, value: String) {
        Log.i(tag, value)
    }

    fun getLanguageBase(hashMap: HashMap<String, String>) {
        mConnection.getAvailableLanguageApi(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getSelectedLangBase(code: String) {
        isLoading.postValue(true)
        mConnection.getSelectedLangBaseApi(code).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun loginOrSignUpBase(hashMap: HashMap<String, String>, context: Context) {
        //isLoading.postValue(true)
        Utilz.showProgress(context)
        mConnection.signInBaseApi(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun sendCustomOtp(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.sendCustomOtp(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun signUpBase(context: Context) {
        Utilz.showProgress(context)
        mConnection.signUpBaseApi(map, requestbody)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun signUpWithImageBase(context: Context) {
        Utilz.showProgress(context)
        mConnection.signUpBaseWithImageApi(map, requestbody, body!!)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getTokenBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.getTokenBaseApi(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getProfileBase() {
        isLoading.postValue(true)
        mConnection.getProfileBaseApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun updateProfileBase() {
        isLoading.postValue(true)
        mConnection.updateProfileApi(map, requestbody)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun updateProfileImageBase() {
        isLoading.postValue(true)
        mConnection.updateProfileImageApi(map, body!!)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun checkPhoneNumberAvailabilityBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.checkPhoneNumberApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getFavoritePlaceBase() {
        isLoading.postValue(true)
        mConnection.getFavoritePlaceApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun saveFavoritePlaceBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.saveFavoritePlaceApi(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun deleteFavoritePlaceBase(slug: String) {
        isLoading.postValue(true)
        mConnection.deleteFavoriteApi(map, slug).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getVehicleTypeBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.getVehicleTypesApi(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getPromoListBase(type: String) {
        isLoading.postValue(true)
        mConnection.getPromoListApi(map,type).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun createRequestBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.createRequestApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun applyPromoBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.applyPromoApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getSOSListBase() {
        isLoading.postValue(true)
        mConnection.getSOSListApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun cancelRequestBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.cancelRequestApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun requestInProgressBase() {
        isLoading.postValue(true)
        mConnection.requestInProgressApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getCancelReasonsBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.getCancelReasonsApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }


    fun rateDriverBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.rateDriverApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getHistoryListBase(hashMap: HashMap<String, String>, pageNo: String) {
        /* shimmer layout for loading */
        mConnection.getHistoryListApi(map, hashMap, pageNo)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun saveSosBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.saveSosApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getSuggestionListBase() {
        isLoading.postValue(true)
        mConnection.getSuggestionListApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun submitSuggestionBase(hashMap: HashMap<String, String>,context: Context) {
      //      isLoading.postValue(true)
        Utilz.showProgress(context)
        mConnection.submitSuggestionApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getComplaintsListBase() {
        isLoading.postValue(true)
        mConnection.getComplaintsListApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getTripComplaintsListBase() {
        isLoading.postValue(true)
        mConnection.getTripComplaintsListApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }


    fun getLogOutApiBase() {
        isLoading.postValue(true)
        mConnection.getLogOutApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getFaqApiBase() {
        isLoading.postValue(true)
        mConnection.getFaqApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun deleteSosBase(id: String) {
        isLoading.postValue(true)
        mConnection.deleteSosApi(map, id).enqueue(baseCalback as Callback<BaseResponse>)
    }


    fun changeLocationBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.changeLocationApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getWalletListApiBase() {
        isLoading.postValue(true)
        mConnection.getWalletListApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun addWalletAmountBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.getWalletAddAmount(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getNotifactionBase() {
        isLoading.postValue(true)
        mConnection.getNotificationApi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getNotifactionBase(pageNo: String) {
        isLoading.postValue(true)
        mConnection.getNotificationApi(map, pageNo).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun checkZoneBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.getCheckZone(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getPostSelectedLanguage(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.getPostSelectedLanguageBase(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getReferralDetailsBase() {
        isLoading.postValue(true)
        mConnection.getReferralDetailsApi(map)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun updateErrorBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.updateErrorApi(map, hashMap).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getOutstationListApiBase() {
        isLoading.postValue(true)
        mConnection.getOutStationapi(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getOutStationTypesBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.getOutStationTypesApi(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }


    fun getRentalPackageListBase() {
        isLoading.postValue(true)
        mConnection.getRentalPackageListApi(map)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getSelectedPackageDetailsBase(hashMap: HashMap<String, String>, context: Context) {
        //Utilz.showProgress(context)
        isLoading.postValue(true)
        mConnection.getSelectedPackageDetailsApi(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getAdminContactBase() {
        isLoading.postValue(true)
        mConnection.getAdminContactApi(map)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getCheckOutstationApiBase(hashMap: HashMap<String, String>) {
        isLoading.postValue(true)
        mConnection.getcheckOutstationApi(map, hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun uploadNightImageBase() {
        isLoading.postValue(true)
        mConnection.updateNightImage(map, requestbody,body!!,)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun getRatingQuestionsBase(){
        isLoading.postValue(true)
        mConnection.getRatingQuestions(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun requestRetakeBase(hashMap: HashMap<String, String>){
        mConnection.requestRetakeApi(map,hashMap)
            .enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun suggestionHistoryBase(){
        mConnection.getSuggestionHistory(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    fun complaintsHistoryBase(){
        mConnection.getComplaintsHistory(map).enqueue(baseCalback as Callback<BaseResponse>)
    }

    private val baseCalback = object : Callback<T?> {
        override fun onFailure(call: Call<T?>, t: Throwable) {
            Log.e("MainActivity", "Problem calling Github API" + call.request().body)
            onFailureApi(call, CustomException(501, t.localizedMessage))
        }


        override fun onResponse(call: Call<T?>, response: Response<T?>) {
            response.isSuccessful.let {
                if (response.code() == 200)
                    onSuccessfulApi(response.body())
                else if (response.code() == 426) {
                    val errorMsg = CustomException(
                        response.code(),
                        "Update App"
                    )
                    onFailureApi(call, errorMsg)
                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                            Log.e("gree", "__" + jsonObj.getString("message"))
                            Log.e("greelkren", "__" + response.code())
                            if (jsonObj.has("data") && jsonObj.getJSONObject("data")
                                    .has("error_code")
                            ) {
                                val data = jsonObj.getJSONObject("data")
                                if (data.getInt("error_code") == 2001) {
                                    val reqId = data.getString("request_id")
                                    val errorcode = data.getInt("error_code")
                                    val errorMsg = CustomException(errorcode, reqId)
                                    onFailureApi(call, errorMsg)
                                }
                            } else if (response.code() == 401 || response.code() == 403) {
                                val errorMessage = jsonObj.getString("message")
                                val errorCode = response.code()
                                val erroMessage = CustomException(errorCode, errorMessage)
                                onFailureApi(call, erroMessage)

                            } else {
                                var errorMsg: CustomException? =
                                    convertToException(response.errorBody()!!, response.code())
                                if (errorMsg?.exception == null) {
                                    errorMsg = CustomException(response.code(), response.message())
                                }
                                onFailureApi(call, errorMsg)
                            }
                        } catch (e: Exception) {
                            var errorMsg: CustomException? =
                                convertToException(response.errorBody()!!, response.code())
                            if (errorMsg?.exception == null) {
                                errorMsg = CustomException(response.code(), response.message())
                            }
                            onFailureApi(call, errorMsg)
                        }


                    } else {
                        val errorMsg = CustomException(
                            response.code() ?: 0,
                            "Something went wrong. Please try again."
                        )
                        onFailureApi(call, errorMsg)
                    }
                }

            }
        }
    }

    fun setNavigator(navigator: N) {
        mNavigator = navigator
    }

    fun getNavigator() = mNavigator

    fun moveCamera(googleMap: GoogleMap, latLng: LatLng?, zoomLevel: Float) {
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(latLng!!, zoomLevel)
        )
    }


    open fun round(value: Double, precision: Int): Double {
        val scale = 10.0.pow(precision.toDouble()).toInt()
        return (value * scale).roundToInt().toDouble() / scale
    }

    fun changeMapStyle(googleMap: GoogleMap?, context: Context) {
        if (googleMap == null) return
        val style = MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json)
        googleMap.setMapStyle(style)
    }

    fun setLanguage(c: Context, lang: String) {
        val localeNew = Locale(lang)
        Locale.setDefault(localeNew)
        val res: Resources = c.resources
        val newConfig = Configuration(res.configuration)
        newConfig.setLocale(localeNew)
        newConfig.setLayoutDirection(localeNew)
        res.updateConfiguration(newConfig, res.displayMetrics)
        newConfig.setLocale(localeNew)
        c.createConfigurationContext(newConfig)
    }

}