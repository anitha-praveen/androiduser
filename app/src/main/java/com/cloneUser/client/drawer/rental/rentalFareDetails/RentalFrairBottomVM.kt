package com.cloneUser.client.drawer.rental.rentalFareDetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.RentalPackageTypes
import com.cloneUser.client.ut.Utilz


class RentalFairBottomVM(
    application: Application,
    val translationModel: TranslationModel,
    val type: RentalPackageTypes.Type,
    val pkg : String
) : AndroidViewModel(application) {
    val totalPrice = MutableLiveData(type.currencySymbol + " " + Utilz.removeZero("${type.totalAmount}"))

    class Factory(
        val application: Application,
        val translationModel: TranslationModel,
        val type: RentalPackageTypes.Type,
        val pkg: String
    ) :
        NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RentalFairBottomVM(application, translationModel, type, pkg) as T
        }

    }

}

