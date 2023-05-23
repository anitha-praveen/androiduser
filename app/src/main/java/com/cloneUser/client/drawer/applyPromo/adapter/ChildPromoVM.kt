package com.cloneUser.client.drawer.applyPromo.adapter

import android.text.Editable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.PromoCodeModel
import com.cloneUser.client.drawer.rideConfirm.RideConfirmNavigator
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import java.lang.NumberFormatException

class ChildPromoVM(
    promos: PromoCodeModel.Promocode,
    adapterLister: ChildPromoItemListener,
    session: SessionMaintainence,
    mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, RideConfirmNavigator>(session, mConnect) {
    var promo: PromoCodeModel.Promocode = promos
    var adapterlister: ChildPromoItemListener = adapterLister
    var promoCode = promo.promoCode
    var promoTitle = promo.selectOfferOptionTitle
    var discountAmount = if (promo.promoType == 2) "( ${translationModel.txt_upto} ${
        try {
            "${promo.percentage?.toDouble()?.toInt()}"
        } catch (e: NumberFormatException) {
            "${promo.percentage}"
        }
    }% )" else "( " + translationModel.txt_upto + " " + promo.countrySymbol + promo.amount + " )"
    var promoDescription = promo.description
    var promoImg = ObservableField(promo.promoIcon)
    val showDescription = ObservableField(promoDescription.isNullOrEmpty())


    fun onItemSelected(view: View) {
        adapterlister.itemSelected(promo)
    }


    interface ChildPromoItemListener : BaseViewOperator {
        fun itemSelected(promo: PromoCodeModel.Promocode)
    }

    override fun onSuccessfulApi(response: BaseResponse?) {

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {

    }

}

@BindingAdapter("promoImageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url)
        .apply(RequestOptions().error(R.drawable.ic_car).placeholder(R.drawable.ic_car))
        .into(imageView)
}