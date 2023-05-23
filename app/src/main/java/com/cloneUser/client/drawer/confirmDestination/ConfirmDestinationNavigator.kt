package com.cloneUser.client.drawer.confirmDestination

import android.content.Context
import android.location.Location
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.FragmentConfirmDestinationBinding

interface ConfirmDestinationNavigator : BaseViewOperator {
    fun getCtx() : Context
    fun gotoRideConfirm()
    fun callRequestInProgressFromDrawer()
    fun openRiderSelection()

    fun loadFavorites(favPlaces: List<FavPlace.Favourite>)
    fun setSelectedFavPlace(favPlace : FavPlace.Favourite)

    fun loadRecentLocations(lastTrips: List<FavPlace.LastTripHistory>)
    fun setSelectedRecentLocation(lastTrip: FavPlace.LastTripHistory)

    fun loadSearchResults(places: List<FavPlace.Favourite>)
    fun setSelectedSearchResult(place : FavPlace.Favourite)

    fun getBinds() : FragmentConfirmDestinationBinding
    fun setFavoriteList(favouriteList: List<FavPlace.Favourite>)
    fun setLastTripHistory(lastTripsList : List<FavPlace.LastTripHistory>)
    fun openFavBottomSheet()
    fun setFavDetails(lastTripHistory: FavPlace.LastTripHistory)
    fun getCurrentLoc() : Location?
    fun hideKeyBoardNav()



}