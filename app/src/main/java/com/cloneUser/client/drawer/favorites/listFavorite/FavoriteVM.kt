package com.cloneUser.client.drawer.favorites.listFavorite

import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import java.util.ArrayList
import javax.inject.Inject

class FavoriteVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, FavoriteNavigator>(session, mConnect) {

    /*
    apiCode 0-> get favorites
     */
    var apiCode = -1
    var favPlacesVM: MutableList<FavPlace.Favourite> = ArrayList()


    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCode == 0 && response!!.data != null) {
            val resp: FavPlace =
                Gson().fromJson(Gson().toJson(response.data), FavPlace::class.java)
            if (resp.favouriteList != null) {
                getNavigator().addList(resp.favouriteList)
            }
        }else if(apiCode == 1){
            getFavoritePlaces()
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun onClickBack() {
        getNavigator().closeFragment()
    }

    fun onClickAdd() {
        getNavigator().gotoAddFavorite()
    }

    fun getFavoritePlaces() {
        apiCode = 0
        if (getNavigator().isNetworkConnected())
            getFavoritePlaceBase()
        else
            getNavigator().showNetworkUnAvailable()
    }

    fun deleteFavoritePlace(slug : String){
        apiCode = 1
        if(getNavigator().isNetworkConnected())
            deleteFavoritePlaceBase(slug)
        else
            getNavigator().showNetworkUnAvailable()
    }

}