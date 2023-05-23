package com.cloneUser.client.drawer.favorites.pickFromMap

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.cloneUser.client.base.BaseViewOperator

interface PickFromMapNavigator : BaseViewOperator {
    fun getCtx() : Context
    fun getGmap() : GoogleMap
    fun goToFavoritesList()
    fun launchSearchPlace()
}