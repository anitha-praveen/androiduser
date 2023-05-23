package com.cloneUser.client.drawer.adapters.complaints_list_adapter

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.ComplaintsModel

class ComplaintListAdapterVM (
    var complaint: ComplaintsModel, adapterLister: AdapterViewModelListener
) {
    var isSelected = ObservableBoolean(complaint.isSelected)
    var complaintTitle  = ObservableField(complaint.title?:"")
    var adapterlister: AdapterViewModelListener = adapterLister

    fun onClickItem(){
        adapterlister.itemSelected(complaint)
    }
    interface AdapterViewModelListener : BaseViewOperator {
        fun itemSelected(complaint: ComplaintsModel)
    }

}
