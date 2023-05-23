package com.cloneUser.client.dialogs.paymentType

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class PaymentTypeVM(
    application: Application,
    val translationModel: TranslationModel,
    private val nav: PaymentTypeNavigator
) : AndroidViewModel(application) {

    class Factory(
        val application: Application,
        val translationModel: TranslationModel,
        val nav : PaymentTypeNavigator
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PaymentTypeVM(application, translationModel, nav) as T
        }

    }

    var paymentType = ObservableField("")


    fun confirm() {
        if (paymentType.get()!!.isNotEmpty())
            nav.sendPaymentType()
        else
            translationModel.text_Please_Select_an_Option?.let { nav.showMessage(it) }
    }
}