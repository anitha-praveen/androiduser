package com.cloneUser.client.drawer.referral

import androidx.fragment.app.FragmentActivity
import com.cloneUser.client.base.BaseViewOperator

interface ReferralNavigator : BaseViewOperator{
    fun getAct():FragmentActivity?
}