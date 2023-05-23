package com.cloneUser.client.drawer.profile.proAddFvourites

import android.location.Geocoder
import android.os.Build
import android.text.Editable
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.*
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationVM
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.collections.MutableList
import kotlin.collections.set

class ProAddFavouritesVm @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, ProAddFavoNavigator>(session, mConnect) {
    val latlng = ObservableField<LatLng>()
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var searchPlaceVm: MutableList<FavPlace.Favourite> = ArrayList()
    val showMarker = ObservableBoolean(true)
    val showAdapter = ObservableBoolean(false)
    val showCnfrmButton = ObservableBoolean(false)
    val address = ObservableField("")
    val mPlaceName = ObservableField("")
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        getNavigator().closeFrag()

    }
    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showMessage(e.exception!!)
    }
    var currentEditLength = 0
    var prevEditLength = 0
    fun onAddressEdit(e: Editable) {
        currentEditLength = e.length
        if(currentEditLength - prevEditLength == 1 || currentEditLength - prevEditLength == -1 ){
            if (currentEditLength > 3){
                showMarker.set(false)
                getPlaces(e.toString())
                showAdapter.set(true)
            }

        }
        prevEditLength = currentEditLength
    }

    private fun getPlaces(text: String) {
        val latlng: LatLng? = latlng.get()
        searchPlaceVm.clear()
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
                        searchPlaceVm.add(favplace)
                    }
                    getNavigator().loadAdapter(searchPlaceVm)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                println("++$t")
            }
        }

        if (latlng != null)
            mapsHelper.GetPlaceApi(
                "${latlng.latitude},${latlng.longitude}", text,
                false,session.getString(SessionMaintainence.PLACES_DYNAMIC_KEY)
            )?.enqueue(placesApi as Callback<JsonObject?>)
        else
            mapsHelper.GetPlaceApi(
                text,
                false,session.getString(SessionMaintainence.PLACES_DYNAMIC_KEY))
                ?.enqueue(placesApi as Callback<JsonObject?>)
    }
    fun getLatLngFromAddress(address: String, place_id: String, target: ObservableField<LatLng>) {
        try {
            val gCoder =
                Geocoder(getNavigator().getCtx())
            val list = gCoder.getFromLocationName(address, 1)
            if (list != null && list.size > 0) {
                target.set(LatLng(list[0].latitude, list[0].longitude))
                getNavigator().getGMap()?.let { moveCamera(it, target.get(), 16f) }

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
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                println("++$t")
            }
        }
        mapsHelper.GetLatLngFromPlaceId(place_id, session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY))
            ?.enqueue(placesApi as Callback<JsonObject?>)
    }
    fun onClickClear(){
        showCnfrmButton.set(false)
        address.set("")
    }
    fun getAddressFromLatLng(target: ObservableField<String>, source: ObservableField<LatLng>) {
        mapsHelper.GetAddressFromLatLng(
            source.get()!!.latitude.toString() + "," + source.get()!!.longitude,
            false,session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY))!!.enqueue(object : Callback<JsonObject?> {
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
                            showCnfrmButton.set(true)
                        } else if (status == "OVER_QUERY_LIMIT") {
                            try {
                                val geocoder = getNavigator().getCtx().let { Geocoder(it) }
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
                                                mAddress = mAddress + ", " + addresses[0].getAddressLine(1)
                                            }
                                            if (addresses[0].getAddressLine(2) != null) {
                                                mAddress = mAddress + ", " + addresses[0].getAddressLine(2)
                                            }
                                        }
                                        target.set(mAddress)
                                        showCnfrmButton.set(true)
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
                                            mAddress = mAddress + ", " + addresses[0].getAddressLine(1)
                                        }
                                        if (addresses[0].getAddressLine(2) != null) {
                                            mAddress = mAddress + ", " + addresses[0].getAddressLine(2)
                                        }
                                    }
                                    target.set(mAddress)
                                    showCnfrmButton.set(true)
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
    fun saveFavourites(){
        var hashMap = HashMap<String, String>()
        hashMap[Config.title] = mPlaceName.get()!!
        hashMap[Config.address] = address.get()!!
        hashMap[Config.latitude] = latlng.get()?.latitude.toString()
        hashMap[Config.longitude] = latlng.get()?.longitude.toString()
        saveFavoritePlaceBase(hashMap)

    }


}