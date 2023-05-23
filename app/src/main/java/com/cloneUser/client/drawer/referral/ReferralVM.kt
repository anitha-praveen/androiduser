package com.cloneUser.client.drawer.referral

import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.ReferralModel
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject
import android.content.Intent
import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.startActivity
import com.cloneUser.client.BuildConfig
import com.cloneUser.client.R
import java.lang.Exception


class ReferralVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, ReferralNavigator>(session, mConnect) {
    var refContent = ObservableField("")
    var refAmount = ObservableField("")
    var referralCode = ObservableField("")
    var referByUser = ObservableField("")
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (response != null) {
            val refModel: ReferralModel =
                Gson().fromJson(Gson().toJson(response.data), ReferralModel::class.java)
            refModel.referralAmount?.let { amt ->
                refModel.currencySymbol?.let { s ->
                    refAmount.set("$amt $s")
                    refModel.referByUserAmount?.let { ua ->
                        refContent.set("${translationModel.txt_invite_content} $ua $s")
                    }
                }
            }
            refModel.referralCode?.let { referralCode.set(it) }
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun onClickShare() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                getNavigator().getAct()?.getString(R.string.app_name)
            )
            var shareMessage = "\nHi, use my referral code ${referralCode.get()}\n\n"
            shareMessage =
                """
        ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
        
        
        """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            getNavigator().getAct()?.startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }
}