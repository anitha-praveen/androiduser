package com.cloneUser.client.drawer.complaint.history.suggestionHistory

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.SuggestionHistoryModel

interface SuggestionHistoryNav : BaseViewOperator {
    fun setData(list : List<SuggestionHistoryModel.Suggestion>)
    fun getTranslation() : TranslationModel
}