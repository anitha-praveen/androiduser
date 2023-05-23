package com.cloneUser.client.drawer.outStation.types

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.OutStationTypes
import kotlin.math.roundToInt

class ChildTypesOutStationVm(
    private val typesModel: OutStationTypes,
    val adapterLister:ChildOutStationTypesItemListener,
    val translationModel: TranslationModel,
    val isTwoWay : Boolean
) {
    var vehicalType = typesModel.getVehicle?.vehicleName

    var price = "${typesModel.currencySymbol}${
        try {
            if(isTwoWay) typesModel.distancePriceTwoWay!!.roundToInt() else typesModel.distancePrice!!.roundToInt()
        }catch (e:NumberFormatException){
            if(isTwoWay) typesModel.distancePriceTwoWay else typesModel.distancePrice
        }
    } / ${translationModel.txt_km} "
    val isSelected = ObservableBoolean(!(typesModel.isSelected == null || typesModel.isSelected == false))
    var imageUrl = ObservableField(if (isSelected.get()) typesModel.getVehicle?.highlightImage else typesModel.getVehicle?.image)



    fun onItemSelected() {
        typesModel.isSelected = true
        adapterLister.itemSelected(typesModel)
    }




    interface ChildOutStationTypesItemListener : BaseViewOperator {
        fun itemSelected(typesModel: OutStationTypes)
    }
}
