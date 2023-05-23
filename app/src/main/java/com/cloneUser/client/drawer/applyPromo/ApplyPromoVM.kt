package com.cloneUser.client.drawer.applyPromo

import android.text.Editable
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.PromoCodeModel
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class ApplyPromoVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) : BaseVM<BaseResponse, ApplyPromoNavigator>(session, mConnect) {

    /*
    apiCode 0 -> get promo list
            1 -> apply promo code
     */
    private var apiCode = -1
    var map: HashMap<String, String> = HashMap()
    var promoModel = PromoCodeModel.Promocode()
    var isPromoApplied = ObservableBoolean(false)
    var isPromoAvailable = ObservableBoolean(false)
    var promoCode = ObservableField("")
    var currency = ""
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCode == 0 && response!!.data != null) {
            val response: PromoCodeModel =
                Gson().fromJson(Gson().toJson(response.data), PromoCodeModel::class.java)
            isPromoAvailable.set(response.promocode?.isNotEmpty() == true)
            if (response.promocode?.isNotEmpty() == true) {
                getNavigator().addList(response.promocode)
            }else{
                getNavigator().showNoPromo()
            }
        } else if (apiCode == 1) {
            val response: PromoCodeModel =
                Gson().fromJson(Gson().toJson(response?.data), PromoCodeModel::class.java)
            promoModel.amount = response.user?.amount
            promoModel.promoType = response.user?.promoType
            promoModel.percentage = response.user?.percentage
            isPromoApplied.set(true)
            showApply.set(false)
            getNavigator().showPromoSuccess()
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showSnackBar(e.exception!!)
    }

    fun getPromoList(tripType: String) {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 0
            getPromoListBase(tripType)
        } else
            getNavigator().showNetworkUnAvailable()
    }

    fun applyPromo(promoCode: String) {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 1
            map.clear()
            map[Config.promo_code] = promoCode
            map[Config.trip_type] = getNavigator().getTripType()
            applyPromoBase(map)
        }
    }

    fun removePromo(view: View) {
        promoCode.set("")
        isPromoApplied.set(false)
    }

    fun applyPromoEdited() {
        if (promoCode.get()!!.isNotEmpty())
            applyPromo(promoCode.get()!!)
        else
            getNavigator().showMessage(translationModel.text_Please_Select_an_Option!!)
    }

    var showApply = ObservableBoolean(false)
    fun onPromoEdit(e: Editable) {
        if(!isPromoApplied.get() && e.isNotEmpty())
            showApply.set(true)
        else if(isPromoApplied.get())
            showApply.set(false)
    }
}