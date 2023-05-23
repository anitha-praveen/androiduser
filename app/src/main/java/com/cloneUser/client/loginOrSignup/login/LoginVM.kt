package com.cloneUser.client.loginOrSignup.login

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject
import android.location.Geocoder
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.Country
import java.io.IOException
import java.util.*


class LoginVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, LoginNavigator>(session, mConnect) {
    var countryCode = ObservableField("+")
    lateinit var countryModel : Country
    /*BaseView baseView;*/
    var emailOrPhone = ObservableField("")
    val gson = Gson()
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
    }

    fun onClickOutSide() {
       getNavigator().hideKeyBoard()
    }

    fun countryChoose() {
        getNavigator().openCountryListDialogue()
    }

    fun onClickConfirm(){
        when {
            emailOrPhone.get().isNullOrEmpty() -> getNavigator().showMessage(translationModel.txt_phone_cannot_be_empty ?: "")
            emailOrPhone.get()!!.length < 4 -> getNavigator().showMessage(translationModel.txt_enter_complete_ph ?:"")
            else -> getNavigator().openOtp(emailOrPhone.get()!!, countryCode.get()!!, countryModel.id!!)
        }
    }

    fun getCountryFromLatLng(lat: Double,lng:Double) : String{
        var countryName = ""
        val geocoder = Geocoder(getNavigator().getContextAttached(), Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(lat, lng, 1) as List<Address>
            val obj = addresses[0]
            countryName = obj.countryCode
        }catch (e : IOException){
            e.printStackTrace()
        }
        return countryName
    }

    fun checkCountryModelIsInitialized():Boolean{
        return this::countryModel.isInitialized
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    var mLocationRequest: LocationRequest? = null
    lateinit var fusedLocationClient: FusedLocationProviderClient

    fun requestLocationUpdates(requireActivity: FragmentActivity) {
        mLocationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            isWaitForAccurateLocation = true
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
        if (ContextCompat.checkSelfPermission(
                requireActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                mLocationRequest!!,
                mLocationCallback,
                Looper.myLooper()!!
            )
        }
    }

    var currentLatlng = ObservableField<LatLng>()
    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (location != null) {
                    currentLatlng.set(LatLng(location.latitude, location.longitude))
                    stopCallBack()
                }
            }
            session.saveString(SessionMaintainence.CURRENT_LATITUDE, "" + currentLatlng.get()!!.latitude)
            session.saveString(
                SessionMaintainence.CURRENT_LONGITUDE,
                "" + currentLatlng.get()!!.longitude
            )
        }
    }

    private fun stopCallBack(){
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
    }
}