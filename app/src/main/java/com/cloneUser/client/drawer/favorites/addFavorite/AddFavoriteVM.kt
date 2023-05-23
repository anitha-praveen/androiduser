package com.cloneUser.client.drawer.favorites.addFavorite

import android.location.Geocoder
import android.os.Build
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.MapsHelper
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AddFavoriteVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, AddFavoriteNavigator>(session, mConnect) {

    companion object{
        const val TAG = "AddFavoriteVM"
    }
    var mIsEditEnable = ObservableBoolean(false)
    var mPlaceName = ObservableField("")
    var mAddress = ObservableField("")
    var mIsSubmitEnable = ObservableBoolean(false)
    var mLatLng: LatLng? = null
    var hashMap : HashMap<String,String> = HashMap()
    var optionSelected = ObservableBoolean(false)
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if(response!!.success==true && response.data != null){
            getNavigator().closeFragment()
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun onClickBack(view: View) {
        getNavigator().closeFragment()
    }

    fun onHomeClick(view: View) {
        mPlaceName.set(translationModel.txt_Home)
        mIsEditEnable.set(false)
        optionSelected.set(true)
    }

    fun onWorkClick(view: View) {
        mPlaceName.set(translationModel.txt_Work)
        mIsEditEnable.set(false)
        optionSelected.set(true)
    }

    fun onOtherClick(view: View) {
        mPlaceName.set("")
        mIsEditEnable.set(true)
        optionSelected.set(true)
    }

    fun onPlaceTextChanged(editable: Editable) {
        mPlaceName.set(editable.toString())
        if (mPlaceName.get()!!.isNotEmpty() && mAddress.get()!!.isNotEmpty())
            mIsSubmitEnable.set(true)
        else
            mIsSubmitEnable.set(false)
    }

    fun onAddressTextChanged(editable: Editable) {
        mAddress.set(editable.toString())
        if (mPlaceName.get()!!.isNotEmpty() && mAddress.get()!!.isNotEmpty())
            mIsSubmitEnable.set(true)
        else
            mIsSubmitEnable.set(false)
    }

    fun onPickFromMapClick() {
        if(mPlaceName.get()!!.isNotEmpty())
        getNavigator().goToPickFromMap()
        else{
            getNavigator().showMessage(translationModel.txt_enter_short_name?:"")
            getNavigator().getBinding().placeName.requestFocus()
        }
    }

    fun onClickSavePlace(view: View) {
        if (getNavigator().isNetworkConnected()) {
            if(mAddress.get()?.trim()?.length!! == 0){
                getNavigator().showMessage("Enter a valid text")
            }else{
                isLoading.value =true
                getLocationFromAddress(mAddress.get()!!)
            }
        } else {
            getNavigator().showNetworkUnAvailable()
        }
    }

    private fun getLocationFromAddress(place: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val gCoder = Geocoder(getNavigator().getCtx())
                val list = gCoder.getFromLocationName(place, 1) { list ->

                    if (list != null && list.size > 0) {
                        mLatLng = LatLng(list[0].latitude, list[0].longitude)
                        var address = ""
                        if (list[0].getAddressLine(0) != null) {
                            address = list[0].getAddressLine(0)
                        }
                        if (list[0].getAddressLine(1) != null) {
                            address = address + ", " + list[0].getAddressLine(1)
                        }
                        if (list[0].getAddressLine(2) != null) {
                            address = address + ", " + list[0].getAddressLine(2)
                        }
                        mAddress.set(address)
                        addFavorite()
                    }
                }
            } else {

                val gCoder = Geocoder(getNavigator().getCtx())
                val list = gCoder.getFromLocationName(place, 1)
                if (list != null && list.size > 0) {
                    mLatLng = LatLng(list[0].latitude, list[0].longitude)
                    var address = ""
                    if (list[0].getAddressLine(0) != null) {
                        address = list[0].getAddressLine(0)
                    }
                    if (list[0].getAddressLine(1) != null) {
                        address = address + ", " + list[0].getAddressLine(1)
                    }
                    if (list[0].getAddressLine(2) != null) {
                        address = address + ", " + list[0].getAddressLine(2)
                    }
                    mAddress.set(address)
                    addFavorite()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mapsHelper.GetLatLngFromAddress(place, false, session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY))!!.enqueue(object : Callback<JsonObject?> {
                    override fun onResponse(
                        call: Call<JsonObject?>,
                        response: Response<JsonObject?>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body() != null && response.body()!!
                                    .getAsJsonArray("results") != null && response.body()!!
                                    .getAsJsonArray("results").size() != 0
                            ) {
                                val lat = response.body()!!
                                    .getAsJsonArray("results")[0].asJsonObject["geometry"].asJsonObject["location"].asJsonObject["lat"].asDouble
                                val lng = response.body()!!
                                    .getAsJsonArray("results")[0].asJsonObject["geometry"].asJsonObject["location"].asJsonObject["lng"].asDouble
                                mLatLng = LatLng(lat, lng)
                                mAddress.set(
                                    response.body()!!
                                        .getAsJsonArray("results")[0].asJsonObject["formatted_address"].asString
                                )
                                addFavorite()
                            }
                        } else {
                            Log.d(TAG, "GetAddressFromLatlng $response")
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Log.d(TAG, "GetAddressFromLatlng$t")
                    }
                })
        }
    }

    fun addFavorite(){
        hashMap.clear()
        hashMap[Config.title] = mPlaceName.get()!!
        hashMap[Config.address] = mAddress.get()!!
        hashMap[Config.latitude] = mLatLng!!.latitude.toString()
        hashMap[Config.longitude] = mLatLng!!.longitude.toString()
        saveFavoritePlaceBase(hashMap)
    }

}