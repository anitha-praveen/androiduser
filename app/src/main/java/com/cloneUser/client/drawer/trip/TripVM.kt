package com.cloneUser.client.drawer.trip

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.regions.Region
//import com.amazonaws.services.s3.AmazonS3
//import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.MapsHelper
import com.cloneUser.client.connection.responseModels.CancelReason
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.connection.responseModels.RequestInProgress
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.ut.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Integer.parseInt
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class TripVM @Inject constructor(
    val application: Application,
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, TripNavigator>(session, mConnect),
    FirebaseHelper.FirebaseObserver {
    companion object {
        const val TAG = "TripVM"
    }

    var pickupAddress = ObservableField("")
    var dropAddress = ObservableField("")
    var isTripStared = ObservableBoolean(false)
    var isDriverArrived = ObservableBoolean(false)
    var userImageURL = ObservableField("")
    var waitingTime = ObservableField("")
    var tripTime = ObservableField("")
    var statusOfTrip = ObservableField("")
    var driverImageURL = ObservableField("")
    var carModel = ObservableField("")
    var driverFirstName = ObservableField("")
    var driverLastName = ObservableField("")
    var rating = ObservableField("")
    var tripOTP = ObservableField("")
    var carNumber = ObservableField("")
    var distance = ObservableField("")
    var paymentType = ObservableField("")
    var enablePromoOption = ObservableBoolean(false)
    var isPromoDone = ObservableBoolean(false)
    var pickupLatLng = ObservableField<LatLng>()
    var dropLatLng = ObservableField<LatLng>()
    var driverCurrentLatLng = ObservableField<LatLng>()
    var driverOldLatLng = ObservableField<LatLng>()
    var requestData: RequestData.Data? = null
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var googleMap: GoogleMap? = null
    lateinit var firebaseHelper: FirebaseHelper
    var requestId: String? = null
    var baseResponse: BaseResponse? = null
    var tripObserverData: JSONObject? = null
    var lineOptionsDest1: PolylineOptions? = null
    var lineOptionDesDark: PolylineOptions? = null
    var polyLineDest1: Polyline? = null
    var polyLineDestDark: Polyline? = null
    var driverCarMarker: Marker? = null
    var pickUpMarker: Marker? = null
    var dropMarker: Marker? = null
    var pickupToDropPathDrawn = false
    val showetatxt = ObservableField("")
    val showEta = ObservableBoolean(false)
    val foundTxt = ObservableField("")

    val reqNumber = ObservableField("")

    //var waitingTimeVisibility = ObservableBoolean(false)
    val timeType = ObservableField("")
    val showWaitingTimeTxt = ObservableBoolean(false)

    var hashMap: HashMap<String, String> = HashMap()
    var apiCode = 0

    val stopAddress = ObservableField("")
    val stopLat = ObservableField("")
    val stopLong = ObservableField("")

    val isStopAdded = ObservableBoolean(false)


    val vehicalTypeimg = ObservableField("")

    val vehicalTypeTxt = ObservableField("")

    val showArriveInTxt = ObservableField("")

    val isPromoApplied = ObservableBoolean(false)

    /*
    apiCode 1 -> get cancel reasons api
            2 -> cancel the request
            3 -> request in Progress
     */
    var invoiceOpened = false
    var reason: CancelReason.Reason? = null
    var latLngTaken = false
    var userLat = ""
    var userLng = ""
    var userAddress = ""
    var driverAddress = ""
    var cancelAlertShown = false
    var showDrop = ObservableBoolean(false)
    var showMeter = ObservableBoolean(false)

    fun onClickCancel() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 1
            hashMap.clear()
            if (requestData?.isDriverArrived != null && requestData?.isDriverArrived == 1)
                hashMap[Config.arrive_status] = "1"
            else
                hashMap[Config.accept_status] = "1"
            getCancelReasonsBase(hashMap)
        }
    }

    fun cancelRequest() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 2
            hashMap.clear()
            hashMap[Config.request_id] = requestId!!
            hashMap[Config.reason] = "" + reason?.id!!
