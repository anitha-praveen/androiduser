package com.cloneUser.client.drawer.confirmDestination

import android.app.Application
import android.location.Geocoder
import android.text.Editable
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.*
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject


class ConfirmDestinationVM @Inject constructor(
    val application: Application,
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, ConfirmDestinationNavigator>(session, mConnect) {
    companion object {
        const val TAG = "ConfirmDestinationVM"
    }

    var pickAddress = ObservableField("")
    var pickLatLng = ObservableField<LatLng>()
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var hashMap = HashMap<String, String>()
    var requestId = ""


    /* new UI elements */
    var rider = ObservableField("Myself")
    var showMultipleDropElements = ObservableBoolean(false)
    var drop1Address = ObservableField("")
    var drop2Address = ObservableField("")
    var drop1LatLng = ObservableField<LatLng>()
    var drop2LatLng = ObservableField<LatLng>()
    var apiCode = -1 /* apiCode 0-> favorite and recent location api
                                1 -> add favorite
                                2 -> check zone
                                */
    var haveFavorites = ObservableBoolean(false)
    var haveRecentLocations = ObservableBoolean(false)
    var showRecyclers = ObservableBoolean(true)
    private val isInsideZone = ObservableBoolean(false)


    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCode == 0) {
            val fav: FavPlace = Gson().fromJson(Gson().toJson(response?.data), FavPlace::class.java)
            if (fav.favouriteList?.isNotEmpty()!!) {
                getNavigator().setFavoriteList(fav.favouriteList)
                getNavigator().loadFavorites(fav.favouriteList)
                haveFavorites.set(true)
            } else {
                haveFavorites.set(false)
            }
            if (fav.lastTripHistory?.isNotEmpty()!!) {
                getNavigator().setLastTripHistory(fav.lastTripHistory)
                getNavigator().loadRecentLocations(fav.lastTripHistory)
                haveRecentLocations.set(true)
            } else {
                haveRecentLocations.set(false)
            }
        } else if (apiCode == 1) {
            getFavoritePlaceApiCall()
        } else {
            getNavigator().callRequestInProgressFromDrawer()
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }


    fun getAddressFromLatLng(target: ObservableField<String>, source: ObservableField<LatLng>) {
        mapsHelper.GetAddressFromLatLng(
            source.get()!!.latitude.toString() + "," + source.get()!!.longitude,
            false, session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY)
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
                            proceedNext()
                        } else if (status == "OVER_QUERY_LIMIT") {
                            try {
                                val geocoder = Geocoder(getNavigator().getCtx())
                                val addresses = geocoder.getFromLocation(
                                    source.get()!!.latitude,
                                    source.get()!!.longitude,
                                    1
                                )
                                var mAddress = ""
                                if (addresses != null && addresses.size > 0) {
                                    if (addresses[0].getAddressLine(0) != null) {
                                        mAddress = addresses[0].getAddressLine(0)
                                    }
                                    if (addresses[0].getAddressLine(1) != null) {
                                        mAddress = mAddress + ", " + addresses[0].getAddressLine(1)
                                    }
                                    if (addresses[0].getAddressLine(2) != null) {
                                        mAddress = mAddress + ", " + addresses[0].getAddressLine(2)
                                    }
                                }
                                target.set(mAddress)
                                proceedNext()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "GetAddressFromLatlng$response")
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Log.d(
                    TAG, "GetAddressFromLatlng$t"
                )
            }
        })
    }


    fun swapDestinations() {

        if (drop1Address.get()?.isNotEmpty()!! && drop2Address.get()?.isNotEmpty()!!) {
            /* swap address */
            val temp = drop1Address.get()
            drop1Address.set(drop2Address.get())
            drop2Address.set(temp)

            /*swap LatLng */
            val tempL = drop1LatLng.get()
            drop1LatLng.set(drop2LatLng.get())
            drop2LatLng.set(tempL)
        }

    }


    /* new UI methods */

    fun onClickRiderSelection() {
        getNavigator().hideKeyBoardNav()
        getNavigator().openRiderSelection()
    }

    fun onClickPlus() {
//        if (drop1LatLng.get()?.longitude != 0.0 && drop1LatLng.get()?.longitude != null)
        showMultipleDropElements.set(true)
    }

    fun onClickMinus() {
        showMultipleDropElements.set(false)
        drop2Address.set("")
        drop2LatLng.set(LatLng(0.0, 0.0))
        proceedNext()
    }

    /* The below api used to get favorite and recent locations*/
    fun getFavoritePlaceApiCall() {
        apiCode = 0
        if (getNavigator().isNetworkConnected())
            getFavoritePlaceBase()
        else
            getNavigator().showNetworkUnAvailable()
    }


    var editValue = 1

    /*
    Edit value 1 for drop1
               2 for drop2
     */
    private var prevFirstDropEditLength = 0
    private var currentFirstDropEditLength = 0

    fun onFirstDropChanged(e: Editable) {
//        editValue = 1
//
//        currentFirstDropEditLength = e.length
//        if (currentFirstDropEditLength - prevFirstDropEditLength == 1 || currentFirstDropEditLength - prevFirstDropEditLength == -1) {
//            if (currentFirstDropEditLength > 3) {
//                if (e.length in searchCondition)
//                    getPlaces(e.toString())
//                showRecyclers.set(true)
//            }
//        }
        getPlaces(e.toString())
        showRecyclers.set(true)
        prevFirstDropEditLength = currentFirstDropEditLength

        if (e.isEmpty()) {
            getNavigator().getBinds().dropAddress1.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.clear_icon_invisible_small,
                0
            )
        } else {
            getNavigator().getBinds().dropAddress1.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.clear_icon_visible_small,
                0
            )
        }
    }


    var currentSecondDropEditLength = 0
    var prevSecondDropEditLength = 0
    fun onSecondDropChanged(e: Editable) {
//        editValue = 2
//        currentSecondDropEditLength = e.length
//        if (currentSecondDropEditLength - prevSecondDropEditLength == 1 || currentSecondDropEditLength - prevSecondDropEditLength == -1) {
//            if (currentSecondDropEditLength > 3) {
//                if (e.length in searchCondition)
//                    getPlaces(e.toString())
//                showRecyclers.set(true)
//            }
//        }
        getPlaces(e.toString())
        showRecyclers.set(true)
        prevSecondDropEditLength = currentSecondDropEditLength
        if (e.isEmpty()) {
            getNavigator().getBinds().dropAddress2.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.clear_icon_invisible_small,
                0
            )
        } else {
            getNavigator().getBinds().dropAddress2.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.clear_icon_visible_small,
                0
            )
        }

    }

    fun onClickDrop1() {
        editValue = 1
        getNavigator().getBinds().dropAddress1.requestFocus()
    }

    fun onClickDrop2() {
        editValue = 2
        getNavigator().getBinds().dropAddress2.requestFocus()
    }


    var favPlacesVM: MutableList<FavPlace.Favourite> = ArrayList()
    private fun getPlaces(text: String) {
        val latlng: LatLng? = pickLatLng.get()
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
            )?.enqueue(placesApi as Callback<JsonObject?>)
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
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                println("++$t")
            }
        }

        mapsHelper.GetLatLngFromPlaceId(
            place_id,
            session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY)
        )?.enqueue(placesApi as Callback<JsonObject?>)
    }


    fun getLatLngFromAddress(address: String, place_id: String, target: ObservableField<LatLng>) {
        try {
            val gCoder =
                Geocoder(getNavigator().getCtx())
            val list = gCoder.getFromLocationName(address, 1)
            if (list != null && list.size > 0) {
                target.set(LatLng(list[0].latitude, list[0].longitude))
            } else {
                getLatLng(place_id, target)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            getLatLng(place_id, target)
        }
    }

    fun proceedNext() {
        if (showMultipleDropElements.get()) {
            if (drop1LatLng.get() != null && drop1LatLng.get()!!.latitude != 0.0 && drop1LatLng.get()!!.longitude != 0.0
                && drop2LatLng.get() != null && drop2LatLng.get()!!.latitude != 0.0 && drop2LatLng.get()!!.longitude != 0.0
            ) {
                /* go to next screen */
                getNavigator().gotoRideConfirm()
            }
        } else {
            if (drop1LatLng.get() != null && drop1LatLng.get()!!.latitude != 0.0 && drop1LatLng.get()!!.longitude != 0.0) {
                /* go to next screen */
                getNavigator().gotoRideConfirm()
            }
        }

    }


    var fPlaceName = ""
    var fAddress = ""
    var fLat = ""
    var fLng = ""
    fun addFavorite() {
        apiCode = 1
        if (getNavigator().isNetworkConnected()) {
            hashMap.clear()
            hashMap[Config.title] = fPlaceName
            hashMap[Config.address] = fAddress
            hashMap[Config.latitude] = fLat
            hashMap[Config.longitude] = fLng
            saveFavoritePlaceBase(hashMap)
        } else
            getNavigator().showNetworkUnAvailable()
    }

    fun checkOutstation(lat: String, long: String) {
        if (getNavigator().isNetworkConnected() && isLoading.value == false) {
            apiCode = 2
            val map = HashMap<String, String>()
            map[Config.pickup_lat] = lat
            map[Config.pickup_long] = long
            getCheckOutstationApiBase(map)
        }
    }


}