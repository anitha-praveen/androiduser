package com.cloneUser.client.drawer.rental

import android.app.Application
import android.location.Location
import android.util.Log
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.MapsHelper
import com.cloneUser.client.connection.responseModels.PackageModel
import com.cloneUser.client.connection.responseModels.RentalPackageTypes
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.FirebaseHelper
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import org.json.JSONObject
import retrofit2.Call
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class RentalVM @Inject constructor(
    val application: Application,
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, RentalNavigator>(session, mConnect), FirebaseHelper.FirebaseObserver {
    var apiCode = -1

    /*
    apiCode = 0 -> getRentalPackage list
              1 -> getSelectedPackagesDetails
              2 -> create Request
              3 -> create Ride Later request
              4 -> cancel Request
     */
    var estimatedFare = ObservableField("")
    var promoCode = ObservableField("")
    var isPromoApplied = ObservableBoolean(false)
    var promoEstimatedFare = ObservableField("")
    var paymentType = ObservableField("CASH")
    var isPaymentSelected = ObservableBoolean(true) /* default cash */
    var pickupAddress = ObservableField("")
    var pickupLatLng = ObservableField<LatLng>()
    var currencySymbol = ""
    var packageModel: PackageModel.Package? = null
    var rentalTypeModel: RentalPackageTypes.Type? = null
    var showPromoNotApplicable =
        ObservableBoolean(false) /* whether promo code chosen is applicable to selected type */
    var showPromoAppliedTotalAmount = ObservableBoolean(false)
    var isTripBooked = ObservableBoolean(false)
    var progressRate = ObservableField(0)
    var requestId = ""
    var isTypesLoaded = ObservableBoolean(false)

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        //Utilz.dismissProgress()
        if (apiCode == 0) {
            val model: PackageModel? =
                Gson().fromJson(Gson().toJson(response), PackageModel::class.java)
            packageModel = model?.packageList?.get(0)
            model?.packageList?.let { getNavigator().addList(it) }
            getSelectedPackageDetails()
        } else if (apiCode == 1) {
            val model2: RentalPackageTypes? =
                Gson().fromJson(Gson().toJson(response), RentalPackageTypes::class.java)
            model2?.rentalTypes?.let { getNavigator().addSelectedPackageDetails(it) }
        } else if (apiCode == 2) {
            val resp: RequestData =
                Gson().fromJson(Gson().toJson(response?.data), RequestData::class.java)
            if (resp.data != null)
                requestId = resp.data?.id ?: ""
            isTripBooked.set(true)
            getNavigator().runProgressBar()
        } else if (apiCode == 3) {
            getNavigator().openSuccess()
        } else if (apiCode == 4) {
            if (response!!.success == true)
                isTripBooked.set(false)
            getNavigator().stopProgressBar()
            getNavigator().getBind().customSeek.progress = 0
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        Utilz.dismissProgress()
        if (apiCode == 2) {
            if (e.code == 2001) {
                requestId = e.exception ?: ""
                isTripBooked.set(false)
                getNavigator().showMessage("No Driver found")
            } else {
                isTripBooked.set(false)
                getNavigator().showMessage(e.exception ?: "")
            }
        } else {
            getNavigator().showCustomDialog(e.exception!!)
            isTripBooked.set(false)
        }

    }

    var rideType = ObservableField(1)
    var rideLaterDate = ObservableField("")
    var rideLaterTime = ObservableField("")
    var dateTimeRideLater =
        ObservableField("") /* This variable is used as api param value to call Create Ride Later request */

    fun onClickSchedule() {
        getNavigator().openScheduleBottomSheet()
    }


    fun getRentalPackageList() {
        apiCode = 0
        if (getNavigator().isNetworkConnected()) {
            getRentalPackageListBase()
        } else
            getNavigator().showNetworkUnAvailable()
    }

    var map = HashMap<String, String>()

    fun getSelectedPackageDetails() {
        apiCode = 1
        if (getNavigator().isNetworkConnected()) {
            map.clear()
            map[Config.package_id] = packageModel?.slug ?: ""
            if (isPromoApplied.get())
                map[Config.promo_code] = promoCode.get()!!
            getSelectedPackageDetailsBase(map, getNavigator().getCtx())
        } else
            getNavigator().showNetworkUnAvailable()
    }

    fun onClickEstimatedPrice() {
        getNavigator().showRentalEta()
    }

    fun onClickPromoCode() {
        getNavigator().openPromoDialog()
    }

    fun onCashClicked() {

    }

    var bookLaterText = ObservableField("")
    var bookNowText = ObservableField("")
    var isDriversAvailable = ObservableBoolean(false)
    fun onClickConfirm(view: View) {
        if (isLoading.value == false) {
            if (rentalTypeModel?.id != null) {
                getNavigator().showConfirmAlert()
            }
        }
    }

    fun changePickUp() {
        if (isLoading.value == false)
            getNavigator().pickupAddressChange()
    }

    var driversLocations: HashMap<String, GeoLocation> = HashMap()
    var driverBearing: HashMap<String, Double> = HashMap()
    var driverDatas = HashMap<String, String>()
    private val driverImages: HashMap<String, Marker> = HashMap()
    var firebaseHelper: FirebaseHelper? = null
    lateinit var bitmapDescriptorFactory: BitmapDescriptor
    var markeroption = MarkerOptions()

    override fun driverEnteredFence(key: String?, location: GeoLocation?, response: String?) {
        activeInactiveDrivers(key!!, location!!, response!!)
    }

    override fun driverExitedFence(key: String?, response: String?) {
        try {
            if (driverImages.containsKey(key)) {
                val driverPin = driverImages[key]
                driverPin!!.remove()
                driversLocations.remove(key)
                driverImages.remove(key)
                driverDatas.remove(key)
                firebaseHelper?.removeObserverFor(key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun driverMovesInFence(key: String?, location: GeoLocation?, response: String?) {}

    private fun activeInactiveDrivers(key: String, location: GeoLocation, response: String) {
        try {
            val jsonObject = JSONObject(response)
            if (jsonObject.has("is_active")) {
                val isActive = jsonObject.getBoolean("is_active")
                if (isActive) {
                    val updatedAt = jsonObject.getLong("updated_at")
                    val currentTime = Date().time
                    val diff = (currentTime - updatedAt) / 1000
                    if (diff < 5 * 60) {
                        val bearing = jsonObject.getDouble("bearing")
                        if (jsonObject.has("service_category") && jsonObject.getString("service_category")
                                .contains(
                                    "RENTAL",
                                    true
                                )
                        ) {
                            /*here driver passed all conditions
                              check whether he is already in our list
                              If he is already in our list -> update his data , location and remove his marker and show marker with updated data
                              else add his data , location and remove his marker and show marker with updated data
                           */
                            if (driverDatas.containsKey(key) && driversLocations.containsKey(key)) {
                                /* driver is already in our data
                                   so update his data , location and remove his marker and show marker with updated data
                                 */
                                driverDatas[key] = response
                                driversLocations[key] = location

                                if (driverImages.containsKey(key)) {
                                    val driverPin = driverImages[key]
                                    driverPin!!.remove()
                                    driverImages.remove(key)
                                }


                                if (typeSlug.isNullOrEmpty() || typeSlug.equals("bajaj-auto")) {
                                    if (jsonObject.has("type") && jsonObject.getString("type")
                                            .equals("bajaj-auto")
                                    ) {
                                        bitmapDescriptorFactory =
                                            BitmapDescriptorFactory.fromResource(R.drawable.roda_auto)

                                        markeroption.position(
                                            LatLng(
                                                location.latitude,
                                                location.longitude
                                            )
                                        )
                                            .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                            .icon(bitmapDescriptorFactory)
                                        driverImages[key] =
                                            getNavigator().getGMap()!!.addMarker(markeroption)!!
                                    }
                                } else {
                                    bitmapDescriptorFactory =
                                        BitmapDescriptorFactory.fromResource(R.drawable.car_icon_us)
                                    markeroption.position(
                                        LatLng(
                                            location.latitude,
                                            location.longitude
                                        )
                                    )
                                        .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                        .icon(bitmapDescriptorFactory)
                                    driverImages[key] =
                                        getNavigator().getGMap()!!.addMarker(markeroption)!!
                                }
                            } else {
                                /* driver data is not present ,so make new */
                                driverDatas[key] = response
                                driversLocations[key] = location
                                if (typeSlug.isNullOrEmpty() || typeSlug.equals("bajaj-auto")) {
                                    if (jsonObject.has("type") && jsonObject.getString("type")
                                            .equals("bajaj-auto")
                                    ) {
                                        bitmapDescriptorFactory =
                                            BitmapDescriptorFactory.fromResource(R.drawable.roda_auto)

                                        markeroption.position(
                                            LatLng(
                                                location.latitude,
                                                location.longitude
                                            )
                                        )
                                            .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                            .icon(bitmapDescriptorFactory)
                                        driverImages[key] =
                                            getNavigator().getGMap()!!.addMarker(markeroption)!!
                                    }
                                } else {
                                    bitmapDescriptorFactory =
                                        BitmapDescriptorFactory.fromResource(R.drawable.car_icon_us)
                                    markeroption.position(
                                        LatLng(
                                            location.latitude,
                                            location.longitude
                                        )
                                    )
                                        .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                        .icon(bitmapDescriptorFactory)
                                    driverImages[key] =
                                        getNavigator().getGMap()!!.addMarker(markeroption)!!
                                }
                                firebaseHelper?.addObserverFor(key)
                            }
                            setDriverAvailability()
                        }
                    }
                } else {
                    if (driverImages.containsKey(key))
                        driverImages.remove(key)
                    if (driversLocations.containsKey(key))
                        driversLocations.remove(key)
                    if (driverDatas.containsKey(key))
                        driverDatas.remove(key)
                }
            }
        } catch (e: Exception) {

        }

    }

    override fun driverWentOffline(key: String?) {
        try {
            if (driverImages.containsKey(key)) {
                val driverPin = driverImages[key]
                driverPin!!.remove()
                driverImages.remove(key)
                driverDatas.remove(key)
                setDriverAvailability()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun driverDataUpdated(key: String?, latLng: LatLng, response: String?) {
        val location = GeoLocation(latLng.latitude, latLng.longitude)
        if (!eta.get().equals("N/A")) {
            val jsonObject = JSONObject(response)
            if (jsonObject.has("type") && jsonObject.has("service_category") && jsonObject.getString(
                    "service_category"
                ).contains(
                    "RENTAL",
                    true
                )
            ) {
                if (typeSlug?.equals(jsonObject.getString("type")) == true && !driverImages.containsKey(
                        key
                    )
                ) {
                    Log.e("driverUpdated---", "new driver : $key")
                    activeInactiveDrivers(key!!, location, response!!)
                }
            }
        }
    }

    override fun tripStatusReceived(response: String?) {}

    fun initializeFireBaseObserver() {
        firebaseHelper = FirebaseHelper(session, this, false)
        if (pickupLatLng.get() != null)
            firebaseHelper?.queryDrivers(
                pickupLatLng.get()!!,
                if (session.getString(SessionMaintainence.SEARCH_RADIUS)
                        .isNullOrEmpty()
                ) 3.0 else session.getString(SessionMaintainence.SEARCH_RADIUS)!!.toDouble()
            )
    }

    var typeSlug: String? = null
    var filteredDriversLatLng: MutableList<LatLng> = ArrayList()
    var timeList: MutableList<Float> = ArrayList()
    fun setDriverAvailability() {
        var driverCount = 0
        filteredDriversLatLng.clear()
        if (driversLocations.isEmpty()) {
            isDriversAvailable.set(false)
        } else {
            for (key in driversLocations.keys) {
                val driverData = driverDatas[key]
                try {
                    val jsonObject = JSONObject(driverData)
                    if (jsonObject.has("type") && jsonObject.has("service_category")) {
                        val typeId = jsonObject.getString("type")
                        val serviceCategory = jsonObject.getString("service_category")
                        if (typeSlug != null) {
                            if (typeSlug + "" == typeId && serviceCategory.contains(
                                    "RENTAL",
                                    true
                                )
                            ) {
                                driverCount++
                                if (jsonObject.has("l")) {
                                    val l = jsonObject.getString("l")
                                    val stringLatLng =
                                        jsonObject.getString("l").replace("[", "").replace("]", "")
                                            .replace(" ", "")
                                    val parts = stringLatLng.split(",")
                                    if (parts.size == 2) {
                                        val lat = parts[0]
                                        val lng = parts[1]
                                        val latLng = LatLng(lat.toDouble(), lng.toDouble())
                                        filteredDriversLatLng.add(latLng)
                                    }
                                }
                            }
                        }

                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                isDriversAvailable.set(driverCount > 0)
                if (isDriversAvailable.get())
                    ETACalculation()
                else {
                    eta.set("N/A")
                    getNavigator().setETA()
                    if (typeSlug.isNullOrEmpty())
                        clearMaps()
                    printD("entering--", "_e___n")
                }

            }

        }
    }

    fun clearMaps() {
        getNavigator().getGMap()!!.clear()
        driverImages.clear()
    }


//    private fun addAllDrivers() {
//        drivers.forEach { (key, value) ->
//            bitmapDescriptorFactory =
//                BitmapDescriptorFactory.fromResource(R.drawable.new_car)
//            driverBearing[key]?.toFloat()?.let {
//                markeroption.position(LatLng(value.latitude, value.longitude)).anchor(0.5f, 0.5f)
//                    .rotation(it).icon(bitmapDescriptorFactory)
//            }
//            val marker: Marker? = getNavigator().getGMap()?.addMarker(markeroption)
//            marker?.let { driverImages[key] = it }
//        }
//
//    }

    var eta = ObservableField("")

    private fun ETACalculation() {
        val distanceList: MutableList<Float> = java.util.ArrayList()
        timeList.clear()

        /* calculating distance for all LatLng in filteredDriversLatLng and adding to distanceList */
        for (latlng in filteredDriversLatLng) {
            val pickLocation = Location("pickup")
            pickLocation.latitude = pickupLatLng.get()?.latitude!!
            pickLocation.longitude = pickupLatLng.get()?.longitude!!
            val driverLocation = Location("driver")
            driverLocation.latitude = latlng.latitude
            driverLocation.longitude = latlng.longitude
            val distance = driverLocation.distanceTo(pickLocation)
            distanceList.add(distance)
        }

        /* calculating time for all distance in distance list and adding to timeList*/
        for (dist in distanceList) {
            val speed = 40
            val time = dist / 1000 / speed.toFloat()
            timeList.add(time)
        }

        /*selecting the minimumTime from timeList */
        var minTime = 0f
        for (time in timeList) {
            if (minTime == 0f)
                minTime = time
            if (time < minTime)
                minTime = time
        }
        val sMinutes = String.format(Locale.ENGLISH, "%.0f", minTime * 60)
        var iMinutes = sMinutes.toInt()
        if (iMinutes == 0) iMinutes = 1
        if (iMinutes >= 60) {
            val hours = iMinutes / 60
            val minutes = iMinutes % 60
            if (hours == 1) {
                eta.set("$hours hr $minutes mins")

            } else {
                eta.set("$hours hrs $minutes mins")

            }
        } else if (iMinutes == 1 || iMinutes == 0) {
            eta.set("${iMinutes + 2} mins")

        } else {
            eta.set("$iMinutes mins")

        }
        getNavigator().setETA()
    }

    /*
     Api call For creating new request
   */
    fun createRequest() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 2
            map.clear()
            map[Config.pick_lat] = pickupLatLng.get()!!.latitude.toString()
            map[Config.pick_lng] = pickupLatLng.get()!!.longitude.toString()
            map[Config.pick_address] = pickupAddress.get()!!
            map[Config.payment_opt] = "CASH"
            map[Config.ride_type] = "RENTAL"
            map[Config.package_item_id] = "" + rentalTypeModel?.id
            if (isPromoApplied.get())
                map[Config.promo_code] = promoCode.get()!!
            createRequestBase(map)
        } else {
            getNavigator().showNetworkUnAvailable()
        }
    }

     fun createRideLaterRequest() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 3
            map.clear()
            map[Config.package_item_id] = "" + rentalTypeModel?.id
            map[Config.ride_type] = "RENTAL"
            if (isPromoApplied.get())
                map[Config.promo_code] = promoCode.get()!!
            map[Config.is_later] = "1"
            map[Config.trip_start_time] = dateTimeRideLater.get()!!
            map[Config.payment_opt] = "CASH"
            map[Config.pick_lat] = pickupLatLng.get()!!.latitude.toString()
            map[Config.pick_lng] = pickupLatLng.get()!!.longitude.toString()
            map[Config.pick_address] = pickupAddress.get()!!
            createRequestBase(map)
        } else {
            getNavigator().showNetworkUnAvailable()
        }
    }

    fun cancelRequest() {
        apiCode = 4
        if (getNavigator().isNetworkConnected()) {
            map.clear()
            map[Config.request_id] = requestId
            cancelRequestBase(map)
        } else
            getNavigator().showNetworkUnAvailable()

    }


}