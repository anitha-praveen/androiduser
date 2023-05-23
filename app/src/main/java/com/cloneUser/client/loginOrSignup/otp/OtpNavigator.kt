package com.cloneUser.client.loginOrSignup.otp

import android.app.Activity
import android.content.Context
import com.cloneUser.client.base.BaseViewOperator

interface OtpNavigator : BaseViewOperator {
    fun verifyCode(otp: String?)
    fun getOpt(): String?
    fun resendOtp()
    fun goToSignUp(phone: String , cc : String , ccId : String)
    fun goToDrawer()
    fun getAct():Activity
    fun callHideKeyBoard()
    fun getCtx():Context
}