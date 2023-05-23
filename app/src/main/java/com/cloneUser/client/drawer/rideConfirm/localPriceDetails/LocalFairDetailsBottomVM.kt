package com.cloneUser.client.drawer.rideConfirm.localPriceDetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.TypesModel
import com.cloneUser.client.ut.Utilz

class LocalFairDetailsBottomVM(
    application: Application,
    val translationModel: TranslationModel,
    val type: TypesModel.ZoneTypePrice,
    val typesModel: TypesModel,
    val nav : LocalFairDetailsBottomNav
) : AndroidViewModel(application) {

    val isPromoApplied = MutableLiveData(false)
    val total = MutableLiveData("")
    val showPricePerTime = MutableLiveData(if(type.pricePerTime == null) false else if(type.pricePerTime == "0") false else type.pricePerTime != "0.0")
    val showOutZonePrice = MutableLiveData(if(type.outZonePrice.isNullOrEmpty()) false else if(type.outZonePrice == "0") false else type.outZonePrice != "0.0")
    val basePrice = MutableLiveData(typesModel.currencySymble+" "+ Utilz.removeZero(type.basePrice?:""))
    val ratePerKm = MutableLiveData(typesModel.currencySymble+" "+Utilz.removeZero(type.computedPrice?:""))
    val pricePerTime = MutableLiveData(typesModel.currencySymble+" "+Utilz.removeZero(type.pricePerTime?:""))
    val waitingCharge = MutableLiveData(typesModel.currencySymble+" "+Utilz.removeZero(type.waitingCharge?:""))
    val outZonePrice = MutableLiveData(typesModel.currencySymble+" "+Utilz.removeZero(type.outZonePrice?:""))
    val promoAmount = MutableLiveData(typesModel.currencySymble+" "+Utilz.removeZero(type.promoAmount?:""))
    val bookingFees = MutableLiveData(typesModel.currencySymble+" "+Utilz.removeZero(type.bookingFees?:""))
    val baseDistanceDesc = MutableLiveData(translationModel.txt_for+" "+Utilz.removeZero(type.baseDistance?:"")+" "+translationModel.txt_km)
    val ratePerKmDesc = MutableLiveData(translationModel.txt_for +" "+Utilz.removeZero("${type.computedDistance}")+" "+ translationModel.txt_km +" x "+ Utilz.removeZero(type.pricePerDistance?:""))

    fun onClickGotIt(){
        nav.gotIt()
    }
    class Factory(
        val application: Application,
        val translationModel: TranslationModel,
        val type: TypesModel.ZoneTypePrice,
        val typesModel: TypesModel,
        val nav : LocalFairDetailsBottomNav
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocalFairDetailsBottomVM(application, translationModel, type, typesModel, nav) as T
        }

    }

}