//            hashMap[Config.custom_reason]
            hashMap[Config.user_lat] = userLat
            hashMap[Config.user_lng] = userLng
            hashMap[Config.driver_lat] =
                if (driverCurrentLatLng.get() != null) "" + driverCurrentLatLng.get()?.latitude else userLat
            hashMap[Config.driver_lng] =
                if (driverCurrentLatLng.get() != null) "" + driverCurrentLatLng.get()?.longitude else userLng
            hashMap[Config.user_location] = userAddress
            hashMap[Config.driver_location] = driverAddress
            cancelRequestBase(hashMap)
        }
    }

    fun callReqProgress() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 3
            requestInProgressBase()
        } else
            getNavigator().showNetworkUnAvailable()
    }

    private fun uploadImage() {
        /* Upload image of person for night time*/
        apiCode = 4
        requestbody.clear()
        requestId?.let {
            requestbody[Config.request_id] = RequestBody.create("text/plain".toMediaTypeOrNull(), it)
            if (realPath != null) {
                val reqFile =
                    realFile?.let { it1 -> RequestBody.create("image/*".toMediaTypeOrNull(), it1) }
                body = reqFile?.let { it1 ->
                    MultipartBody.Part.createFormData(Config.images, realFile!!.name,
                        it1
                    )
                }
                uploadNightImageBase()
            }
        }
    }

    fun requestRetakeDriver(){
        apiCode = 5
        hashMap.clear()
        requestId?.let {
            hashMap[Config.request_id] = it
            requestRetakeBase(hashMap)
        }

    }

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCode == 1) {
            response?.data?.let { data ->
                val res: CancelReason? =
                    Gson().fromJson(Gson().toJson(data), CancelReason::class.java)
                res?.let { cancelReason ->
                    cancelReason.reasons?.let {
                        if (it.isNotEmpty())
                            getNavigator().cancelReasonDialog(it)
                    }
                }
            }
        } else if (apiCode == 2) {
            getNavigator().close()
        } else if (apiCode == 3) {
            if (response != null) {
                val requestInProgress =
                    Gson().fromJson(Gson().toJson(response.data), RequestInProgress::class.java)
                if (requestInProgress.trips != null) {
                    requestData = requestInProgress.trips.data
                    setInitialData()
                } else {
                    /* no trip redirect home */
                    getNavigator().close()
                }
            }
        }else if(apiCode ==4){
            Log.e("photoUploaded",Gson().toJson(response))
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun changePickupAddress() {
        if (!isDriverArrived.get() && !requestId.isNullOrEmpty())
            getNavigator().goToSearchPlace("pickup")
        else if (isDriverArrived.get())
            getNavigator().showMessage(translationModel.txt_driver_arrived_pickup ?: "")
    }

    fun changeDropAddress() {
        /*
        Condition check needed
         */
        if (!requestId.isNullOrEmpty())
            getNavigator().goToSearchPlace("drop")

    }

    fun onMenuClick() {
        getNavigator().openSideMenu()
    }

    fun onClickCall(view: View) {
        requestData?.driver?.phoneNumber?.let {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:$it")
            view.context.startActivity(callIntent)
        }

    }

    fun onClickSms(view: View) {
        requestData?.driver?.phoneNumber?.let {
            val sendIntent = Intent(Intent.ACTION_VIEW)
            sendIntent.data = Uri.parse("sms:$it")
            view.context.startActivity(sendIntent)
        }
    }


    var meterImg = ""
    fun onClickMeter() {
        (getNavigator().getAct() as DrawerActivity).showMeterPic(meterImg,requestData?.startKM?:"")
    }

    var isOutstation = ObservableBoolean(false)
    var isUserPicUpload = false /* holds the value for if photo of user uploaded or not */
    var isDriverUploadImage = false /* holds the value for if photo of driver uploaded or not */
    var isNightPhotoSkip = false /* holds the value for if photo of user upload skipped by driver */
    var nightImageDriver = ""
    var isRetake = false
    fun setInitialData() {
        requestData?.let { request ->
            requestId = request.id
            request.pickAddress?.let { pickupAddress.set(it) }
            request.dropAddress?.let { dropAddress.set(it) }
            request.pickLat?.let { lat ->
                request.pickLng?.let {
                    pickupLatLng.set(
                        LatLng(
                            lat,
                            it
                        )
                    )
                }
            }
            request.requestNumber.let { reqNumber.set(it) }
            request.dropLat?.let { lat -> request.dropLng?.let { dropLatLng.set(LatLng(lat, it)) } }
            request.driverOverallRating?.let { rating.set("" + round(it, 1)) }
            request.paymentOpt?.let { paymentType.set(it) }
            request.vehical_type?.let { vehicalTypeTxt.set(it) }
            request.isTripStart?.let { isTripStared.set(it == 1) }
            request.tripShareLink?.let { shareTripLink = it }
            request.serviceCategory?.let {
                if (!it.equals("RENTAL", true) && isTripStared.get())
                    showDrop.set(true)

                if (it.equals("OUTSTATION", true))
                isOutstation.set(true)
            }
            request.promo_applied?.let { isPromoApplied.set(it) }
            request.vehicleTypeImg?.let { vehicalTypeimg.set(it) }

            request.driver?.let { driver ->
                driver.profilePic?.let {  driverImageURL.set(it) }
                driver.firstname?.let { driverFirstName.set(it) }
                driver.lastname?.let { driverLastName.set(it) }
            }
            if (request.locationApprove != null)
                if (request.locationApprove == 1)
                    getNavigator().openApproveAlert()
            request.user?.let { it.profile_pic?.let { userImageURL.set(request.user.profile_pic) } }
            request.requestOtp?.let { tripOTP.set(translationModel.text_otp + " - " + it.toString()) }
            request.carDetails?.let { carDetail ->
                carDetail.carModel?.let { carModel.set(it) }
                carDetail.carNumber?.let { carNumber.set(it) }
            }
            if (request.isCompleted != null && request.isCompleted == 1) {
                session.saveString(SessionMaintainence.REQ_ID, "")
                statusOfTrip.set(translationModel.Txt_TripCompleted)
                request.requestBill?.let { bill ->
                    bill.data?.let {
                        if (!invoiceOpened) {
                            invoiceOpened = true
                            getNavigator().invoice(request)
                        }
                    }
                }
            } else if (request.isCancelled != null && request.isCancelled == 1 && !cancelAlertShown) {
                cancelAlertShown = true
                getNavigator().showTripCancelled()
            } else if (request.isTripStart != null && request.isTripStart == 1) {
                statusOfTrip.set(translationModel.txt_driver_started_destination)
                isDriverArrived.set(true)
                showEta.set(false)
                foundTxt.set(translationModel.txt_enjoy_ride)
                showetatxt.set(translationModel.txt_arrived)
            } else if (request.isDriverArrived != null && request.isDriverArrived == 1) {
                foundTxt.set(translationModel.txt_found_driver)
                statusOfTrip.set(translationModel.txt_driver_arrived_pickup)
                isDriverArrived.set(true)
                showArriveInTxt.set(translationModel.txt_arrived)
                showEta.set(false)
            } else {
                showEta.set(true)
                showArriveInTxt.set(translationModel.txt_arrive_in)
                foundTxt.set(translationModel.txt_found_driver)
                statusOfTrip.set(translationModel.txt_driver_accepted)
            }

            if (request.isTripStart != null && request.isTripStart == 1 && !pickupToDropPathDrawn) {
                requestData?.polyString?.let { drawMultiplePaths() }
            }
        }
        if (!(requestData?.stops?.address.isNullOrEmpty())) {
            isStopAdded.set(true)
            stopAddress.set(requestData?.stops?.address ?: "")
            stopLat.set(requestData?.stops?.latitude ?: "")
            stopLong.set(requestData?.stops?.longitude ?: "")
        } else
            isStopAdded.set(false)
        requestData?.startNightTime?.let { initialTime = it }
        requestData?.endNightTime?.let { endTime = it }
        requestData?.userUploadImage?.let { isUserPicUpload = it }
        requestData?.skipNightPhoto?.let { isNightPhotoSkip = it }
        requestData?.driverUploadImage?.let { isDriverUploadImage = it }
        requestData?.nightPhotoDriver?.let { nightImageDriver = it }
        requestData?.tripStartKmImage?.let {
            showMeter.set(it.isNotEmpty())
            meterImg = it
        }

        if (isDriverUploadImage)
            checkForImageUpload()
    }

    fun checkForImageUpload() {
        Log.e("EnteredCheck","isNightPhotoSkip : ${isNightPhotoSkip}   isUserPicUpload : ${isUserPicUpload}    ifDispatch :${requestData?.ifDispatch}")
        if (!cancelAlertShown && shouldITakePhoto() && !isNightPhotoSkip && !isUserPicUpload && requestData?.ifDispatch != null && requestData?.ifDispatch != 1) {
            getNavigator().showTakePhotoDialog()
        }
    }

    private var initialTime = ""
    private var endTime = ""
    private fun shouldITakePhoto(): Boolean {
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(Date())
        return if (Utilz.isTimeBetweenTwoTime(
                initialTime,
                "23:59:59",
                currentTime
            )
        ) true else Utilz.isTimeBetweenTwoTime("00:00:00", endTime, currentTime)
    }

    /*
    The below method setBound from pickup to drop or driver's location to pickup according to the parameter given to it
    This method is called from drawPath()
     */
    private fun buildBound(from: LatLng?, to: LatLng?) {
        val boundsBuilder = LatLngBounds.builder()
        from?.let { f ->
            to?.let {
                boundsBuilder.include(f).include(it)
                getNavigator().getAct().runOnUiThread {
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            boundsBuilder.build(), 30
                        )
                    )
                }

            }
        }
    }

    /*
    The below method draw path from pickup to drop or driver's location to pickup according to the parameter given to it
    This method is called from tripStatusReceived()
     */
