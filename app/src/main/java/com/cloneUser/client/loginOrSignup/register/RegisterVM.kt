package com.cloneUser.client.loginOrSignup.register

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.RealPathUtil
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

class RegisterVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, RegisterNavigator>(session, mConnect) {
    var FirstName = ObservableField("")
    var LastName = ObservableField("")
    var Email = ObservableField("")
    var countryForDisplay = ObservableField("")
    var countryForApi = ObservableField("")
    var phoneForDisplay = ObservableField("")
    var errorMsg = ""
    var referral_code = ObservableField("")
    var map = HashMap<String, String>()
    val gson = Gson()
    private var tokenApiCalled = false
    lateinit var data: BaseResponse.NewUser
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        Utilz.dismissProgress()
        if (!tokenApiCalled) {
            if (response!!.data != null) {
                data = gson.fromJson(response.data.toString(), BaseResponse.NewUser::class.java)
                tokenAPI(data.clientId!!, data.clientSecret!!)
            }
        } else {
            if (response!!.accessToken != null && response.tokenType != null) {
                session.saveString(SessionMaintainence.AccessToken, response.accessToken!!)
                session.saveString(SessionMaintainence.TokenType, response.tokenType!!)
                getNavigator().goToDrawer()
            }
        }

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        Utilz.dismissProgress()
        if (e.code == 1001)
            showConfirmAlert(e.exception!!)
        else
            getNavigator().showCustomDialog(e.exception!!)
    }

    fun onClickOutSide() {

    }

    fun goBack() {
        getNavigator().close()
    }

    fun onClickSignup(view: View) {
        if (getNavigator().isNetworkConnected()) {
            if (validation()) {
                tokenApiCalled = false
                requestbody.clear()

                requestbody[Config.firstname] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), FirstName.get()!!)


                requestbody[Config.phone_number] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), phoneForDisplay.get()!!)

                requestbody[Config.country_code] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), countryForApi.get()!!)


                if (Email.get()!!.isNotEmpty()) {
                    requestbody[Config.email] =
                        RequestBody.create("text/plain".toMediaTypeOrNull(), Email.get()!!)
                }

                if (referral_code.get()!!.isNotEmpty()) {
                    requestbody[Config.referral_code] =
                        RequestBody.create("text/plain".toMediaTypeOrNull(), referral_code.get()!!)
                }


                requestbody[Config.device_info_hash] =
                    RequestBody.create(
                        "text/plain".toMediaTypeOrNull(),
                        session.getString(SessionMaintainence.FCM_TOKEN)!!
                    )

                requestbody[Config.device_type] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), "ANDROID")

                requestbody[Config.is_primary] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), "0")

                if (realPath != null) {
                    val reqFile =
                        realFile?.let { RequestBody.create("image/*".toMediaTypeOrNull(), it) }
                    body =
                        reqFile?.let {
                            MultipartBody.Part.createFormData("profile_pic", realFile!!.name,
                                it
                            )
                        }
                    signUpWithImageBase(getNavigator().getCtx())
                } else
                    signUpBase(getNavigator().getCtx())
            }
        } else
            getNavigator().showNetworkUnAvailable()


    }

    fun tokenAPI(clientId: String, clientSecret: String) {
        tokenApiCalled = true
        map.clear()
        map[Config.grant_type] = "client_credentials"
        map[Config.client_id] = clientId
        map[Config.client_secret] = clientSecret
        if (getNavigator().isNetworkConnected())
            getTokenBase(map)
        else
            getNavigator().showNetworkUnAvailable()
    }

    private fun validation(): Boolean {
        errorMsg = ""
        if (FirstName.get().isNullOrEmpty())
            errorMsg = translationModel.txt_name_cannot_be_empty ?: ""
        else if (Email.get()!!.isNotEmpty() && !Utilz.isEmailValid(Email.get()!!.trim()) && android.util.Patterns.EMAIL_ADDRESS.matcher(Email.get()).matches())
            errorMsg = translationModel.text_error_email_valid ?: ""

        return if (errorMsg.isEmpty())
            true
        else {
            getNavigator().showSnackBar(errorMsg)
            false
        }

    }

    private fun showConfirmAlert(message: String) {
        val dialog = Dialog(getNavigator().getAct())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.cancel_alert_lay)


        val subHeading = dialog.findViewById<TextView>(R.id.questionText)
        subHeading.text = message

        val yes: AppCompatButton = dialog.findViewById(R.id.yesButton)
        yes.setText(translationModel.txt_yes)

        val no: AppCompatButton = dialog.findViewById(R.id.noButton)
        no.text = translationModel.no

        yes.setOnClickListener {
            if (getNavigator().isNetworkConnected()) {
                tokenApiCalled = false
                requestbody.clear()

                requestbody[Config.firstname] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), FirstName.get()!!)


                requestbody[Config.phone_number] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), phoneForDisplay.get()!!)

                requestbody[Config.country_code] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), countryForApi.get()!!)


                if (Email.get()!!.isNotEmpty()) {
                    requestbody[Config.email] =
                        RequestBody.create("text/plain".toMediaTypeOrNull(), Email.get()!!)
                }

                if (referral_code.get()!!.isNotEmpty()) {
                    requestbody[Config.referral_code] =
                        RequestBody.create("text/plain".toMediaTypeOrNull(), referral_code.get()!!)
                }


                requestbody[Config.device_info_hash] =
                    RequestBody.create(
                        "text/plain".toMediaTypeOrNull(),
                        session.getString(SessionMaintainence.FCM_TOKEN)!!
                    )

                requestbody[Config.device_type] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), "ANDROID")

                requestbody[Config.is_primary] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), "1")

                if (realPath != null) {
                    val reqFile =
                        realFile?.let { it1 ->
                            RequestBody.create("image/*".toMediaTypeOrNull(),
                                it1
                            )
                        }
                    body =
                        reqFile?.let { it1 ->
                            MultipartBody.Part.createFormData("profile_pic", realFile!!.name,
                                it1
                            )
                        }
                    signUpWithImageBase(getNavigator().getCtx())
                } else
                    signUpBase(getNavigator().getCtx())
            } else
                getNavigator().showNetworkUnAvailable()
        }
        no.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }

    var fnameTitle = ObservableBoolean(false)
    fun onNameEdit(editable: Editable) {
        fnameTitle.set(editable.toString().trim().isNotEmpty())
    }

    var emailTitle = ObservableBoolean(false)
    fun onEmailEdit(editable: Editable) {
        emailTitle.set(editable.toString().trim().isNotEmpty())
    }

    var referralTitle = ObservableBoolean(false)
    fun onReferralEdit(editable: Editable) {
        referralTitle.set(editable.toString().trim().isNotEmpty())
    }

    fun onClickImage() {
        getNavigator().openImageDialog()
    }

    var bitmapProfilePicture: ObservableField<String> = ObservableField<String>()
    var realPath: String? = null
    var realFile: File? = null

    fun onSelectFromGalleryResult(data: Intent?) {
        if (data != null) {
            val s_uri = data.data
            realPath = RealPathUtil.getRealPath(getNavigator().getCtx(), s_uri!!)
            Log.v("fatal", realPath!!)
            realFile = File(if (realPath == null) "" else realPath)
            bitmapProfilePicture.set(realPath)
            realPath?.let { getNavigator().setImage(it) }
        }
    }

    fun onCaptureImageResult(data: Intent?) {
        if (data != null) {
            if (data.hasExtra("data")) {
                realPath = Utilz.getImageUri(
                    getNavigator().getCtx(), data.extras?.get(
                        "data"
                    ) as Bitmap
                )
                realFile = File(realPath)
                realPath?.let { getNavigator().setImage(it) }
                bitmapProfilePicture.set(realPath)
            }
        }

    }


}
