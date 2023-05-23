package com.cloneUser.client.drawer.mapfragment

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.FragmentMapBinding

interface MapFragmentNavigator : BaseViewOperator {
    fun goToSplash()
    fun getCtx():Context?
    fun getFrgAct():FragmentActivity?
    fun getGMap() : GoogleMap
    fun goToConfirmDestination()
    fun callRequestInProgressFromDrawer()
    fun checkIsTripAddressChange(): Boolean
    fun openSos()
    fun avoidScrolling()
    fun enableScrolling()
    fun openFavBottomSheet()
    fun setFavDetails(lastTripHistory: FavPlace.LastTripHistory)
    fun getBinds(): FragmentMapBinding
    fun loadFavoriteLocations(favPlace: List<FavPlace.Favourite>?)
    fun loadRecentLocations(lastTrips: List<FavPlace.LastTripHistory>)
    fun setDropFromFavorites(place : FavPlace.Favourite)
    fun setDropFromRecent(place: FavPlace.LastTripHistory)
    fun setupAdapter()
    fun openSideMenu()
    fun openPickupEdit()
    fun addCurrentToFavo()
    fun openNotificationPage()
    fun getMarkerIcon(drawable: Int): BitmapDescriptor?
    fun goTorideConfirmFragement(place:FavPlace.Favourite)
    fun openRental()
    fun openOutstation()

}