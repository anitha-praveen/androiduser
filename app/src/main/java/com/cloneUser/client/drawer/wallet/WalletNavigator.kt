package com.cloneUser.client.drawer.wallet

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.responseModels.WalletResponsModel

interface WalletNavigator : BaseViewOperator {
    fun showToast(message:String)
    fun setUpAdapter(list:ArrayList<WalletResponsModel>, type: BaseResponse.DataObjectsAllApi)

}