package com.cloneUser.client.drawer.adapters.suggestion_adapter

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.SuggestionModel

class SuggestionAdapterVM (
    var suggestion: SuggestionModel, adapterLister: AdapterViewModelListener
) {
    var isSelected = ObservableBoolean(suggestion.isSelected)
    var suggestionTitle  = ObservableField(suggestion.title?:"")
    var adapterlister: AdapterViewModelListener = adapterLister

    fun onClickItem(){
        adapterlister.itemSelected(suggestion)
    }
    interface AdapterViewModelListener : BaseViewOperator {
        fun itemSelected(suggestion: SuggestionModel)
    }

}
