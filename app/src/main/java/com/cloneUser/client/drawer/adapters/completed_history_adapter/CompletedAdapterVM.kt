package com.cloneUser.client.drawer.adapters.completed_history_adapter

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.RequestData
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

class CompletedAdapterVM (
    private val history: RequestData.Data,
    private val adapterLister: CompletedItemListener,
    translationModel: TranslationModel
) {
    val translation = translationModel
    var driverProfile = ObservableField(history.driver?.profilePic ?: "")
    var vehicleImage = ObservableField(history.vehicleHighlighted ?: "")
    var driverName = ObservableField(history.driver?.firstname ?: "Not Assigned")

    fun round(value: Double, precision: Int): Double {
        val scale = 10.0.pow(precision.toDouble()).toInt()
        return (value * scale).roundToInt().toDouble() / scale
    }


    var driverRating = ObservableField(
        if (history.driverOverallRating != null)
            "${round(history.driverOverallRating, 2)}" else "N/A"
    )

    var date = ObservableField(
        if (history.tripStartTime != null) {
            try {
                @SuppressLint("SimpleDateFormat") val mDate =
                    SimpleDateFormat("dd-MM-yyy hh:mm:ss").parse(history.tripStartTime)
                if (mDate != null)
                    (SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(mDate))
                else
                    ""
            } catch (e: Exception) {
                e.printStackTrace()
                history.tripStartTime
            }
        } else ""
    )
    var time = ObservableField(
        if (history.tripStartTime != null) {
            try {
                @SuppressLint("SimpleDateFormat") val mDate =
                    SimpleDateFormat("dd-MM-yyy hh:mm:ss").parse(history.tripStartTime)
                if (mDate != null)
                    (SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(mDate))
                else
                    ""
            } catch (e: Exception) {
                e.printStackTrace()
                history.tripStartTime
            }
        } else ""
    )


    var typeName =
        ObservableField("${history.vehical_type ?: ""} ${if (history.vehicleModel != null) " | ${history.vehicleModel}" else ""}")
    var vehicleNumber = ObservableField(history.vehicleNumber ?: "")
    var requestId = ObservableField(history.requestNumber ?: "")
    var isCompleted = ObservableBoolean(history.isCompleted == 1)
    var isLater = ObservableBoolean(history.isLater == 1)
    var isCancelled = ObservableBoolean(history.isCancelled == 1)
    var pickup = ObservableField(history.pickAddress ?: "")
    val serviceType = if(history.serviceCategory.equals("LOCAL",true)) "" else history.serviceCategory
    var drop = ObservableField(if(history.serviceCategory.equals("RENTAL",true)) "${history.packageHour}hr and ${history.packageKm}km" else "${history.dropAddress}")
    var haveDispute = ObservableBoolean(history.disputeStatus == 1)

    fun onItemSelected() {
        adapterLister.itemSelected(history)
    }

    fun dispute() {
        adapterLister.onDisputeClicked(history)
    }


    interface CompletedItemListener : BaseViewOperator {
        fun itemSelected(history: RequestData.Data)
        fun onDisputeClicked(history: RequestData.Data)
    }

}

//@BindingAdapter("imageUrlDriverIconCompleted")
//fun setDriverImageCompleted(imageView: ImageView, url: String?) {
//    Glide.with(imageView.context).load(url)
//        .apply(
//            RequestOptions().error(R.drawable.simple_profile_bg)
//                .placeholder(R.drawable.ic_history_profile_dummy)
//        ).into(imageView)
//}

@BindingAdapter("vehicleImageCompleted")
fun setVehicleImageCompleted(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url).into(imageView)
}