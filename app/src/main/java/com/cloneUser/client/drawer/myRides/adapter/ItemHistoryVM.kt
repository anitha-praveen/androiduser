package com.cloneUser.client.drawer.myRides.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.RequestData
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

class ItemHistoryVM(
    private val history: RequestData.Data,
    private val adapterLister: HistoryItemListener,
    translationModel: TranslationModel,
    private val context:Context
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
                    SimpleDateFormat("dd-MM-yyy HH:mm:ss").parse(history.tripStartTime)
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
                    SimpleDateFormat("dd-MM-yyy HH:mm:ss").parse(history.tripStartTime)
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

    fun onClickCancel(){
        showDialog()
    }

    fun showDialog(){
        val logDialog = context.let { BottomSheetDialog(it, R.style.AppBottomSheetDialogTheme) }
        logDialog.setContentView(R.layout.logout_dialog)
        logDialog.show()


        val title = logDialog.findViewById<TextView>(R.id.log_title)
        val desc = logDialog.findViewById<TextView>(R.id.desc_log)
        val cancelButton = logDialog.findViewById<MaterialButton>(R.id.cancel_log)
        val logOUtButton = logDialog.findViewById<MaterialButton>(R.id.logOut_button)
        if (title != null) {
            title.text = translation.txt_cancel_ride
        }
        if (desc != null) {
            desc.text =translation.txt_cancel_desc
        }
        if (cancelButton != null) {
            cancelButton.text = translation.close
        }
        if (logOUtButton != null) {
            logOUtButton.text = translation.txt_cancel_ride
        }
        logOUtButton?.setOnClickListener {
            adapterLister.cancel(history)
            logDialog.dismiss()

        }
        cancelButton?.setOnClickListener {
            logDialog.dismiss()
        }
        logDialog.setCancelable(false)
        logDialog.show()
        logDialog.setOnKeyListener { arg0, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                logDialog.dismiss()
            }
            true
        }
    }



    interface HistoryItemListener : BaseViewOperator {
        fun itemSelected(history: RequestData.Data)
        fun onDisputeClicked(history: RequestData.Data)
        fun cancel(history: RequestData.Data)
    }

}

@BindingAdapter("imageUrldrivericon")
fun setDriverImage(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url)
        .apply(
            RequestOptions().error(R.drawable.simple_profile_bg)
                .placeholder(R.drawable.ic_history_profile_dummy)
        ).into(imageView)
}

@BindingAdapter("vehicleImage")
fun setVehicleImage(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url).into(imageView)
}