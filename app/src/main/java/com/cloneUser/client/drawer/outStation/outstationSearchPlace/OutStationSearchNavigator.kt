package com.cloneUser.client.drawer.outStation.outstationSearchPlace

import android.content.Context
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace
import com.google.android.gms.maps.GoogleMap


interface OutStationSearchNavigator: BaseViewOperator {
    fun loadAdapter(places: List<FavPlace.Favourite>)
    fun setPlace(place : FavPlace.Favourite)
    fun getCtx(): Context
    fun getGMap(): GoogleMap
    fun closeFrag()
    fun outOfStationDialog()
}