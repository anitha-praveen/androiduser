package com.cloneUser.client.loginOrSignup.tour

import com.cloneUser.client.base.BaseViewOperator

interface TourGuideNavigator : BaseViewOperator {
    fun forwardClick()
    fun skipClick()
}