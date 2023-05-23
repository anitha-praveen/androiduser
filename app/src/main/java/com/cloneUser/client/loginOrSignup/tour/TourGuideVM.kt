package com.cloneUser.client.loginOrSignup.tour

import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject
import androidx.databinding.BindingAdapter




class TourGuideVM  @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, TourGuideNavigator>(session, mConnect) {
    var skipDisable = ObservableBoolean(false)
    var forwardtxt = ObservableField<String>("")
    var isFirst = ObservableBoolean(true)
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
    }


    fun onClickSkip() {
        getNavigator().skipClick()
    }

    fun onClickNext() {
        getNavigator().forwardClick()
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }
}

@BindingAdapter("layout_width")
fun setLayoutWidth(view: CardView, width: Float) {
    val layoutParams: ViewGroup.LayoutParams = view.layoutParams
    layoutParams.width = width.toInt()
    view.layoutParams = layoutParams
}