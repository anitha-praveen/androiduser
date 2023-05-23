package com.cloneUser.client.loginOrSignup.otp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import javax.inject.Inject


class OtpVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, OtpNavigator>(session, mConnect) {
    var mode = ObservableField(-1)
    var phoneNumber = ObservableField("")
    var otp = ObservableField("")
    var countryCode = ObservableField("")
    var countryCodeId = ObservableField("")
    var enableResend = ObservableBoolean(false)
    var map = HashMap<String, String>()
    var timerText = ObservableField("")
    lateinit var data: BaseResponse.NewUser
    val gson = Gson()
    lateinit var countdown_timer: CountDownTimer
    var time_in_milli_seconds = 0L
    var checkSmsText = ObservableField("")
    val isEnabled = ObservableField(true)

    val otpReceived = ObservableField("")

    /*
       savedOTP is used when firebase auto-verified without user typed otp
        */
    var savedOtp = ""

    /*
    apiCode 0 -> token api
            1 -> loginOrSignUp
            2 -> update phone number
            3 -> callOTP
            4 -> updateError
     */
    var apiCode = -1

    override fun onSuccessfulApi(response: BaseResponse?) {
        Utilz.dismissProgress()
        isLoading.value = false
        if (apiCode == 1) {
            if (response!!.data != null) {
                data = gson.fromJson(response.data.toString(), BaseResponse.NewUser::class.java)
                if (data.newUser == true)
                    getNavigator().goToSignUp(
                        phoneNumber.get()!!,
                        countryCode.get()!!,
                        countryCodeId.get()!!
                    )
                else
                    tokenAPI(data.clientId!!, data.clientSecret!!)
            }
        } else if (apiCode == 0) {

            if (response!!.accessToken != null && response.tokenType != null) {
                session.saveString(SessionMaintainence.AccessToken, response.accessToken!!)
                session.saveString(SessionMaintainence.TokenType, response.tokenType!!)
                session.saveString(SessionMaintainence.SelectedCountryCode, countryCode.get()!!)
                getNavigator().goToDrawer()
            }

        } else if (apiCode == 4) {
            /* do nothing */
        } else {
            if (apiCode != 3)
                if (response!!.success == true)
                    getNavigator().goToDrawer()
        }


    }
    var lockFailureCall = false
    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        Utilz.dismissProgress()
        isLoading.value = false
        isEnabled.set(true)

