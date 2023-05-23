package com.cloneUser.client.drawer.rideConfirm.changeAddress

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace

interface RideConfirmChangeNavigator : BaseViewOperator {
    fun loadAdapter(favPlace: List<FavPlace.Favourite>?)
    fun getCtx():Context
    fun getGMap():GoogleMap?
    fun onItemSelected(place: FavPlace.Favourite)
    fun gotoRideConfirm()

}