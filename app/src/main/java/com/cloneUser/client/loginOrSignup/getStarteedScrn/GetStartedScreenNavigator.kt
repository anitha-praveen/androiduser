package com.cloneUser.client.loginOrSignup.getStarteedScrn

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.Languages

interface GetStartedScreenNavigator : BaseViewOperator {
    fun setSelectedLanguage(languages: Languages)
    fun initiateNavigation()
}