package com.cloneUser.client.dialogs.paymentType.adapter

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator

class ChildPaymentTypeVM (
    payTypes: String,
    isSelected : Boolean,
    adapterLister: ChildPayTypeItemListener) {

    var adapterlister: ChildPayTypeItemListener = adapterLister
    var paymentType = ObservableField(payTypes)
    var isSelected = ObservableBoolean(isSelected)


    fun onItemSelected() {
        adapterlister.itemSelected(paymentType.get()!!)
    }


    interface ChildPayTypeItemListener : BaseViewOperator {
        fun itemSelected(payTypes: String)
    }

}

