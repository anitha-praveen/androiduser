package com.cloneUser.client.drawer.rental.rentalInvoice

import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.android.gms.maps.model.LatLng
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RentalnvoiceVm @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
): BaseVM<BaseResponse, RentalInvoiceNavigator>(session, mConnect)  {

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
    var customCaptainShown = ObservableBoolean(false)
    var isAddnlChargeAvailable = ObservableBoolean(false)
    var isWalletTrip = ObservableBoolean(false)
    var requestData: RequestData.Data? = null
    var currency = ObservableField("")
    var mode = -1
    val isZone = ObservableBoolean(false)
    val vehicalNum = ObservableField("")
    val vehicalType = ObservableField("")

    var distanceDesText = ObservableField("")
    var showDistanceDesc = ObservableBoolean(false)
    var packageSelected = ObservableField("")
    /*
    mode 0 -> Invoice called from trip
         1 -> Invoice called from History
     */
    var showDispute = ObservableBoolean(false)
    var pickupLatLng = ObservableField<LatLng>()
    var dropLatLng = ObservableField<LatLng>()
    var isDriverInfoVisible = ObservableBoolean(false)
    var isBillAvailbale = ObservableBoolean(false)
    var showTimeCost = ObservableBoolean(false)
    var showWaitingCost = ObservableBoolean(false)
    var showHillstationFee = ObservableBoolean(false)
    var showBasePrice = ObservableBoolean(false)
    var startKM = ObservableField("")
    var endKM = ObservableField("")
    val showStops = ObservableBoolean(false)
    val showPromo = ObservableBoolean(false)
    val stopAddress = ObservableField("")
    val buttonText = ObservableField("")

    override fun onSuccessfulApi(response: BaseResponse?) {

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {

    }

    //set up data
    fun setupInitialData() {
        if (mode == 1)
            buttonText.set(translationModel.txt_Gotit)
        else
            buttonText.set(translationModel.txt_confirm)
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


            request.startKM?.let { "${startKM.set(it)}" }
            request.endKM?.let { "${endKM.set(it)}" }

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
            request.totalDistance?.let { distance.set("${Utilz.removeZero(it)} " + (request.unit ?: "")) }

            request.driver?.let { driver ->
                isDriverInfoVisible.set(true)
                 driver.profilePic?.let { driverProfilePic.set(it) }
//                getImageFromS3(driver.profilePic)
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
                    packageSelected.set("(${data.packageHours} ${translationModel.txt_hrs} ${Utilz.removeZero("${data.packageKm}")} ${translationModel.txt_km})")
                    data.requestedCurrencySymbol?.let { currency.set(it) }
                    data.totalAmount?.let {
                        total.set("${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                        total_trip_cost.set(" ${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                    }
                    data.basePrice?.let {
                        if(it > 0){
                            baseprice.set("${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                            showBasePrice.set(true)
                        }
                    }
                    data.distancePrice?.let {
                        distanceCost.set(
                            "${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}"
                        )
                    }
                    data.timePrice?.let {
                        if(it > 0)
                            showTimeCost.set(true)
                        else
                            showTimeCost.set(false)
                        timeCost.set("${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                    }
                    data.waitingCharge?.let {
                        waitingPrice.set(
                            "${currency.get()} ${
                               Utilz.removeZero("${round(
                                   it,
                                   2
                               )}") 
                            }"
                        )
                    }
                    /* refferalBonus not set and hidden in ui  */
                    data.promoDiscount?.let {
                        if (it > 0)
                            showPromo.set(true)
                        promoBonus.set("-${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                    }
                    data.cancellationFee?.let {
                        cancellation_fees.set(
                            "${currency.get()} ${
                                Utilz.removeZero("${round(
                                    it,
                                    2
                                )}")
                            }"
                        )
                    }
                    if(data.pendingKm == null || data.pendingKm == "0" || data.pendingKm == ""){
                        showDistanceDesc.set(false)
                    }else{
                        showDistanceDesc.set(true)
                        distanceDesText.set("(${Utilz.removeZero("${data.pendingKm}")} ${translationModel.txt_km} x ${data.requestedCurrencySymbol} ${Utilz.removeZero("${data.pricePerDistance}")})")
                    }
                }

                if (bill.data?.hillStationPrice == null || bill.data.hillStationPrice <= 0) {
                    showHillstationFee.set(false)
                } else {
                    hillStationFee.set(
                        "${Utilz.removeZero("${ round(
                            bill.data.hillStationPrice,
                            2
                        )}")
                        } ${currency.get()}"
                    )
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



    //ui clicks
    fun onConfirm(view:View) {
        getNavigator().chooseDirection()
    }

    fun onClickClose(view: View){
        getNavigator().chooseDirection()
    }
}