package com.cloneUser.client.drawer.profile.proAddFvourites

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace

interface ProAddFavoNavigator : BaseViewOperator {
    fun loadAdapter(places: List<FavPlace.Favourite>)
    fun setPlace(place : FavPlace.Favourite)
    fun getCtx():Context
    fun getGMap():GoogleMap
    fun closeFrag()
}