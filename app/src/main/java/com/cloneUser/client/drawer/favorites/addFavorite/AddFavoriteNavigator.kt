package com.cloneUser.client.drawer.favorites.addFavorite

import android.content.Context
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.databinding.FragmentAddFavoriteBinding

interface AddFavoriteNavigator : BaseViewOperator {
    fun closeFragment()
    fun getCtx():Context
    fun goToPickFromMap()
    fun getBinding(): FragmentAddFavoriteBinding
}