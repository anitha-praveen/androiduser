package com.cloneUser.client.dialogs.addressEdit

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.DialogAddressEditBinding

interface AddressEditNavigator : BaseViewOperator {
    fun loadSearchResults(places: List<FavPlace.Favourite>)
    fun getCtx():Context?
    fun setSelectedSearchResult(place : FavPlace.Favourite)
    fun getGMap() : GoogleMap?
    fun close()
    fun sendToTarget()
    fun getBindss() : DialogAddressEditBinding
}