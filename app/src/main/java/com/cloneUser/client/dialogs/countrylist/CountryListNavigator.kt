package com.cloneUser.client.dialogs.countrylist

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.Country

interface CountryListNavigator : BaseViewOperator{
    fun clickedItem(country : Country)
    fun close()
}