package com.cloneUser.client.drawer.profile

import android.content.Context
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.connection.responseModels.Languages

interface ProfileNavigator : BaseViewOperator{
    fun editProfile(mode: Int, value: String)
    fun alertSelectCameraGallery()
    fun getCtx():Context
    fun setImage(url : String)
    fun openLanguageSelection()
    fun setSelectedLanguage(languages: Languages)
    fun refresh()
    fun logOut()
    fun loadAdapter(places: List<FavPlace.Favourite>)
    fun deleteFavorite(favPlace: FavPlace.Favourite)
    fun showProfileImg(url: String)
}