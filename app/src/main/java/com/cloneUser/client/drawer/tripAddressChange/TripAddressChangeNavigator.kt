package com.cloneUser.client.drawer.tripAddressChange

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace

interface TripAddressChangeNavigator : BaseViewOperator{
    fun getCtx():Context


    fun loadFavorites(favPlaces: List<FavPlace.Favourite>)
    fun setSelectedFavPlace(favPlace : FavPlace.Favourite)

    fun loadRecentLocations(lastTrips: List<FavPlace.LastTripHistory>)
    fun setSelectedRecentLocation(lastTrip: FavPlace.LastTripHistory)

    fun loadSearchResults(places: List<FavPlace.Favourite>)
    fun setSelectedSearchResult(place : FavPlace.Favourite)

    fun getGMap() : GoogleMap?
    fun callRequestInProgressFromDrawer()

    fun closeFromViewModel()


}