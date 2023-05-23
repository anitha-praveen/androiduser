package com.cloneUser.client.splash

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.AvailableCountryAndKLang

interface SplashNavigator : BaseViewOperator {
    fun startRequestingPermission()
    fun translationForCurrentLanguage(model: AvailableCountryAndKLang)
    fun openAppUpdateDialog()

}