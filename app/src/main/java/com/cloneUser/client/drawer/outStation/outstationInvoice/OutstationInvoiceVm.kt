package com.cloneUser.client.drawer.outStation.outstationInvoice

import android.graphics.Region
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import com.google.android.gms.maps.model.LatLng

import kotlinx.coroutines.*
import retrofit2.Call
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class OutstationInvoiceVm @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
) :
    BaseVM<BaseResponse, OutstationInvoiceNavigator>(session, mConnect) {

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
    var total = ObservableField("")
    var distance = ObservableField("")
    var baseprice = ObservableField("")
    var distanceCost = ObservableField("")
    var timeCost = ObservableField("")
    var refferalBonus = ObservableField("")
    var promoBonus = ObservableField("")
    var serviceTax = ObservableField("")
    var total_trip_cost = ObservableField("")
    var cancellation_fees = ObservableField("")
    var custom_captain_fee = ObservableField("")
    var hillStationFee = ObservableField("")
    var zone_fees = ObservableField("")
    var isShare = ObservableBoolean(false)
    var isAddnlChargeAvailable = ObservableBoolean(false)
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
    var startKM = ObservableField("")
    var endKM = ObservableField("")
    val showStops = ObservableBoolean(false)
    val stopAddress = ObservableField("")
    val distanceHint = ObservableField("")
    val showPromo = ObservableBoolean(false)
    val isTwoWay = ObservableBoolean(false)
    override fun onSuccessfulApi(response: BaseResponse?) {

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {

    }

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
            if (request.stops?.address != null) {
                showStops.set(true)
                request.stops.address.let { stopAddress.set(it) }
            } else
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
                        tripEndDate.set(
                            SimpleDateFormat(
                                "dd-MM-yy",
                                Locale.ENGLISH
                            ).format(mDate)
                        )
                        tripEndTime.set(SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(mDate))
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            request.oustationTripType?.let {
                if (it.equals("TWO", true))
                    isTwoWay.set(true)
                else
                    isTwoWay.set(false)
            }

            request.startKM?.let { startKM.set(it) }
            request.endKM?.let { endKM.set(it) }
            request.totalDistance?.let {
                distanceDesText.set("${translationModel.text_distance_cost}")
                distance.set("${Utilz.removeZero(it)} " + (request.unit ?: ""))
            }

            request.driver?.let { driver ->
                isDriverInfoVisible.set(true)
                 driver.profilePic?.let { driverProfilePic.set(it) }
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
                    distanceHint.set(
                        "(${Utilz.removeZero("${data.totalDistance}")} ${request.unit?.lowercase() ?: ""} x ${currency.get()} ${
                            request.requestBill.data?.pricePerDistance?.let {
                                Utilz.removeZero("${round(it, 2)}")
                            }
                        })"
                    )
                    data.totalAmount?.let {
                        total.set("${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}")
                        total_trip_cost.set(
                            " ${currency.get()} ${
                                Utilz.removeZero(
                                    "${
                                        round(
                                            it,
                                            2
                                        )
                                    }"
                                )
                            }"
                        )
                    }
                    data.bookingFees.let { bookIngFees.set("${currency.get()} ${Utilz.removeZero("$it")}") }
                    data.basePrice?.let {
                        if (it > 0) {
                            baseprice.set(
                                "${currency.get()} ${
                                    Utilz.removeZero(
                                        "${
                                            round(
                                                it,
                                                2
                                            )
                                        }"
                                    )
                                }"
                            )
                            showBasePrice.set(true)
                        }
                    }
                    data.distancePrice?.let {
                        distanceCost.set(
                            "${currency.get()} ${Utilz.removeZero("${round(it, 2)}")}"
                        )
                    }
                    data.timePrice?.let {
                        timeCost.set(
                            "${currency.get()} ${
                                Utilz.removeZero(
                                    "${
                                        round(
                                            it,
                                            2
                                        )
                                    }"
                                )
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
                                Utilz.removeZero(
                                    "${
                                        round(
                                            it,
                                            2
                                        )
                                    }"
                                )
                            }"
                        )
                    }
                }

                if (bill.data?.hillStationPrice == null || bill.data.hillStationPrice <= 0) {
                    showHillstationFee.set(false)
                } else {
                    hillStationFee.set(
                        "${
                            Utilz.removeZero(
                                "${
                                    round(
                                        bill.data.hillStationPrice,
                                        2
                                    )
                                }"
                            )
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




    //ui click

    fun close(view: View) {
        getNavigator().chooseDirection()
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
