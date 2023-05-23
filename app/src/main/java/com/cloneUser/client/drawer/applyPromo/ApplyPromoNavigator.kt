package com.cloneUser.client.drawer.applyPromo

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.PromoCodeModel

interface ApplyPromoNavigator: BaseViewOperator {
    fun addList(promos : List<PromoCodeModel.Promocode>?)
    fun onPromoSelected(promo: PromoCodeModel.Promocode)
    fun showPromoSuccess()
    fun showNoPromo()
    fun getTripType():String
}