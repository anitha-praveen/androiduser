package com.cloneUser.client.loginOrSignup.register

import android.app.Activity
import android.content.Context
import com.cloneUser.client.base.BaseViewOperator

interface RegisterNavigator : BaseViewOperator {
    fun getAct():Activity
    fun goToDrawer()
    fun close()
    fun openImageDialog()
    fun getCtx(): Context
    fun setImage(url : String)

}