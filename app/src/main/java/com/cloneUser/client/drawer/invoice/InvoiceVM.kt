package com.cloneUser.client.drawer.invoice

import android.widget.ImageView
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
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.connection.responseModels.Route
import com.cloneUser.client.connection.responseModels.Step
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class InvoiceVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, InvoiceNavigator>(session, mConnect) {


    var pickup = ObservableField("")
    var drop = ObservableField("")
    var driverProfilePic = ObservableField("")
    var rating = ObservableField("")
    var driverName = ObservableField("")
    var vehType = ObservableField("")
    var reqId = ObservableField("")

    var tripDate = ObservableField("")
    var tripStartTime = ObservableField("")
    var tripEndTime = ObservableField("")
    var tripEndDate = ObservableField("")

    var duration = ObservableField("")
    var total = ObservableField("")
    var distance = ObservableField("")
    var baseprice = ObservableField("")
    var distanceCost = ObservableField("")
    var timeCost = ObservableField("")
    var waitingPrice = ObservableField("")
    var refferalBonus = ObservableField("")
    var promoBonus = ObservableField("")
    var serviceTax = ObservableField("")
    var total_trip_cost = ObservableField("")
    var cancellation_fees = ObservableField("")
    var custom_captain_fee = ObservableField("")
    var hillStationFee = ObservableField("")
    var zone_fees = ObservableField("")
    var walletAmount = ObservableField("")
    var showDistanceCost = ObservableBoolean(false)
    var customCaptainShown = ObservableBoolean(false)
    var isAddnlChargeAvailable = ObservableBoolean(false)
    var isWalletTrip = ObservableBoolean(false)
    var requestData: RequestData.Data? = null
    var currency = ObservableField("")
    var mode = -1
    val bookIngFees = ObservableField("")
    val isZone = ObservableBoolean(false)
    val vehicalNum = ObservableField("")
    val vehicalType = ObservableField("")

    val outZonetotal = ObservableField("")
    var distanceDesText = ObservableField("")
    /*
    mode 0 -> Invoice called from trip
         1 -> Invoice called from History
     */
    var showDispute = ObservableBoolean(false)
    var pickupLatLng = ObservableField<LatLng>()
    var dropLatLng = ObservableField<LatLng>()
    var isDriverInfoVisible = ObservableBoolean(false)
    var isBillAvailbale = ObservableBoolean(false)
    var buttonText = ObservableField("")
    var showHillstationFee = ObservableBoolean(false)
    var showBasePrice = ObservableBoolean(false)
    val showStops = ObservableBoolean(false)
    val stopAddress = ObservableField("")
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (response?.data != null) {
            val res = Gson().fromJson(Gson().toJson(response.data), RequestData::class.java)
            requestData = res.data
            if (requestData?.isLater == 1 && requestData?.isCancelled != 1) {
                buttonText.set(translationModel.txt_cancel_ride)
            } else {
                buttonText.set(translationModel.txt_confirm)
                getNavigator().showMessage("Trip cancelled")
            }

        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun setupInitialData() {
        requestData?.let { request ->
            request.pickAddress?.let { pickup.set(it) }
            request.dropAddress?.let { drop.set(it) }
            request.pickLat?.let { lat ->
                request.pickLng?.let { lng ->
                    pickupLatLng.set(LatLng(lat, lng))
                }
            }
            request.dropLat?.let { lat ->
                request.dropLng?.let { lng ->
                    dropLatLng.set(LatLng(lat, lng))
                }
            }
            if (request.stops?.address != null){
                showStops.set(true)
                request.stops.address.let { stopAddress.set(it) }
            }else
                showStops.set(false)
            request.requestNumber?.let { reqId.set(it) }
            request.carDetails?.carNumber?.let { vehicalNum.set(it) }
            request.vehicleHighlighted?.let { vehicalType.set(it) }
            request.disputeStatus?.let { showDispute.set(it == 1 && mode != 0) }
            request.tripStartTime?.let {
                try {
                    val mDate =
                        SimpleDateFormat("dd-MM-yyy HH:mm:ss", Locale.ENGLISH).parse(it)
                    if (mDate != null) {
                        tripDate.set(SimpleDateFormat("dd-MM-yy", Locale.ENGLISH).format(mDate))
                        tripStartTime.set(SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(mDate))
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                    tripDate.set(it)
                }
            }
            request.completedAt?.let {
                try {
                    val mDate =
                        SimpleDateFormat("dd-MM-yyy HH:mm:ss", Locale.ENGLISH).parse(it)
                    if (mDate != null) {
                        tripEndDate.set(SimpleDateFormat("dd-MM-yy", Locale.ENGLISH).format(mDate))
                        tripEndTime.set(SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(mDate))
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            distanceDesText.set("${translationModel.text_distance_cost}")
            request.totalTime?.let {
                try{
                    val hours: Int = it.toDouble().toInt() / 60 //since both are ints, you get an int
                    val minutes: Int = it.toDouble().toInt() % 60
                    if(hours >= 1 && minutes >= 1){
                        duration.set("$hours ${translationModel.txt_hrs} $minutes ${translationModel.txt_min}")
                    }else if(hours >= 1 && minutes < 1){
                        duration.set("$hours ${translationModel.txt_hrs}")
                    }else if(hours < 1 && minutes >= 1){
                        duration.set("$minutes ${translationModel.txt_min}")
                    }else if(hours < 1 && minutes < 1){
                        duration.set(it + " ${translationModel.txt_min}")
                    }
                }catch (e : NumberFormatException){
                    duration.set(it + " ${translationModel.txt_min}")
                }
            }
            request.totalDistance?.let { distance.set(Utilz.removeZero(it) + (request.unit ?: "")) }

            request.driver?.let { driver ->
                isDriverInfoVisible.set(true)
                // driver.profilePic?.let { driverProfilePic.set(it) }
                driverProfilePic.set(driver.profilePic)
                driver.firstname?.let { first ->
                    driver.lastname?.let {
                        driverName.set("$first $it")
                    }
                }
            }
            request.carDetails?.let { details ->
                details.carModel?.let { vehType.set(it) }
            }
            request.driverOverallRating?.let { rating.set("" + round(it, 1)) }
            request.requestBill?.let { bill ->
                bill.data?.let { data ->
                    isBillAvailbale.set(true)
                    data.requestedCurrencySymbol?.let { currency.set(it) }
                    data.totalAmount?.let {
                        total.set("${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                        total_trip_cost.set(" ${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                    }
                    data.bookingFees.let { bookIngFees.set("${currency.get()} ${Utilz.removeZero("$it")}")}
                    data.basePrice?.let {
                        if(it > 0){
                            baseprice.set("${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                            showBasePrice.set(true)
                        }
                    }
                    data.distancePrice?.let {
                        if(it > 0)
                            showDistanceCost.set(true)
                        distanceCost.set(
                            "${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}"
                        )
                    }
                    data.timePrice?.let { timeCost.set("${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}") }
                    data.waitingCharge?.let {
                        waitingPrice.set("${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                    }
                    /* refferalBonus not set and hidden in ui  */
                    data.promoDiscount?.let { promoBonus.set("-${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}") }
                    data.cancellationFee?.let {
                        cancellation_fees.set("${currency.get()} ${Utilz.removeZero("${ round(it, 2)}")}")
                    }
                }

                if (bill.data?.hillStationPrice == null || bill.data.hillStationPrice <= 0) {
                    showHillstationFee.set(false)
                } else {
                    hillStationFee.set("${currency.get()} ${Utilz.removeZero("${ round(bill.data.hillStationPrice, 2)}")}")
                    showHillstationFee.set(true)
                }

                if (bill.data?.outZonePrice != null && bill.data.outZonePrice > 0) {
                    isZone.set(true)
                    zone_fees.set("${currency.get()} ${Utilz.removeZero("${bill.data.outZonePrice}")}")
                } else
                    isZone.set(false)
                bill.data?.serviceTax?.let { serviceTax.set("${currency.get()} ${Utilz.removeZero("$it")}") }
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
//                    driverProfilePic.set(url.toString())
//                }
//            }
//        }
//
//    }

    fun buildBound(googleMap: GoogleMap) {
        val width: Int = getNavigator().getCtx().resources.displayMetrics.widthPixels
        val padding = (width * 0.25).toInt()
        if (pickupLatLng.get() != null && dropLatLng.get() != null) {
            val boundsBuilder = LatLngBounds.builder()
                .include(pickupLatLng.get()!!)
                .include(dropLatLng.get()!!)
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(), padding
                )
            )
        } else {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pickupLatLng.get()!!, 30f
                )
            )
        }
    }


    fun setMarkers(googleMap: GoogleMap?) {

        pickupLatLng.get()?.let {
            googleMap!!.addMarker(
                MarkerOptions()
                    .position(it)
                    .title("Pickup Location")
                    .anchor(0.5f, 0.5f)
                    .icon(getNavigator().getMarkerIcon(R.drawable.ic_pick_pin))
            )

        }
        dropLatLng.get()?.let {
            googleMap!!.addMarker(
                MarkerOptions()
                    .position(it)
                    .title("Drop Location")
                    .anchor(0.5f, 0.5f)
                    .icon(getNavigator().getMarkerIcon(R.drawable.ic_drop_pin))
            )
        }
    }


    var routeDest: Route? = null
    var pointsDest: MutableList<LatLng>? = null
    var lineOptionsDest1: PolylineOptions? = null
    var lineOptionDesDark: PolylineOptions? = null
    var polyLineDest1: Polyline? = null
    var polyLineDestDark: Polyline? = null
    var predictedDistance = ObservableField("")
    var predictedTripTime = ObservableField("")

    fun drawPathPickToDrop(googleMap: GoogleMap?) {
        pickupLatLng.get()?.let { p ->
            dropLatLng.get()?.let { d ->

                if (polyLineDest1 == null && polyLineDestDark == null) {
                    mapsHelper.getDrawPath(
                        p.latitude.toString() + "," + p.longitude,
                        d.latitude.toString() + "," + d.longitude, false,
                        session.getString(SessionMaintainence.DIRECTION_DYNAMIC_KEY)
                    )!!.enqueue(object : Callback<JsonObject?> {
                        override fun onResponse(
                            call: Call<JsonObject?>,
                            response: Response<JsonObject?>
                        ) {
                            if (response.body() != null) {
                                routeDest = Route()
                                Utilz.parseRoute(response.body()!!, routeDest!!)
                                routeDest?.let {
                                    predictedDistance.set(it.getDistanceText())
                                    predictedTripTime.set(it.getDurationText())
                                }
                                val step: ArrayList<Step> = routeDest!!.getListStep()!!
                                println("step size=====> " + step.size)
                                pointsDest = ArrayList<LatLng>()
                                lineOptionsDest1 = PolylineOptions()
                                lineOptionDesDark = PolylineOptions()
                                lineOptionsDest1!!.geodesic(true)
                                lineOptionDesDark!!.geodesic(true)
                                for (i in step.indices) {
                                    val path: List<LatLng> = step[i].getListPoints()!!
                                    println(
                                        "step =====> " + i + " and "
                                                + path.size
                                    )
                                    pointsDest!!.addAll(path)
                                }
                                if (polyLineDest1 != null) polyLineDest1!!.remove()
                                lineOptionsDest1!!.addAll(pointsDest!!)
                                lineOptionsDest1!!.width(10f)
                                lineOptionsDest1!!.startCap(SquareCap())
                                lineOptionsDest1!!.endCap(SquareCap())
                                lineOptionsDest1!!.jointType(JointType.ROUND)
                                // if (getNavigator().isAddedInAct()) {
                                lineOptionsDest1!!.color(
                                    ContextCompat.getColor(
                                        getNavigator().getCtx(),
                                        R.color.clr_FB4A46
                                    )
                                )
                                lineOptionDesDark!!.color(
                                    ContextCompat.getColor(
                                        getNavigator().getCtx(),
                                        R.color.clr_FF9391
                                    )
                                )
                                // }

                                try {
                                    if (lineOptionsDest1 != null) {
                                        polyLineDest1 = googleMap!!.addPolyline(lineOptionsDest1!!)
                                        polyLineDestDark =
                                            googleMap.addPolyline(lineOptionDesDark!!)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                            getNavigator().showMessage(t.message!!)
                        }
                    })
                } else {
                    try {
                        if (lineOptionsDest1 != null) {
                            polyLineDest1 = googleMap!!.addPolyline(lineOptionsDest1!!)
                            polyLineDestDark = googleMap.addPolyline(lineOptionsDest1!!)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    var hashMap = HashMap<String, String>()
    fun cancelRideLaterTrip() {
        if (getNavigator().isNetworkConnected()) {
            hashMap.clear()
            hashMap[Config.request_id] = requestData?.id!!
            cancelRequestBase(hashMap)
        } else
            getNavigator().showNetworkUnAvailable()
    }

    /*
    This is close the invoice . It does not decide whether to cancel.
    It will close the invoice and navigate to history list or rating fragment based on the situation
     */
    fun onClickClose() {
        getNavigator().close()
    }

    /*
    This will decide whether to close and navigate or call cancel api
     */
    fun onConfirm() {
        getNavigator().chooseDirection()
    }

    fun onClickDispute() {
        getNavigator().gotoComplaints()
    }

}

@BindingAdapter("invoiceUserImg")
fun setImage(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url)
        .apply(
            RequestOptions().error(R.drawable.profile_place_holder)
                .placeholder(R.drawable.ic_rectange_imv)
        )
        .into(imageView)
}

@BindingAdapter("vehicaltype")
fun setType(imageView: ImageView, type: String) {
    Glide.with(imageView.context).load(type)
        .apply(
            RequestOptions().error(R.drawable.ic_selected_rental_car)
                .placeholder(R.drawable.ic_rectange_imv)
        )
        .into(imageView)
}
