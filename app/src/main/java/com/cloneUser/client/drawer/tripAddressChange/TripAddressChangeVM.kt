package com.cloneUser.client.drawer.tripAddressChange

import android.app.Application
import android.location.Geocoder
import android.os.Build
import android.text.Editable
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.*
import com.cloneUser.client.connection.responseModels.Route
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationVM
import com.cloneUser.client.drawer.rental.RentalData
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class TripAddressChangeVM @Inject constructor(
    val application: Application,
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) : BaseVM<BaseResponse, TripAddressChangeNavigator>(session, mConnect) {
    var apiCode = -1
    /*
    apiCode 1-> check outstation
            2-> change location api
     */

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCode == 1) {
            if (response != null) {
                val data = Gson().toJson(response.data)
                val obj = Gson().fromJson(data, BaseResponse.DataObjectsAllApi::class.java)
                if (addressChangeValue.get() == 1) {
                    /* get poly string and call api */
                    getPolyString(latLng.get()!!, destinationLatLng!!)
                } else {
                    callAddressChangeApi()
                }
            }

        } else {
            getNavigator().callRequestInProgressFromDrawer()
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
    }

    lateinit var fusedLocationClient: FusedLocationProviderClient
    var latLng = ObservableField<LatLng>()
    var addressChangeValue = ObservableField(-1)
    var address = ObservableField("")
    var showRecyclers = ObservableBoolean(true)

    /* below to params set to false since the recyclers are not used
    It can be used if needed
     */
    var haveFavorites = ObservableBoolean(false)
    var haveRecentLocations = ObservableBoolean(false)

    var prevEditLength = 0
    var currentEditLength = 0
    fun onAddressEdit(e: Editable) {
        currentEditLength = e.length
        if (currentEditLength - prevEditLength == 1 || currentEditLength - prevEditLength == -1) {
            if (currentEditLength > 3) {
                getPlaces(e.toString())
                showRecyclers.set(true)
            }
        }
        prevEditLength = currentEditLength
        showClear.set(e.isNotEmpty())
    }

    var showClear = ObservableBoolean(false)
    fun onClickClear() {
        enableConfirm.set(false)
        address.set("")
    }

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
                false, session.getString(SessionMaintainence.PLACES_DYNAMIC_KEY)
            )?.enqueue(placesApi as Callback<JsonObject?>)
        else
            mapsHelper.GetPlaceApi(
                text,
                false, session.getString(SessionMaintainence.PLACES_DYNAMIC_KEY)
            )
                ?.enqueue(placesApi as Callback<JsonObject?>)
    }

    var hashMap = HashMap<String, String>()
    var requestId = ""
    var enableConfirm = ObservableBoolean(true)
    fun confirm() {
        checkOutstation(latLng.get()?.latitude.toString(), latLng.get()?.longitude.toString())
    }

    fun getAddressFromLatLng(target: ObservableField<String>, source: ObservableField<LatLng>) {
        mapsHelper.GetAddressFromLatLng(
            source.get()!!.latitude.toString() + "," + source.get()!!.longitude,
            false, session.getString(SessionMaintainence.PLACES_DYNAMIC_KEY)
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
                        } else if (status == "OVER_QUERY_LIMIT") {
                            try {
                                val geocoder = Geocoder(getNavigator().getCtx())
                                var mAddress = ""
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    geocoder.getFromLocation(
                                        source.get()!!.latitude,
                                        source.get()!!.longitude,
                                        1
                                    ) { addresses ->
                                        if (addresses.size > 0) {
                                            if (addresses[0].getAddressLine(0) != null) {
                                                mAddress = addresses[0].getAddressLine(0)
                                            }
                                            if (addresses[0].getAddressLine(1) != null) {
                                                mAddress =
                                                    mAddress + ", " + addresses[0].getAddressLine(1)
                                            }
                                            if (addresses[0].getAddressLine(2) != null) {
                                                mAddress =
                                                    mAddress + ", " + addresses[0].getAddressLine(2)
                                            }
                                        }
                                        target.set(mAddress)
                                        enableConfirm.set(true)
                                    }
                                } else {
                                    val addresses = geocoder.getFromLocation(
                                        source.get()!!.latitude,
                                        source.get()!!.longitude,
                                        1
                                    )
                                    if (addresses != null && addresses.size > 0) {
                                        if (addresses[0].getAddressLine(0) != null) {
                                            mAddress = addresses[0].getAddressLine(0)
                                        }
                                        if (addresses[0].getAddressLine(1) != null) {
                                            mAddress =
                                                mAddress + ", " + addresses[0].getAddressLine(1)
                                        }
                                        if (addresses[0].getAddressLine(2) != null) {
                                            mAddress =
                                                mAddress + ", " + addresses[0].getAddressLine(2)
                                        }
                                    }

                                    target.set(mAddress)
                                    enableConfirm.set(true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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
        try {
            val gCoder =
                Geocoder(getNavigator().getCtx())
            val list = gCoder.getFromLocationName(address, 1)
            if (list != null && list.size > 0) {
                target.set(LatLng(list[0].latitude, list[0].longitude))
                getNavigator().getGMap()?.let { moveCamera(it, target.get(), 16f) }
                enableConfirm.set(true)
            } else {
                getLatLng(place_id, target)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            getLatLng(place_id, target)
        }
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
                        enableConfirm.set(true)
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
        )
            ?.enqueue(placesApi as Callback<JsonObject?>)
    }

    private fun checkOutstation(lat: String, long: String) {
        if (getNavigator().isNetworkConnected() && isLoading.value == false) {
            apiCode = 1
            val map = HashMap<String, String>()
            map[Config.pickup_lat] = lat
            map[Config.pickup_long] = long
            getCheckOutstationApiBase(map)
        }
    }

    private fun callAddressChangeApi() {
        apiCode = 2
        if (addressChangeValue.get() == 3) {
            RentalData.pickAddress = "${address.get()}"
            RentalData.pickLat = latLng.get()?.latitude ?: 0.0
            RentalData.pickLng = latLng.get()?.longitude ?: 0.0
            RentalData.flag = true
            getNavigator().closeFromViewModel()
        } else {
            if (getNavigator().isNetworkConnected()) {
                hashMap.clear()
                hashMap[Config.request_id] = requestId
                hashMap[Config.latitude] = "${latLng.get()?.latitude}"
                hashMap[Config.longitude] = "${latLng.get()?.longitude}"
                hashMap[Config.address] = "${address.get()}"
                if (addressChangeValue.get() == 1 && polyString.isNotEmpty())
                    hashMap[Config.poly_string] = polyString
                hashMap[Config.type] =
                    if (addressChangeValue.get() == 1) "PICKUP" else if (addressChangeValue.get() == 2) "DROP" else return
                changeLocationBase(hashMap)
            } else
                getNavigator().showNetworkUnAvailable()
        }
    }

    var destinationLatLng: LatLng? = null
    var polyString = ""
    var routeDest: Route? = null
    private fun getPolyString(origin: LatLng, destination: LatLng) {
        mapsHelper.getDrawPath(
            origin.latitude.toString() + "," + origin.longitude,
            destination.latitude.toString() + "," + destination.longitude, false,
            session.getString(SessionMaintainence.DIRECTION_DYNAMIC_KEY)
        )!!.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.body() != null) {
                    routeDest = Route()
                    Utilz.parseRoute(response.body()!!, routeDest!!)
                    routeDest?.polyPoints?.let {
                        polyString = it
                    }
                }
                callAddressChangeApi()
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                getNavigator().showMessage(t.message!!)
                callAddressChangeApi()
            }
        })
    }


}