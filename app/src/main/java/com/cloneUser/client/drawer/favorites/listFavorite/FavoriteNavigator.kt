package com.cloneUser.client.drawer.favorites.listFavorite

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.FavPlace

interface FavoriteNavigator : BaseViewOperator {
    fun addList(favPlace: List<FavPlace.Favourite>?)
    fun gotoAddFavorite()
    fun closeFragment()
    fun deleteFavorite(favPlace: FavPlace.Favourite)
}