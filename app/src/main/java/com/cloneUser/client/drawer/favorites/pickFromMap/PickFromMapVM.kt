package com.cloneUser.client.drawer.favorites.pickFromMap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
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

class PickFromMapVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, PickFromMapNavigator>(session, mConnect) {
    lateinit var fusedLocationClient: FusedLocationProviderClient

    var placeName = ""
    var mAddress = ObservableField("")
    var isLocationAvailable =ObservableBoolean(true)
    var latLng: MutableLiveData<LatLng>? = MutableLiveData()
    var hashMap = HashMap<String,String>()
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        getNavigator().showMessage("Favorite place added successfully")
        getNavigator().goToFavoritesList()
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun onClickSearchLocation(){
        getNavigator().launchSearchPlace()
    }

    fun onClickSelectPlace(){
        if(mAddress.get()!!.isNotEmpty())
            addFavorite()
    }

    fun onClickCurrentLocation(){
        getLastLocation(getNavigator().getCtx(),getNavigator().getGmap())
    }

    fun getLastLocation(context: Context, googleMap: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                latLng!!.value = LatLng(location.latitude, location.longitude)
            }
            latLng?.value?.let {
                moveCamera(googleMap,it,19f)
            }
        }
    }

    fun  getAddressFromLatLng() {
        mapsHelper.GetAddressFromLatLng(
            latLng?.value!!.latitude.toString() + "," + latLng?.value!!.longitude,
            false,session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY)
        )!!.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    if (response.body() != null && response.body()!!
                            .getAsJsonArray("results") != null
                    ) {
                        val status = response.body()!!["status"].asString
                        if (status == "OK") {
                            mAddress.set(response.body()!!.getAsJsonArray("results")[0].asJsonObject["formatted_address"].asString)
                        } else if (status == "OVER_QUERY_LIMIT") {
                            try {  val geocoder = Geocoder(getNavigator().getCtx())
                                var mAddress = ""

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    geocoder.getFromLocation(
                                        latLng?.value!!.latitude, latLng?.value!!.longitude, 1
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
                                        this@PickFromMapVM.mAddress.set(
                                            mAddress
                                        )
                                    }
                                } else {

                                    val addresses = geocoder.getFromLocation(
                                        latLng?.value!!.latitude, latLng?.value!!.longitude, 1
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
                                    this@PickFromMapVM.mAddress.set(
                                        mAddress
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    Log.d(PickFromMapFragment.TAG, "GetAddressFromLatlng$response")
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Log.d(PickFromMapFragment.TAG, "GetAddressFromLatlng$t")
            }
        })
    }

    fun addFavorite(){
        hashMap.clear()
        hashMap[Config.title] = placeName
        hashMap[Config.address] = mAddress.get()!!
        hashMap[Config.latitude] = latLng!!.value!!.latitude.toString()
        hashMap[Config.longitude] = latLng!!.value!!.longitude.toString()
        saveFavoritePlaceBase(hashMap)
    }


}