//    private fun drawPath(from: LatLng, to: LatLng) {
//        if (getNavigator().isAddedInAct()) {
//            polyLineDest1 = null
//            polyLineDestDark = null
//            isPathDrawn = true
//            buildBound(from, to)
//            if (polyLineDest1 == null && polyLineDestDark == null) {
//                mapsHelper.getDrawPath(
//                    from.latitude.toString() + "," + from.longitude,
//                    to.latitude.toString() + "," + to.longitude,
//                    false,
//                    Config.PlaceApi_key
//                )!!.enqueue(object : Callback<JsonObject?> {
//                    override fun onResponse(
//                        call: Call<JsonObject?>,
//                        response: Response<JsonObject?>
//                    ) {
//                        if (response.body() != null) {
//                            routeDest = Route()
//                            Utilz.parseRoute(response.body()!!, routeDest!!)
//                            val step: ArrayList<Step> = routeDest!!.getListStep()
//                            println("step size=====> " + step.size)
//                            pointsDest = ArrayList<LatLng>()
//                            lineOptionsDest1 = PolylineOptions()
//                            lineOptionDesDark = PolylineOptions()
//                            lineOptionsDest1!!.geodesic(true)
//                            lineOptionDesDark!!.geodesic(true)
//                            for (i in step.indices) {
//                                val path: List<LatLng> = step[i].getListPoints()!!
//                                println(
//                                    "step =====> " + i + " and "
//                                            + path.size
//                                )
//                                pointsDest!!.addAll(path)
//                            }
//                            if (polyLineDest1 != null)
//                                polyLineDest1!!.remove()
//                            lineOptionsDest1!!.addAll(pointsDest!!)
//                            lineOptionsDest1!!.width(10f)
//                            lineOptionsDest1!!.startCap(SquareCap())
//                            lineOptionsDest1!!.endCap(SquareCap())
//                            lineOptionsDest1!!.jointType(JointType.ROUND)
//                            if (getNavigator().isAddedInAct()) {
//                                lineOptionsDest1!!.color(
//                                    ContextCompat.getColor(getNavigator().getCtx(), R.color.black)
//                                )
//                                lineOptionDesDark!!.color(
//                                    ContextCompat.getColor(
//                                        getNavigator().getCtx(),
//                                        R.color.clr_8992A3
//                                    )
//                                )
//                            }
//
//                            try {
//                                if (lineOptionsDest1 != null && googleMap != null) {
//                                    removeIfPolyAdded()
//                                    polyLineDest1 = googleMap?.addPolyline(lineOptionsDest1!!)
//                                    polyLineDestDark = googleMap?.addPolyline(lineOptionDesDark!!)
//
//                                    animatePolyLine()
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }
//
//                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
//                        getNavigator().showMessage(t.message!!)
//                    }
//                })
//            } else {
//                try {
//                    if (lineOptionsDest1 != null && googleMap != null) {
//                        removeIfPolyAdded()
//                        polyLineDest1 = googleMap?.addPolyline(lineOptionsDest1!!)
//                        polyLineDestDark = googleMap?.addPolyline(lineOptionsDest1!!)
//                        animatePolyLine()
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }

    fun removeIfPolyAdded() {
        if (polyLineDest1 != null)
            polyLineDest1!!.remove()
        if (polyLineDestDark != null)
            polyLineDestDark!!.remove()
    }


    /*
   The below  method 'animatePolyLine()' and 'polyLineAnimationListener' for animating the path in google map
    */
    fun animatePolyLine(latLng: MutableList<LatLng>) {
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
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                polyLineDest1?.let { dest1 ->
                    polyLineDestDark?.let { destDark ->
                        val blackLatLng: MutableList<LatLng> = dest1.points
                        val greyLatLng: MutableList<LatLng> = destDark.points
                        greyLatLng.clear()
                        greyLatLng.addAll(blackLatLng)
                        blackLatLng.clear()
                        polyLineDest1!!.points = blackLatLng
                        polyLineDestDark!!.points = greyLatLng
                        polyLineDest1!!.zIndex = 2f
                        animator.start()
                    }
                }
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        }


    override fun driverEnteredFence(key: String?, location: GeoLocation?, response: String?) {}

    override fun driverExitedFence(key: String?, response: String?) {}

    override fun driverMovesInFence(key: String?, location: GeoLocation?, response: String?) {}

    override fun driverWentOffline(key: String?) {}

    override fun driverDataUpdated(key: String?, latLng: LatLng, response: String?) {}

    var isWaitingTime = ObservableBoolean(true)

    override fun tripStatusReceived(response: String?) {
        printE(TripFragment.TAG, "Trip status : $response")
        try {
            tripObserverData = JSONObject(response!!)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        tripObserverData?.let {
            baseResponse = Gson().fromJson(it.toString(), BaseResponse::class.java)
        }
        baseResponse?.let { res ->


            /* setting waiting time or trip time block start */

            var time: String? = ""
            if (res.rentalTripTime != null) {
                time = res.rentalTripTime
                isWaitingTime.set(false)
            } else {
                time = res.waitingTime
                isWaitingTime.set(true)
            }
            var formattedTime = ""
            /* formating waiting time  */
            val formatedStringList = time?.let { convertISOTimeToDate(it) }
            if (formatedStringList != null) {
                if (formatedStringList.isNotEmpty() && formatedStringList.size == 3) {
                    try {
                        val sec = parseInt(formatedStringList[2])
                        val min = parseInt(formatedStringList[1])
                        val hour = parseInt(formatedStringList[0])
                        if (hour > 0) {
                            formattedTime = ("$hour:$min:$sec")
                            timeType.set(translationModel.txt_hour)
                        } else if (min > 0) {
                            formattedTime = ("$min:$sec")
                            timeType.set(translationModel.txt_min)
                        } else {
                            formattedTime = ("$sec")
                            timeType.set(translationModel.txt_sec)
                        }


                    } catch (e: NumberFormatException) {
                        formattedTime = if (res.rentalTripTime != null)
                            res.rentalTripTime + ""
                        else
                            res.waitingTime + ""

                    }

                } else {
                    formattedTime = if (res.rentalTripTime != null)
                        res.rentalTripTime + ""
                    else
                        res.waitingTime + ""
                }
            } else {
                formattedTime = if (res.rentalTripTime != null)
                    res.rentalTripTime + ""
                else
                    res.waitingTime + ""
            }

            if (res.rentalTripTime != null) {
                tripTime.set(formattedTime)
            } else {
                waitingTime.set(formattedTime)
            }

            /* setting waiting time or trip time block end */


            res.totalDistancee?.let { distance.set("$it KM") }
            res.lat?.let { newLat ->
                res.lng?.let { newLng ->
                    driverOldLatLng.set(driverCurrentLatLng.get())
                    driverCurrentLatLng.set(LatLng(newLat, newLng))
                    /*
                    check if path drawn
                    move driver
                    else
                    draw path and move driver
                     */
                    driverCurrentLatLng.get()?.let { latLng ->
                        res.bearing?.let {
                            updateDriverMarker(latLng, it)
                            if (!isDriverArrived.get()) {
                                val pickupLocation = Location("pickup")
                                pickupLocation.latitude = pickupLatLng.get()?.latitude ?: 0.0
                                pickupLocation.longitude = pickupLatLng.get()?.longitude ?: 0.0
                                val driverLocation = Location("driver")
                                driverLocation.latitude = latLng.latitude
                                driverLocation.longitude = latLng.longitude
                                val distance = driverLocation.distanceTo(pickupLocation)
                                val speed = 50
                                val time = distance / 1000 / speed
                                val sMinutes = String.format(Locale.ENGLISH, "%.0f", time * 60)
                                var iMinutes = sMinutes.toInt()
                                if (iMinutes == 0) iMinutes = 1
                                if (iMinutes >= 60) {
                                    val hours = iMinutes / 60
                                    val minutes = iMinutes % 60
                                    if (hours == 1) {
                                        showetatxt.set("$hours hr $minutes mins")
                                    } else {
                                        showetatxt.set("$hours hrs $minutes mins")
                                    }
                                } else {
                                    showetatxt.set("" + (iMinutes + 2) + "mins")
                                }
                            } else {
                                showEta.set(false)
                            }

                        }
                    }
                }
            }
        }
    }

    fun initializeFireBaseObserver() {
        firebaseHelper = FirebaseHelper(session, this, false)
        requestId?.let { firebaseHelper.addTripObserverFor(it) }
    }

    private fun drawMultiplePaths() {
        val polyString = requestData?.polyString
        val polyList: MutableList<LatLng> =
            Utilz.decodeOverviewPolyLinePoints(polyString!!) as MutableList<LatLng>
        val stopLatLng = LatLng(
            requestData?.stops?.latitude?.toDouble() ?: 0.0,
            requestData?.stops?.longitude?.toDouble() ?: 0.0
        )
        val mutiDropMarker: Marker? = null
        if (isStopAdded.get()) {
            if (mutiDropMarker == null) {
                pickUpMarker = googleMap?.addMarker(
                    MarkerOptions().position(stopLatLng).title("Stop Point")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_pin))
                )
            }
        }
        lineOptionsDest1 = PolylineOptions()
        lineOptionDesDark = PolylineOptions()
        if (polyLineDest1 != null) polyLineDest1!!.remove()
        lineOptionsDest1!!.addAll(polyList)
        lineOptionsDest1!!.width(10f)
        lineOptionsDest1!!.startCap(SquareCap())
        lineOptionsDest1!!.endCap(SquareCap())
        lineOptionsDest1!!.jointType(JointType.ROUND)
        // if (getNavigator().isAddedInAct()) {
        lineOptionsDest1!!.color(
            ContextCompat.getColor(getNavigator().getCtx(), R.color.colorPrimary)
        )
        lineOptionDesDark!!.color(
            ContextCompat.getColor(
                getNavigator().getCtx(),
                R.color.colorPrimary
            )
        )

        try {
            if (lineOptionsDest1 != null) {
                polyLineDest1 = googleMap!!.addPolyline(lineOptionsDest1!!)
                polyLineDestDark = googleMap!!.addPolyline(lineOptionDesDark!!)
                buildBound(pickupLatLng.get(), dropLatLng.get())
//                animatePolyLine(polyList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun bitMapFromVector(vectorResID: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(getNavigator().getCtx(), vectorResID)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    /*
    The below method will create a new driver's car marker if no marker present. else if already present
    it move marker.
    The below method called from tripStatusReceived().
     */
    private fun updateDriverMarker(latLng: LatLng, bearing: Float) {
        if (driverCarMarker == null) {
            driverCarMarker = googleMap?.addMarker(
                MarkerOptions().position(latLng).title("Driver Point")
                    .icon(bitMapFromVector(R.drawable.car_icon_us))
            )

        } else {
            driverCarMarker?.rotation = bearing
            val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(16f)
                .build()
            googleMap?.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                3000,
                null
            );
            val latLngInterpolator: LatLngInterpolator = LatLngInterpolator.Linear()
            MarkerAnimation.animateMarker(driverCarMarker, latLng, latLngInterpolator)
        }
    }

    fun setPickupDropMarkers() {
        val mutiDropMarker: Marker? = null
        val stopLatLng = LatLng(
            requestData?.stops?.latitude?.toDouble() ?: 0.0,
            requestData?.stops?.longitude?.toDouble() ?: 0.0
        )
        pickupLatLng.get()?.let {

            val bitMapFactory =
                Utilz.getBitmapDescriptor(getNavigator().getCtx(), R.drawable.road_marker)
            pickUpMarker = googleMap?.addMarker(
                MarkerOptions().position(LatLng(it.latitude, it.longitude)).title("Pickup Point")
                    .icon(bitMapFactory)
            )
        }

        dropLatLng.get()?.let {
            val bitMapFactory =
                Utilz.getBitmapDescriptor(getNavigator().getCtx(), R.drawable.red_marker)
            dropMarker = googleMap?.addMarker(
                MarkerOptions().position(LatLng(it.latitude, it.longitude)).title("Drop Point")
                    .icon(bitMapFactory)
            )

        }
        if (isStopAdded.get()) {
            if (mutiDropMarker == null) {
                pickUpMarker = googleMap?.addMarker(
                    MarkerOptions().position(stopLatLng).title("Stop Point")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_pin))
                )
            }
        }
    }


    fun getUserLocationToCancel() {
        if (ActivityCompat.checkSelfPermission(
                getNavigator().getCtx(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getNavigator().getCtx(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            isLoading.value = false
            return
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (!latLngTaken) {
                    if (location != null) {
                        latLngTaken = true
                        userLat = "${location.latitude}"
                        userLng = "${location.longitude}"
                        getAddressFromLatLng(location.latitude, location.longitude, 0)
                    } else {
                        latLngTaken = true
                        userLat = session.getString(SessionMaintainence.CURRENT_LATITUDE)!!
                        userLng = session.getString(SessionMaintainence.CURRENT_LONGITUDE)!!
                        getAddressFromLatLng(userLat.toDouble(), userLng.toDouble(), 0)
                    }
                }

            }
        }
    }

//    private fun getImageFromS3(profilePic: String?) {
//        val viewModelJob = Job()
//        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
//
//        uiScope.launch {
//            withContext(Dispatchers.IO) {
//                val cal = Calendar.getInstance()
//                cal.time = Date()
//                cal.add(Calendar.HOUR, +1)
//                val oneHourLater: Date = cal.time
//                val s3: AmazonS3 = AmazonS3Client(
//                    BasicAWSCredentials(
//                        session.getString(SessionMaintainence.AWS_ACCESS_KEY_ID),
//                        session.getString(SessionMaintainence.AWS_SECRET_ACCESS_KEY)
//                    )
//                )
//                s3.setRegion(Region.getRegion(session.getString(SessionMaintainence.AWS_DEFAULT_REGION)))
//                val url: URL = s3.generatePresignedUrl(
//                    session.getString(SessionMaintainence.AWS_BUCKET),
//                    profilePic,
//                    oneHourLater
//                )
//                withContext(Dispatchers.Main) {
//                    driverImageURL.set(url.toString())
//                }
//            }
//        }
//
//    }


    /*
     mode 0-> called for user address
     mode 1-> called for driver address
     */
    private fun getAddressFromLatLng(lat: Double, lng: Double, mode: Int) {

        mapsHelper.GetAddressFromLatLng(
            "$lat,$lng",
            false, session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY)
        )!!.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    if (response.body() != null && response.body()!!
                            .getAsJsonArray("results") != null
                    ) {
                        val status = response.body()!!["status"].asString
                        if (status == "OK") {

                            if (mode == 0) {
                                userAddress = response.body()!!
                                    .getAsJsonArray("results")[0].asJsonObject["formatted_address"].asString
                                getAddressFromLatLng(
                                    if (driverCurrentLatLng.get() != null)
                                        driverCurrentLatLng.get()!!.latitude else userLat.toDouble(),
                                    if (driverCurrentLatLng.get() != null) driverCurrentLatLng.get()!!.longitude else userLng.toDouble(),
                                    1
                                )
                            } else {
                                driverAddress = response.body()!!
                                    .getAsJsonArray("results")[0].asJsonObject["formatted_address"].asString
                                cancelRequest()
                            }

                        } else if (status == "OVER_QUERY_LIMIT") {
                            try {
                                val geocoder = Geocoder(getNavigator().getCtx())
                                var mAddress = ""
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
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
                                        if (mode == 0) {
                                            userAddress = mAddress
                                            getAddressFromLatLng(
                                                driverCurrentLatLng.get()!!.latitude,
                                                driverCurrentLatLng.get()!!.longitude,
                                                1
                                            )
                                        } else {
                                            driverAddress = mAddress
                                            cancelRequest()
                                        }
                                    }
                                } else {
                                    val addresses = geocoder.getFromLocation(lat, lng, 1)
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

                                    if (mode == 0) {
                                        userAddress = mAddress
                                        getAddressFromLatLng(
                                            driverCurrentLatLng.get()!!.latitude,
                                            driverCurrentLatLng.get()!!.longitude,
                                            1
                                        )
                                    } else {
                                        driverAddress = mAddress
                                        cancelRequest()
                                    }
                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    isLoading.value = false
                    getNavigator().showCustomDialog("Unable to get location for cancel")
                    Log.d(TripFragment.TAG, "GetAddressFromLatlng$response")
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                isLoading.value = false
                getNavigator().showCustomDialog("Unable to get location for cancel")
                Log.d(
                    TripFragment.TAG, "GetAddressFromLatlng$t"
                )
            }
        })
    }

    fun sosclick() {
        getNavigator().openSos()
    }

    var shareTripLink = ""
    fun onShareTripClick() {
//        try {
//            val shareIntent = Intent(Intent.ACTION_SEND)
//            shareIntent.type = "text/plain"
//            shareIntent.putExtra(
//                Intent.EXTRA_SUBJECT,
//                getNavigator().getAct().getString(R.string.app_name)
//            )
//            shareIntent.putExtra(Intent.EXTRA_TEXT, shareTripLink)
//            getNavigator().getAct()
//                .startActivity(Intent.createChooser(shareIntent, "choose one"))
//        } catch (e: java.lang.Exception) {
//            //e.toString();
//        }

        ShareCompat.IntentBuilder
            .from(getNavigator().getAct())
            .setType("text/plain")
            .setChooserTitle("Share URL")
            .setText(URL(shareTripLink).toString())
            .startChooser()
    }


    var realPath: String? = null
    var realFile: File? = null
    fun onCaptureImageResult(data: Intent) {
        if (data != null) {
            if (data.hasExtra("data")) {
                realPath = getNavigator().getCtx().let {
                    Utilz.getImageUri(
                        it, data.extras?.get(
                            "data"
                        ) as Bitmap
                    )
                }
                realFile = File(realPath)
                uploadImage()
            }
        }

    }

}

@BindingAdapter("imageUrl")
fun setImage(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url)
        .apply(RequestOptions.fitCenterTransform().error(R.drawable.ic_car))
        .into(imageView)
}

@BindingAdapter("driverImageUrl")
fun setDriverImage(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url)
        .apply(
            RequestOptions().error(R.drawable.profile_place_holder)
                .placeholder(R.drawable.profile_place_holder)
        )
        .into(imageView)
}

fun convertISOTimeToDate(isoTime: String): List<String>? {
    try {
        val data = isoTime.split(":")
        return data
    } catch (e: ArrayIndexOutOfBoundsException) {
        return null
    }


}