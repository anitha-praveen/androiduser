package com.cloneUser.client.drawer.rideConfirm

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.MapsHelper
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.connection.responseModels.Route
import com.cloneUser.client.connection.responseModels.StopModel
import com.cloneUser.client.connection.responseModels.TypesModel
import com.cloneUser.client.ut.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class RideConfirmVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, RideConfirmNavigator>(session, mConnect), FirebaseHelper.FirebaseObserver {

    var pickAddress = ObservableField("")
    var pickupLatLng = ObservableField<LatLng>()

    var dropAddress = ObservableField("")
    var dropLatLng = ObservableField<LatLng>()

    var stopAddress = ObservableField("")
    var stopLatLng = ObservableField<LatLng>()

    var isStopAdded = ObservableBoolean(false)


    var isPromoApplied = ObservableBoolean(false)
    var showPromoNotApplicable =
        ObservableBoolean(false) /* whether promo code chosen is applicable to selected type */
    var showPromoAppliedTotalAmount = ObservableBoolean(false)
    var promoCode = ObservableField("")

    var distanceTxt = ObservableField("")
    var stopModel: ArrayList<StopModel> = ArrayList()
    var eta = ObservableField("")

    var mPick: Marker? = null
    var mDrop: Marker? = null
    var mStops: Marker? = null


    /*
    This is temporarily made true  for testing
     */
    var isDriversAvailable = ObservableBoolean(false)
    var hashMap = HashMap<String, String>()
    var typesModel: TypesModel? = null
    var routeDest: Route? = null
    var pointsDest = mutableListOf<LatLng>()

    var polyLineDest1: Polyline? = null
    var polyLineDestDark: Polyline? = null
    var typeSlug: String? = null
    var paymentType = ObservableField("CASH")
    var isPaymentSelected = ObservableBoolean(true) /* default cash */
    var type: TypesModel.ZoneTypePrice? = null

    /*
    apiCode 0 -> get vehicle types
            1 -> create request
            2 -> create ride later request
            3 -> cancel request
     */
    private var apiCode = -1
    lateinit var bitmapDescriptorFactory: BitmapDescriptor
    var markeroption = MarkerOptions()
    var firebaseHelper: FirebaseHelper? = null
    lateinit var requestData: RequestData
    var tripPageOpened = false
    var rideType = ObservableField(1) /* ride type 1 -> normal ride , 2 -> ride later request */
    var bookLaterText = ObservableField("")


    /* newUI variables */
    var estimatedFare = ObservableField("") /* This variable is used to show estimated fare in UI */
    var promoEstimatedFare =
        ObservableField("") /* This variable is used to show promo applied estimated fare in UI */
    var bookNowText = ObservableField("")
    var dateTimeRideLater =
        ObservableField("") /* This variable is used as api param value to call Create Ride Later request */
    var rideLaterDate = ObservableField("") /* This variable is used to show date in UI */
    var rideLaterTime = ObservableField("") /* This variable is used to show time n UI */
    var rideLaterApiDate = ""  /* This variable is used as input date to get types Api ride later */
    var rideLaterApiTime = "" /* This variable is used as input time to get types Api  ride later */
    var isTripBooked = ObservableBoolean(false)
    var progressRate = ObservableField(0)
    var requestId = "" /* This variable is used in cancel request api */

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCode == 0 && response!!.data != null) {
            val res: TypesModel =
                Gson().fromJson(Gson().toJson(response.data), TypesModel::class.java)

            /*
            Loading types value to types adapter
             */
            if (res.zoneTypePrice!!.isNotEmpty()) {
                typesModel = res
                distanceTxt.set(res.zoneTypePrice[0].distance.toString())

                if (type == null) {
                    type = res.zoneTypePrice[0]
                }

                /*
                Initially no item selected from the recycler. So selecting first item of the recycler if nothing selected for create request
                 */

                if (typeSlug == null) {
                    typeSlug = res.zoneTypePrice[0].typeSlug
                    bookNowText.set("${translationModel.txt_book_now} ${type?.typeName ?: ""}")
                    bookLaterText.set("${translationModel.txt_schedule} ${type?.typeName ?: ""}")
                    estimatedFare.set("${res.currencySymble} ${Utilz.removeZero(res.zoneTypePrice[0].totalAmount?:"")}")
                    res.zoneTypePrice[0].promoTotalAmount?.let { promoEstimatedFare.set("${res.currencySymble} ${Utilz.removeZero(it)}") }
                    showPromoNotApplicable.set(res.zoneTypePrice[0].promoCode == 1 && res.zoneTypePrice[0].promoTotalAmount == null)
                } else {
                    if (res.zoneTypePrice.any { it.typeSlug == typeSlug }) {
                        type = res.zoneTypePrice.find { it.typeSlug == typeSlug }
                        bookNowText.set("${translationModel.txt_book_now} ${type?.typeName ?: ""}")
                        bookLaterText.set("${translationModel.txt_schedule} ${type?.typeName ?: ""}")
                        estimatedFare.set("${res.currencySymble} ${Utilz.removeZero(type?.totalAmount?:"")}")
                        type?.promoTotalAmount?.let { promoEstimatedFare.set("${res.currencySymble} ${Utilz.removeZero(type?.promoTotalAmount?:"")}") }
                        showPromoNotApplicable.set(type?.promoCode == 1 && type?.promoTotalAmount == null)
                    }
                }
                showPromoAppliedTotalAmount.set(isPromoApplied.get() && !showPromoNotApplicable.get())
                getNavigator().addList(res.zoneTypePrice)

                if (firebaseHelper == null)
                    initializeFireBaseObserver()
                else
                    setDriverAvailability()
            }


        } else if (apiCode == 1 && response!!.success == true) {
            getNavigator().closeApiWaiting()
            val resp: RequestData =
                Gson().fromJson(Gson().toJson(response.data), RequestData::class.java)
            if (resp.data != null)
                requestId = resp.data?.id ?: ""
            isTripBooked.set(true)
            getNavigator().runProgressBar()
            buildBoundForPickup()
        } else if (apiCode == 2) {
            getNavigator().openSuccess()
        } else if (apiCode == 3) {
            if (response!!.success == true)
                isTripBooked.set(false)
            getNavigator().stopProgressBar()
            getNavigator().bind().customSeek.progress = 0
            getNavigator().getGMap()?.let { buildBound(it) }
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        if (apiCode == 1) {
            if (e.code == 2001) {
                requestId = e.exception?:""
                getNavigator().closeApiWaiting()
                getNavigator().goToNodriverFound()
            }
            else {
                getNavigator().closeApiWaiting()
                getNavigator().showMessage(e.exception?:"")
            }

        } else if (apiCode == 3) {
            if (e.exception?.equals("The given data was invalid.", true) == true)
                getNavigator().closeFragment()
            else
                getNavigator().bind().customSeek.progress = 0
        } else
            getNavigator().showMessage(e.exception ?: "Please try again")
    }

    fun getTypes(rideType: String, rideDate: String, rideTime: String) {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 0
            hashMap.clear()
            hashMap[Config.pickup_lat] = pickupLatLng.get()!!.latitude.toString()
            hashMap[Config.pickup_long] = pickupLatLng.get()!!.longitude.toString()
            hashMap[Config.drop_lat] = dropLatLng.get()!!.latitude.toString()
            hashMap[Config.drop_long] = dropLatLng.get()!!.longitude.toString()
            hashMap[Config.pickup_address] = pickAddress.get()!!
            hashMap[Config.drop_address] = dropAddress.get()!!
            hashMap[Config.ride_type] = rideType
            hashMap[Config.ride_date] = rideDate
            hashMap[Config.ride_time] = rideTime
            if (stopModel.size > 0)
                hashMap[Config.stops] = Gson().toJson(stopModel)
            if (isPromoApplied.get())
                hashMap[Config.promo_code] = promoCode.get()!!
            getVehicleTypeBase(hashMap)

        }

    }

    fun onclickPayment() {
        if (!typesModel?.paymentTypes.isNullOrEmpty())
            getNavigator().openPaymentMethodDialog(typesModel?.paymentTypes!!.toMutableList())
    }

    fun onClickPromoCode() {
        if (isLoading.value == false)
            getNavigator().openPromoDialog()
    }

    fun onClickSchedule() {
        getNavigator().openScheduleBottomSheet()
    }


    fun onClickConfirm() {
        if (typeSlug != null) {
            if (rideType.get() == 1)
                createRequest()
            else
                createRideLaterRequest()
        }
    }


    fun setPickupDropMarkers() {
        addPickDropMarkers()
        when {
            encodedPoints.isNotEmpty() -> drawDirection(encodedPoints)
            isStopAdded.get() -> getEncodedPointsForMultipleDestAndDraw()
            else -> getEncodedPointsAndDrawDirection()
        }
    }


    fun buildBound(googleMap: GoogleMap) {


        val width: Int = getNavigator().getCtx().resources.displayMetrics.widthPixels
        val height: Int = getNavigator().getCtx().resources.displayMetrics.heightPixels

        val minMetric: Int = width.coerceAtMost(height);
        val padding: Int = (minMetric * 0.20).toInt()

        if (pickupLatLng.get() != null && dropLatLng.get() != null) {
            if (stopLatLng.get() != null) {
                val boundsBuilder = LatLngBounds.builder()
                    .include(pickupLatLng.get()!!)
                    .include(dropLatLng.get()!!)
                    .include(
                        stopLatLng.get()!!
                    )
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(), padding
                    )
                )
            } else {
                val boundsBuilder = LatLngBounds.builder()
                    .include(pickupLatLng.get()!!)
                    .include(dropLatLng.get()!!)
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(), padding
                    )
                )
            }
        } else {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pickupLatLng.get()!!, 20f
                )
            )
        }
    }

    fun buildBoundForPickup() {
//        val width: Int = getNavigator().getCtx().resources.displayMetrics.widthPixels
//        val paddingWidth = (width * 0.20).toInt()
//
//        val boundsBuilder = LatLngBounds.builder()
//            .include(pickupLatLng.get()!!)
//        getNavigator().getGMap()?.animateCamera(
//            CameraUpdateFactory.newLatLngBounds(
//                boundsBuilder.build(), paddingWidth
//            )
//        )
        getNavigator().getGMap()?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                pickupLatLng.get()!!, 16f
            )
        )

    }

    /*
    The below method is for drawing path from pickup to drop
     */

    var encodedPoints = ""
    private fun getEncodedPointsAndDrawDirection() {
        if (pickupLatLng.get() != null && dropLatLng.get() != null) {
            mapsHelper. getDrawPath(
                pickupLatLng.get()!!.latitude.toString() + "," + pickupLatLng.get()!!.longitude,
                dropLatLng.get()!!.latitude.toString() + "," + dropLatLng.get()!!.longitude,
                false,session.getString(SessionMaintainence.DIRECTION_DYNAMIC_KEY))!!.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(
                    call: Call<JsonObject?>,
                    response: Response<JsonObject?>
                ) {
                    if (response.body() != null) {
                        routeDest = Route()
                        Utilz.parseRoute(response.body()!!, routeDest!!)
                        routeDest?.polyPoints?.let { encodedPoints = it }
                        Log.e(RideConfirmFragment.TAG, "Encoded points : $encodedPoints")
                        drawDirection(encodedPoints)
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    getNavigator().showMessage(t.message!!)
                }
            })
        }
    }



    private fun getEncodedPointsForMultipleDestAndDraw() {
        if (pickupLatLng.get() != null && stopLatLng.get() != null && dropLatLng.get() != null) {
            mapsHelper.getDrawPathMultipleStops(
                pickupLatLng.get()?.latitude.toString() + "," + pickupLatLng.get()?.longitude,
                dropLatLng.get()?.latitude.toString() + "," + dropLatLng.get()?.longitude,
                stopLatLng.get()?.latitude.toString() + "," + stopLatLng.get()?.longitude,
                false,session.getString(SessionMaintainence.DIRECTION_DYNAMIC_KEY)
            )!!.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(
                    call: Call<JsonObject?>,
                    response: Response<JsonObject?>
                ) {
                    if (response.body() != null) {
                        routeDest = Route()
                        Utilz.parseRoute(response.body()!!, routeDest!!)
                        routeDest?.polyPoints?.let { encodedPoints = it }
                        printE(RideConfirmFragment.TAG, "Encoded points : $encodedPoints")
                        drawDirection(encodedPoints)
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    getNavigator().showMessage(t.message!!)
                }
            })
        }

    }


    private fun removeIfPolyAdded() {
        if (polyLineDest1 != null)
            polyLineDest1!!.remove()
        if (polyLineDestDark != null)
            polyLineDestDark!!.remove()
    }


    /*
       Api call For creating new request
     */
    private fun createRequest() {
        if (getNavigator().isNetworkConnected()) {
            getNavigator().showApiWaiting()
            apiCode = 1
            hashMap.clear()
            hashMap[Config.pick_lat] = pickupLatLng.get()!!.latitude.toString()
            hashMap[Config.pick_lng] = pickupLatLng.get()!!.longitude.toString()
            hashMap[Config.pick_address] = pickAddress.get()!!
            hashMap[Config.drop_lat] = dropLatLng.get()!!.latitude.toString()
            hashMap[Config.drop_lng] = dropLatLng.get()!!.longitude.toString()
            hashMap[Config.drop_address] = dropAddress.get()!!
            hashMap[Config.vehicle_type] = typeSlug!!
            hashMap[Config.payment_opt] = "CASH"
            hashMap[Config.ride_type] = "LOCAL"
            if(ContactList.isMySelf){
                hashMap[Config.booking_for] = "MYSELF"
            }else{
                hashMap[Config.booking_for] = "OTHERS"
                hashMap[Config.others_name] = ContactList.name ?: ""
                hashMap[Config.others_number] = ContactList.phone ?: ""
            }
            if(driverNotes.isNotEmpty())
                hashMap[Config.driver_notes] = driverNotes
            hashMap[Config.poly_string] = encodedPoints
            if (stopModel.size > 0)
                hashMap[Config.stops] = Gson().toJson(stopModel)
            if (isPromoApplied.get())
                hashMap[Config.promo_code] = promoCode.get()!!
            createRequestBase(hashMap)
        } else {
            getNavigator().showNetworkUnAvailable()
        }
    }

    private fun createRideLaterRequest() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 2
            hashMap.clear()
            hashMap[Config.pick_address] = pickAddress.get()!!
            hashMap[Config.is_later] = "1"
            hashMap[Config.drop_address] = dropAddress.get()!!
            hashMap[Config.payment_opt] = paymentType.get()!!
            hashMap[Config.vehicle_type] = typeSlug!!
            hashMap[Config.pick_lat] = pickupLatLng.get()!!.latitude.toString()
            hashMap[Config.pick_lng] = pickupLatLng.get()!!.longitude.toString()
            hashMap[Config.trip_start_time] = dateTimeRideLater.get()!!
            hashMap[Config.drop_lat] = dropLatLng.get()!!.latitude.toString()
            hashMap[Config.drop_lng] = dropLatLng.get()!!.longitude.toString()
            hashMap[Config.ride_type] = "LOCAL"
            if(ContactList.isMySelf){
                hashMap[Config.booking_for] = "MYSELF"
            }else{
                hashMap[Config.booking_for] = "OTHERS"
                hashMap[Config.others_name] = ContactList.name ?: ""
                hashMap[Config.others_number] = ContactList.phone ?: ""
            }
            hashMap[Config.poly_string] = encodedPoints
            if(driverNotes.isNotEmpty())
                hashMap[Config.driver_notes] = driverNotes
            if (stopModel.size > 0)
                hashMap[Config.stops] = Gson().toJson(stopModel)
            if (isPromoApplied.get())
                hashMap[Config.promo_code] = promoCode.get()!!
            createRequestBase(hashMap)
        } else {
            getNavigator().showNetworkUnAvailable()
        }
    }

    /*
    Fire base observer methods
     */
    var driversLocations: HashMap<String, GeoLocation> = HashMap()
    var driverBearing: HashMap<String, Double> = HashMap()
    private val driverImages: HashMap<String, Marker> = HashMap()
    var driverDatas = HashMap<String, String>()

    override fun driverEnteredFence(key: String?, location: GeoLocation?, response: String?) {
        printE(RideConfirmFragment.TAG, "driverEnteredFence")
        activeInactiveDrivers(key!!, location!!, response!!)
    }

    override fun driverExitedFence(key: String?, response: String?) {
        printE(RideConfirmFragment.TAG, "driverExitedFence")
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

    override fun driverWentOffline(key: String?) {
        printE(RideConfirmFragment.TAG, "driverWentOffline")
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
        printE(RideConfirmFragment.TAG, "driverDataUpdated")
        val location = GeoLocation(latLng.latitude, latLng.longitude)
        if (!eta.get().equals("N/A")) {

            val jsonObject = JSONObject(response)
            if (jsonObject.has("type")) {
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

    private fun bitMapFromVector(vectorResID: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(getNavigator().getCtx(), vectorResID)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun activeInactiveDrivers(key: String, location: GeoLocation, response: String) {
        printE(RideConfirmFragment.TAG, "activeInactiveDrivers")
        try {
            val jsonObject = JSONObject(response)
            if (jsonObject.has("is_active")) {
                val isActive = jsonObject.getBoolean("is_active")
                if (isActive) {
                    val updatedAt = jsonObject.getLong("updated_at")
                    val currentTime = Date().time
                    val diff = (currentTime - updatedAt) / 1000
                    if (diff < 5 * 60) {
                        /*here driver passed all conditions
                          check whether he is already in our list
                          If he is already in our list -> update his data , location and remove his marker and show marker with updated data
                          else add his data , location and remove his marker and show marker with updated data
                         */
                        val bearing = jsonObject.getDouble("bearing")
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
//                                bitmapDescriptorFactory =
//                                    BitmapDescriptorFactory.fromResource(R.drawable.new_car)
                                markeroption.position(LatLng(location.latitude, location.longitude))
                                    .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                    .icon(bitMapFromVector(R.drawable.car_icon_us))
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
//                                bitmapDescriptorFactory =
//                                    BitmapDescriptorFactory.fromResource(R.drawable.new_car)
                                markeroption.position(LatLng(location.latitude, location.longitude))
                                    .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                    .icon(bitMapFromVector(R.drawable.car_icon_us))
                                driverImages[key] =
                                    getNavigator().getGMap()!!.addMarker(markeroption)!!
                            }
                            firebaseHelper?.addObserverFor(key)
                        }

                        setDriverAvailability()
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

    fun initializeFireBaseObserver() {
        printE(RideConfirmFragment.TAG, "initializeFireBaseObserver")
        firebaseHelper = FirebaseHelper(session, this, false)
        if (pickupLatLng.get() != null)
            firebaseHelper?.queryDrivers(pickupLatLng.get()!!,
                if (session.getString(SessionMaintainence.SEARCH_RADIUS)
                        .isNullOrEmpty()
                ) 3.0 else session.getString(SessionMaintainence.SEARCH_RADIUS)!!.toDouble()
            )
    }

    fun setDriverAvailability() {
        printE(RideConfirmFragment.TAG, "setDriverAvailability")
        printE("setDriverAvailability", "typeSLug---_$typeSlug")
        var driverCount = 0
        filteredDriversLatLng.clear()
        if (driversLocations.isEmpty()) {
            isDriversAvailable.set(false)
        } else {
            for (key in driversLocations.keys) {
                val driverData = driverDatas[key]
                try {
                    val jsonObject = JSONObject(driverData!!)

                    Log.e("jsonObj---", "__$jsonObject")
                    if (jsonObject.has("type")) {
                        val typeId = jsonObject.getString("type")
                        printE("filterTypes---", "__$typeSlug")

                        if (typeSlug != null) {
                            if (typeSlug + "" == typeId) {
                                printE("types--", "__" + driverImages[key])

                                driverCount++
                                if (jsonObject.has("l")) {
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
                    printE("exception----", "___" + e.message)
                    e.printStackTrace()
                }

            }

            isDriversAvailable.set(driverCount > 0)
            if (isDriversAvailable.get())
                etaCalculation()
            else {
                eta.set("N/A")
                getNavigator().setETA()
                if (typeSlug.isNullOrEmpty())
                    clearMaps()
                printD("entering--", "_e___n")
            }

        }
    }

    var filteredDriversLatLng: MutableList<LatLng> = ArrayList()
    var timeList: MutableList<Float> = ArrayList()
    private fun etaCalculation() {
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
        } else {
            eta.set("" + (iMinutes + 2) + "mins")
        }
        getNavigator().setETA()
    }


//    private fun removeAllDriver() {
//        driverImages.forEach { (key, value) ->
//            val marker: Marker = value
//            marker.remove()
//            driverImages.remove(key)
//        }
//    }

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

    fun notifyWaitingProgress() {
        val intent = Intent(Config.RECEIVE_TRIP_STATUS)
        LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
    }

    fun onClickBack() {
        getNavigator().onClickBack()
    }

    private fun addPickDropMarkers() {
        if (pickupLatLng.get() != null) {
            bitmapDescriptorFactory =
                Utilz.getBitmapDescriptor(getNavigator().getCtx(), R.drawable.road_marker)
            mPick = getNavigator().getGMap()!!.addMarker(
                MarkerOptions().position(pickupLatLng.get()!!).title("Pickup Point")
                    .icon(bitmapDescriptorFactory)
            )
        }

        if (dropLatLng.get() != null) {
            bitmapDescriptorFactory =
                Utilz.getBitmapDescriptor(getNavigator().getCtx(), R.drawable.red_marker)
            mDrop = getNavigator().getGMap()!!.addMarker(
                MarkerOptions().position(dropLatLng.get()!!).title("Drop Point")
                    .icon(bitmapDescriptorFactory)
            )
        }

        if (isStopAdded.get()!!) {
            if (stopLatLng.get() != null) {
                bitmapDescriptorFactory = Utilz.getBitmapDescriptor(
                    getNavigator().getCtx(),
                    R.drawable.red_marker
                )
                mStops = getNavigator().getGMap()!!.addMarker(
                    MarkerOptions().position(stopLatLng.get()!!).title("Stop Point")
                        .icon(bitmapDescriptorFactory)
                )
                mPick!!.setAnchor(0.5f, 0.5f)
            }
        }
    }

    var lineOptionsDest1: PolylineOptions? = null
    var lineOptionDesDark: PolylineOptions? = null
    private fun drawDirection(encodedPoints: String?) {
        if (encodedPoints != null) {
            pointsDest.clear()
            val polyz: List<LatLng> = Utilz.decodeOverviewPolyLinePoints(encodedPoints)
            pointsDest.addAll(polyz)

            lineOptionsDest1 = PolylineOptions()
            lineOptionDesDark = PolylineOptions()
            lineOptionsDest1!!.geodesic(true)
            lineOptionDesDark!!.geodesic(true)

            polyLineDest1?.remove()
            pointsDest.let { lineOptionsDest1?.addAll(it) }
            lineOptionsDest1?.width(10f)
            lineOptionsDest1?.startCap(SquareCap())
            lineOptionsDest1?.endCap(SquareCap())
            lineOptionsDest1?.jointType(JointType.ROUND)
            // if (getNavigator().isAddedInAct()) {
            lineOptionsDest1?.color(
                ContextCompat.getColor(getNavigator().getCtx(), R.color.colorPrimary)
            )
            lineOptionDesDark?.color(
                ContextCompat.getColor(getNavigator().getCtx(), R.color.colorPrimary)
            )
            // }

            try {
                lineOptionsDest1?.let {
                    lineOptionDesDark?.let { dark ->
                        polyLineDest1 = getNavigator().getGMap()?.addPolyline(it)
                        polyLineDestDark = getNavigator().getGMap()?.addPolyline(dark)
//                        animatePolyLine(polyz)

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            /**
             * Options for the polyline of the route between Cairo and Alexandria.
             */
//            val routePolyLineOptions = PolylineOptions()
//            routePolyLineOptions.width(10F)
//            routePolyLineOptions.startCap(SquareCap())
//            routePolyLineOptions.endCap(SquareCap())
//            routePolyLineOptions.jointType(JointType.ROUND)
//
//            /**
//             * Initializing the [GradientPolyLine] with the custom polyline options, start
//             * and end colors to make the gradient color calculation.
//             */
//
//
//            if(!getNavigator().getJob().start()){
//                val gradientPolyLine =
//                    GradientPolyLine(map = getNavigator().getGMap()!!, job = getNavigator().getJob())
//                        .setPolylineOptions(polylineOptions = routePolyLineOptions)
//                        .setDelayTime(delayTime = 10L)
//                        .setStartColor(
//                            startColor = ContextCompat.getColor(
//                                getNavigator().getCtx(),
//                                R.color.colorPrimary
//                            )
//                        )
//                        .setEndColor(
//                            endColor = ContextCompat.getColor(
//                                getNavigator().getCtx(),
//                                R.color.clr_red
//                            )
//                        )
//
//                if (pointsDest.size != null){
//                    gradientPolyLine.drawPolyline(pointsDest) {
//                        getNavigator().getGMap()?.uiSettings?.setAllGesturesEnabled(true)
//                    }
//                }
//
//            }
//
//            /**
//             * Getting the route result and then draw the polyline.
//             */
        }
    }

    private fun animatePolyLine(latLng: List<LatLng>) {
        polyLineDest1?.let {
            val animator = ValueAnimator.ofInt(0, 100)
            animator.duration = 1500
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener { anim ->
                val latLngList = it.points
                val initialPointSize = latLngList.size
                val animatedValue = anim.animatedValue as Int
                val newPoints = animatedValue * latLng.size / 100
                if (initialPointSize < newPoints) {
                    latLngList.addAll(latLng.subList(initialPointSize, newPoints))
                    it.points = latLngList
                }
            }
            animator.addListener(polyLineAnimationListener)
            animator.start()
        }
    }

    private var polyLineAnimationListener: Animator.AnimatorListener =
        object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                polyLineDest1?.let { line ->
                    polyLineDestDark?.let { lineDark ->
                        val blackLatLng: MutableList<LatLng> = line.points
                        val greyLatLng: MutableList<LatLng> = lineDark.points
                        greyLatLng.clear()
                        greyLatLng.addAll(blackLatLng)
                        blackLatLng.clear()
                        line.points = blackLatLng
                        lineDark.points = greyLatLng
                        line.zIndex = 2f
                        animator.start()
                    }

                }

            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        }


    fun onClickEstimatedPrice() {
        getNavigator().showSelectedPriceDetails()
    }

    fun goToSearchPlace(view:View){
        getNavigator().gotoSearchplace()
    }

    fun cancelRequest() {
        apiCode = 3
        if (getNavigator().isNetworkConnected()) {
            hashMap.clear()
            hashMap[Config.request_id] = requestId
            cancelRequestBase(hashMap)
        } else
            getNavigator().showNetworkUnAvailable()
    }

    fun showCallDialog(view: View) {
        getNavigator().openCallBottomSheet()
    }

    fun changeDropAddress(view: View){

    }

    var dNotes: BottomSheetDialog? = null
    var driverNotes = ""
    fun onClickDriverNotes(view: View) {
        if (dNotes?.isShowing == true)
            dNotes?.dismiss()
        dNotes =
            BottomSheetDialog(view.context, R.style.AppBottomSheetDialogTheme)
        dNotes?.setContentView(R.layout.driver_notes_layout)
        val notesEt =  dNotes?.findViewById<EditText>(R.id.notesEt)
        val submit = dNotes?.findViewById<Button>(R.id.submit)
        notesEt?.setText(driverNotes)
        submit?.text = translationModel.text_submit
        submit?.setOnClickListener {
            driverNotes = notesEt?.text.toString()
            dNotes?.dismiss()
        }
        dNotes?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dNotes?.show()
    }

    fun clearMaps() {
        getNavigator().getGMap()!!.clear()
        setPickupDropMarkers()
        driverImages.clear()
    }

    var rider = ObservableField(ContactList.name)
    fun onClickRiderSelection() {
        getNavigator().openRiderSelection()
    }


}

@BindingAdapter("progress")
fun setProgress(p: ProgressBar, value: Int) {
    p.progress = value
}