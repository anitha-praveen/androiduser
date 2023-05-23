package com.cloneUser.client.drawer.outStation

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.app.ActivityCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.MapsHelper
import com.cloneUser.client.connection.responseModels.OutStationTypes
import com.cloneUser.client.connection.responseModels.OutstationModel
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationVM
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import javax.inject.Inject


class OutStationVM @Inject constructor(
    val application: Application,
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, OutStationNavigator>(session, mConnect) {
    val latLng = ObservableField<LatLng>()
    val address = ObservableField("")
    val zoom = 19f
    var apiCode = -1

    var toTxt = ObservableField("")
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var showTypes = ObservableBoolean(false)
    var outStationListItems: OutstationModel? = null

    var dateTimeRideLater = ObservableField("")
    var rideLaterDate = ObservableField("")
    var rideLaterTime = ObservableField("")
    var showCalender = ObservableBoolean(false)
    var isDateSelected = ObservableBoolean(false)
    var priceTxt = ObservableField("")
    val pickAddress = ObservableField("")
    val dropLatLng = ObservableField<LatLng>()
    val dropAddress = ObservableField("")
    val typeSlug = ObservableField("")
    var polyString = ObservableField("")


    var selectedType: OutStationTypes? = null
    var selectedTypeId: Int? = null

    var promoCode = ObservableField("")
    var isPromoApplied = ObservableBoolean(false)
    var promoAmount = ObservableField("")

    val basePrice = ObservableField("")
    val kmPrice = ObservableField("")
    val driverBata = ObservableField("")
    val distanceTxt = ObservableField<Any>()
//    for just two way trip
//    val distancePrice = ObservableField("")

    val scheduleButtontxt = ObservableField(translationModel.txt_schedule)
    val confirmButtonTxt = ObservableField(translationModel.txt_confirm)

    var baseFare = ObservableField("") /* basefare below 100 km */
    var isTwoWay = ObservableBoolean(false)
    var infoTotal = ObservableField("")
    val editedStartTime = ObservableField("")
    val editedEndTime = ObservableField("")
    val showCurrentTime = ObservableField("")
    val showEndTime = ObservableField("")

    //to check if return date added for two way
    val isReturnDateSet = ObservableBoolean(false)
    var showHillPrice = false
    var extraTime = ObservableField("")

    /*
    apicode 1-> types
     */
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCode == 1) {
            if (response?.data != null) {
                showTypes.set(true)
                val json = Gson().toJson(response)
                val obj = Gson().fromJson(json, BaseResponse.BaseOutstationTypes::class.java)
                getNavigator().loadTypesAdapter(obj.outStationTypes)
            }
        } else if (apiCode == 2) {
            getNavigator().openSuccess()
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    //to get address from latLong
    fun getAddressFromLatLng(target: ObservableField<String>, source: ObservableField<LatLng>) {
        try {
            val geocoder = getNavigator().getCtx()?.let { Geocoder(it) }
            var mAddress = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder?.getFromLocation(
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
                        target.set(mAddress)
                    }else {
                        googleApi(target, source)
                    }


                }
            } else {
                val addresses = geocoder?.getFromLocation(
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
                    target.set(mAddress)
                }else {
                    googleApi(target, source)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            googleApi(target, source)
        }


    }

    private fun googleApi(target: ObservableField<String>, source: ObservableField<LatLng>) {
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
                        } else if (status == "OVER_QUERY_LIMIT") {
                        }
                    }
                } else {
//                    Log.d(ConfirmDestinationVM.TAG, "GetAddressFromLatlng$response")
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Log.d(
                    ConfirmDestinationVM.TAG, "GetAddressFromLatlng$t"
                )
            }
        })
    }

    //getting last location
    private fun getLastLocation(context: Context) {
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
                session.saveString(SessionMaintainence.CURRENT_LATITUDE, "" + location.latitude)
                session.saveString(
                    SessionMaintainence.CURRENT_LONGITUDE,
                    "" + location.longitude
                )
                latLng.set(LatLng(location.latitude, location.longitude))
                getAddressFromLatLng(address, this.latLng)
                moveCamera(getNavigator().getMap(), this.latLng.get(), zoom)
            }
        }

    }

    fun setInitialLocation() {
        if (!session.getString(SessionMaintainence.CURRENT_LATITUDE).isNullOrEmpty() &&
            !session.getString(SessionMaintainence.CURRENT_LONGITUDE).isNullOrEmpty()
        ) {
            val latLng = LatLng(
                session.getString(SessionMaintainence.CURRENT_LATITUDE)!!.toDouble(),
                session.getString(SessionMaintainence.CURRENT_LONGITUDE)!!.toDouble()
            )
            moveCamera(getNavigator().getMap(), latLng, zoom)
            this.latLng.set(latLng)
            getAddressFromLatLng(address, this.latLng)

        } else
            viewModelScope.launch { getNavigator().getCtx()?.let { getLastLocation(it) } }
    }

    fun onChangeAddressClick() {
        if (isLoading.value == false)
            getNavigator().gotoSearchPlace()
    }

    fun goToSelectOutStation() {
        if (isLoading.value == false)
            getNavigator().goSelectOutStation()

    }

    fun getOutsationTypes() {
        val map = HashMap<String, String>()
        map[Config.outstation_id] = outStationListItems!!.id.toString()
        map[Config.pick_lng] = latLng.get()?.longitude.toString()
        map[Config.pick_lat] = latLng.get()?.latitude.toString()
        map[Config.trip_way_type] = if (isTwoWay.get()) "TWO" else "ONE"
        map[Config.from_date] = editedStartTime.get().toString()
        if (isReturnDateSet.get())
            map[Config.to_date] = editedEndTime.get().toString()
        else
            map[Config.to_date] = extraTime.get().toString()
//        map[Config.trip_way_type] = "TWO"
        if (isPromoApplied.get())
            map[Config.promo_code] = promoCode.get().toString()

        if (getNavigator().isNetworkConnected()) {
            apiCode = 1
            getOutStationTypesBase(map)

        } else
            getNavigator().showNetworkUnAvailable()
    }

    fun scheduledClick(view: View) {
        if (isLoading.value == false) {
            if (isTwoWay.get()) {
                if (!isReturnDateSet.get())
                    getNavigator().openScheduleBottomSheet(true)
                else
                    getNavigator().showConfirmation()
            } else {
                if (!isDateSelected.get()) {
//            isDateSelected.set(!isDateSelected.get())
                    showCalenderVm()
                } else
                    getNavigator().showConfirmation()
            }
        }
    }

    fun showCalenderVm() {
        getNavigator().openScheduleBottomSheet(false)
    }

    fun createRequest() {
        val hashMap = HashMap<String, String>()
        if (getNavigator().isNetworkConnected() && isLoading.value == false) {
            if (validation()) {
                apiCode = 2
                hashMap.clear()
                hashMap[Config.pick_address] = address.get()!!
                hashMap[Config.is_later] = "1"
                hashMap[Config.drop_address] = dropAddress.get()!!
                hashMap[Config.payment_opt] = "CASH"
                hashMap[Config.vehicle_type] = typeSlug.get().toString()
                hashMap[Config.pick_lat] = latLng.get()!!.latitude.toString()
                hashMap[Config.pick_lng] = latLng.get()!!.longitude.toString()
                if (isTwoWay.get()) {
                    hashMap[Config.trip_start_time] = editedStartTime.get()!!
                    hashMap[Config.trip_end_time] = editedEndTime.get()!!
                } else
                    hashMap[Config.trip_start_time] = dateTimeRideLater.get()!!
                hashMap[Config.drop_lat] = dropLatLng.get()!!.latitude.toString()
                hashMap[Config.drop_lng] = dropLatLng.get()!!.longitude.toString()
                hashMap[Config.ride_type] = "OUTSTATION"
                hashMap[Config.trip_way_type] = if (isTwoWay.get()) "TWO" else "ONE"
                hashMap[Config.outstation_id] = outStationListItems!!.id.toString()
                if (isPromoApplied.get())
                    hashMap[Config.promo_code] = promoCode.get()!!
                createRequestBase(hashMap)
            }
        } else if (isLoading.value == true) {
            getNavigator().showMessage("Previous request is in progress")
        } else {
            getNavigator().showNetworkUnAvailable()

        }
    }

    fun validation(): Boolean {
        if (isTwoWay.get())
            return true
        else
            return dateTimeRideLater.get()!!.isNotEmpty()

    }

    fun openPromo() {
        getNavigator().openPromoDialog()
    }

    fun onCashClick(view: View) {
        translationModel.txt_only_cash?.let { getNavigator().showMessage(it) }
    }

    fun callAdmin(view: View) {
        getNavigator().toCallAdmin()
    }

    fun etaClicked(view: View) {
        getNavigator().showOutstationEta()
    }

    fun onTwoWayClick() {
        if (outStationListItems?.id != null)
            getOutsationTypes()
    }

    fun onTripTypeClick(type: Boolean) {
        isTwoWay.set(type)
        if (type)
            getNavigator().setCurrentTime()
        else
            isReturnDateSet.set(false)

        if (outStationListItems?.id != null)
            getOutsationTypes()
    }

    fun startTimeClick(view: View) {
        getNavigator().openScheduleBottomSheet(false)

    }

    fun endTimeClick(view: View) {
        getNavigator().openScheduleBottomSheet(true)
    }
}

@BindingAdapter("layoutHeight")
fun layoutHeight(view: View, width: Float) {
    val params = view.layoutParams
    if (width == 0F) {
        params.height = WRAP_CONTENT
    } else params.height = width.toInt()
    view.layoutParams = params

}