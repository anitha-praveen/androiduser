package com.cloneUser.client.drawer.language

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.Languages


interface LanguageNavigator : BaseViewOperator {
    fun setSelectedLanguage(languages: Languages)
    fun refresh()
    fun close()
}