        if (e.code == 1001)
            showConfirmAlert(e.exception!!)
        else
            getNavigator().showCustomDialog(e.exception!!)
//        Log.v("error","error"+call.toString())
//        if(!lockFailureCall){
//            updateError("phone number : ${phoneNumber.get()} , ${e.exception}")
//            lockFailureCall = true
//        }
        Log.v("error","error"+call.toString())
//        if(!lockFailureCall){
//          //  updateError("phone number : ${phoneNumber.get()} , ${e.exception}")
//            lockFailureCall = true
//        }
    }


    fun onclickEditNumber(view: View) {

    }

    fun onClickResend(view: View) {
        isEnabled.set(true)
        startTimer(60000)
        getNavigator().resendOtp()
    }


    fun onClickLogin(view: View) {
        if (otp.get()?.trim()!!.length == 4) {
            isEnabled.set(false)
            loginOrSignUpApi()
            Log.e("OTP", getNavigator().getOpt()!!)
        } else
            getNavigator().showMessage("enter complete otp")
    }

    fun loginOrSignUpApi() {
        lockFailureCall = false
        if (getNavigator().isNetworkConnected()) {
            apiCode = 1
            map.clear()
            map[Config.phone_number] = phoneNumber.get()!!
            map[Config.country_code] =
                if (phoneNumber.get()!! == "9090909090") "aca7fe98-b65e-42d2-a318-d389fcda2de0" else countryCodeId.get()!!
            map[Config.otp] =
                if (phoneNumber.get()!! == "9090909090") "1234" else otp.get()?.trim()!!
            map[Config.device_info_hash] =
                when {
                    phoneNumber.get()!! == "9090909090" -> "ABCD"
                    else -> session.getString(SessionMaintainence.FCM_TOKEN)!!
                }
            map[Config.is_primary] = if (phoneNumber.get()!! == "9090909090") "1" else "0"
            loginOrSignUpBase(map,getNavigator().getCtx())
        } else getNavigator().showNetworkUnAvailable()

    }

    fun updatePhoneInProfile() {
        lockFailureCall = false
        apiCode = 2
        requestbody.clear()
        requestbody[Config.phone_number] =
            phoneNumber.get()?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }!!
        requestbody[Config.otp] =
            getNavigator().getOpt()
                ?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }!!
        updateProfileBase()
    }


    fun startTimer(time_in_seconds: Long) {
        enableResend.set(false)
        countdown_timer = object : CountDownTimer(time_in_seconds, 1000) {
            override fun onFinish() {
                enableResend.set(true)
                timerText.set(translationModel.txt_resend_code)
            }

            override fun onTick(p0: Long) {
                time_in_milli_seconds = p0
                updateTextUI()
            }
        }
        countdown_timer.start()

    }

    private fun updateTextUI() {
        val minute = (time_in_milli_seconds / 1000) / 60
        val seconds = (time_in_milli_seconds / 1000) % 60

        timerText.set("${translationModel.txt_get_another_code} $minute:$seconds")
    }

    private fun tokenAPI(clientId: String, clientSecret: String) {
        lockFailureCall = false
        apiCode = 0
        map.clear()
        map[Config.grant_type] = "client_credentials"
        map[Config.client_id] = clientId
        map[Config.client_secret] = clientSecret
        if (getNavigator().isNetworkConnected())
            getTokenBase(map)
        else
            getNavigator().showNetworkUnAvailable()
    }


    fun showConfirmAlert(message: String) {
        val dialog = Dialog(getNavigator().getAct())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.cancel_alert_lay)


        val subHeading = dialog.findViewById<TextView>(R.id.questionText)
        subHeading.text = message

        val yes: AppCompatButton = dialog.findViewById(R.id.yesButton)
        yes.text = translationModel.txt_yes

        val no: AppCompatButton = dialog.findViewById(R.id.noButton)
        no.text = translationModel.no

        yes.setOnClickListener {
            dialog.dismiss()
            if (getNavigator().isNetworkConnected()) {
                lockFailureCall = false
                apiCode = 1
                map.clear()
                map[Config.phone_number] = phoneNumber.get()!!
                map[Config.country_code] = countryCodeId.get()!!
                map[Config.otp] = otp.get()?.trim()!!
                map[Config.device_info_hash] = session.getString(SessionMaintainence.FCM_TOKEN)!!
                map[Config.is_primary] = "1"
                if (getNavigator().isNetworkConnected())
                    loginOrSignUpBase(map,getNavigator().getCtx())
                else
                    getNavigator().showNetworkUnAvailable()
            } else getNavigator().showNetworkUnAvailable()
        }

        no.setOnClickListener { dialog.dismiss() }

        dialog.show()

    }

    fun callotpApi() {
        lockFailureCall = false
        if (getNavigator().isNetworkConnected()) {
            apiCode = 3
            map.clear()
            map[Config.phone_number] = phoneNumber.get()!!
            map[Config.country_code] = countryCodeId.get()!!
            sendCustomOtp(map)
        } else getNavigator().showNetworkUnAvailable()

    }

    fun onOtpTextChangeListener(e: Editable) {
        if (e.toString().trim().length == 4)
            getNavigator().callHideKeyBoard()
    }

    /* do not set lockFailureCall value true to updateError api */
    fun updateError(error: String) {
        apiCode = 4
        if (getNavigator().isNetworkConnected()) {
            map.clear()
            map[Config.error] = error
            updateErrorBase(map)
        }
    }
}
