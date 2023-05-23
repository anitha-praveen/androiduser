package com.cloneUser.client.loginOrSignup.login

import android.content.Context
import com.cloneUser.client.base.BaseViewOperator

interface LoginNavigator : BaseViewOperator {
    fun hideKeyBoard()
    fun getContextAttached() : Context
    fun openCountryListDialogue()
    fun openOtp(phone: String , cc : String ,ccId : String)
    fun setupCountryList()
}