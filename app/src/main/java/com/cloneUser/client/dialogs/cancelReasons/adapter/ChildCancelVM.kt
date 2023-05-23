package com.cloneUser.client.dialogs.cancelReasons.adapter

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.CancelReason

class ChildCancelVM (
    private val reason: CancelReason.Reason,
    isSelected : Boolean,
    adapterLister: ChildCancelItemListener,
) {

    var adapterlister: ChildCancelItemListener = adapterLister
    var isSelected = ObservableBoolean(isSelected)
    var cancelReason = ObservableField(reason.reason)

    fun onItemSelected() {
        adapterlister.itemSelected(reason)
    }

    interface ChildCancelItemListener : BaseViewOperator {
        fun itemSelected(reason: CancelReason.Reason)
    }

}

