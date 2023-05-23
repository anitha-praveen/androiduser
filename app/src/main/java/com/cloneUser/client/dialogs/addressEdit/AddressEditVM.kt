package com.cloneUser.client.dialogs.addressEdit

import android.app.Activity
import android.text.Editable
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.*
import com.cloneUser.client.connection.responseModels.CancelReason
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationVM
import com.cloneUser.client.ut.SessionMaintainence
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AddressEditVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) : BaseVM<BaseResponse, AddressEditNavigator>(session, mConnect) {
    var reason: CancelReason.Reason? = null
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    var isGetaddressCall = ObservableBoolean(false)
    var addressChangeValue = ObservableField(1)
    var address = ObservableField("")
    var showRecyclers = ObservableBoolean(false)
    var haveRecentLocations = ObservableBoolean(false)
    var haveFavorites = ObservableBoolean(false)
    var enableConfirm = ObservableBoolean(false)
    var showClear = ObservableBoolean(false)
    lateinit var fusedLocationClient: FusedLocationProviderClient

    var prevEditLength = 0
    var currentEditLength = 0
    fun onAddressEdit(e: Editable) {
//        currentEditLength = e.length
//        if (currentEditLength - prevEditLength == 1 || currentEditLength - prevEditLength == -1) {
//            if (currentEditLength > 3) {
//                if (e.length in searchCondition)
//                    getPlaces(e.toString())
//                showRecyclers.set(true)
//            }
//        }
        getPlaces(e.toString())
        showRecyclers.set(true)
        //prevEditLength = currentEditLength
        showClear.set(e.isNotEmpty())
    }

    var latLng = ObservableField<LatLng>()
    var favPlacesVM: MutableList<FavPlace.Favourite> = ArrayList()
    private fun getPlaces(text: String) {
        val latlng: LatLng? = latLng.get()
        favPlacesVM.clear()
        val placesApi: Callback<*> = object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body()!!["status"].asString.equals(
                        "OK",
                        ignoreCase = true
                    )
                ) {
                    val predictionJsonArray = response.body()!!.getAsJsonArray("predictions")
                    for (i in 0 until predictionJsonArray.size()) {
                        val favplace = FavPlace.Favourite()
                        val address = predictionJsonArray[i].asJsonObject["description"].asString
                        val id = predictionJsonArray[i].asJsonObject["place_id"].asString
                        favplace.address = address
                        favplace.placeId = id
                        if (address.contains(",")) {
                            favplace.title = address.substring(0, address.indexOf(","))
                        } else if (!address.contains(",")) {
                            favplace.title = address
                        }
                        favPlacesVM.add(favplace)
                    }
                    getNavigator().loadSearchResults(favPlacesVM)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                println("++$t")
            }
        }

        if (latlng != null)
            mapsHelper.GetPlaceApi(
                "${latlng.latitude},${latlng.longitude}", text,
                false,
                session.getString(SessionMaintainence.PLACES_DYNAMIC_KEY)
            )?.enqueue(placesApi as Callback<JsonObject?>)
        else
            mapsHelper.GetPlaceApi(
                text,
                false,
                session.getString(SessionMaintainence.PLACES_DYNAMIC_KEY)
            )
                ?.enqueue(placesApi as Callback<JsonObject?>)
    }

    fun onClickClear() {
        enableConfirm.set(false)
        address.set("")
        getNavigator().getBindss().pickupAddress.requestFocus()
        val imm: InputMethodManager =
            getNavigator().getBindss().pickupAddress.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(getNavigator().getBindss().pickupAddress, InputMethodManager.SHOW_FORCED)
    }

    fun confirm() {
        getNavigator().sendToTarget()
    }

    fun getAddressFromLatLng(target: ObservableField<String>, source: ObservableField<LatLng>) {
        mapsHelper.GetAddressFromLatLng(
            source.get()!!.latitude.toString() + "," + source.get()!!.longitude,
            false,
            session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY)
        )!!.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    if (response.body() != null && response.body()!!
                            .getAsJsonArray("results") != null
                    ) {
                        val status = response.body()!!["status"].asString
                        if (status == "OK") {
                            target.set(
                                response.body()!!
                                    .getAsJsonArray("results")[0].asJsonObject["formatted_address"].asString
                            )
                            enableConfirm.set(true)
                        }
                    }
                } else {
                    Log.d(ConfirmDestinationVM.TAG, "GetAddressFromLatlng$response")
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Log.d(
                    ConfirmDestinationVM.TAG, "GetAddressFromLatlng$t"
                )
            }
        })

    }

    fun getLatLngFromAddress(address: String, place_id: String, target: ObservableField<LatLng>) {
//        try {
//            val gCoder =
//                Geocoder(getNavigator().getCtx())
//            val list = gCoder.getFromLocationName(address, 1)
//            if (list != null && list.size > 0) {
//                target.set(LatLng(list[0].latitude, list[0].longitude))
//                getNavigator().getGMap()?.let { moveCamera(it, target.get(), 16f) }
//                enableConfirm.set(true)
//            } else {
//                getLatLng(place_id, target)
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
        getLatLng(place_id, target)

    }

    fun getLatLng(place_id: String, target: ObservableField<LatLng>) {
        val placesApi: Callback<*> = object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body()!!["status"].asString.equals(
                        "OK",
                        ignoreCase = true
                    )
                ) {
                    Log.e("PLaces", "${response.body()}")
                    val predictionJsonArray = response.body()!!.getAsJsonArray("results")
                    for (i in 0 until predictionJsonArray.size()) {
                        val lat =
                            predictionJsonArray[i].asJsonObject["geometry"].asJsonObject["location"].asJsonObject["lat"].asDouble
                        val lng =
                            predictionJsonArray[i].asJsonObject["geometry"].asJsonObject["location"].asJsonObject["lng"].asDouble
                        target.set(LatLng(lat, lng))
                        getNavigator().getGMap()?.let { moveCamera(it, target.get(), 16f) }
                        enableConfirm.set(true)
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                println("++$t")
            }
        }

        mapsHelper.GetLatLngFromPlaceId(
            place_id, session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY)
        )?.enqueue(placesApi as Callback<JsonObject?>)
    }

}