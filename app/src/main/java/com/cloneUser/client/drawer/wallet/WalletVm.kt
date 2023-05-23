package com.cloneUser.client.drawer.wallet

import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.WalletResponsModel
import com.cloneUser.client.ut.SessionMaintainence
import retrofit2.Call
import javax.inject.Inject

class WalletVm @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper
) :
    BaseVM<BaseResponse, WalletNavigator>(session, mConnect) {
    val list = ArrayList<WalletResponsModel>()
    var apiCall = -1
    var currencyType = ObservableField("")
    var totalAmount = ObservableField("0")
    var amountToBeAdded = ObservableField("")
    var predefinedAmount1 = "500"
    var predefinedAmount2 = "1000"
    var map = HashMap<String, String>()
    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (response!!.data != null && apiCall == 0) {
            val json = Gson().toJson(response.data)
            val obj = Gson().fromJson(json, BaseResponse.DataObjectsAllApi::class.java)
            list.clear()
            list.addAll(obj.walletTransaction!!)
            getNavigator().setUpAdapter(list, obj)
            currencyType.set(obj.currency)
            totalAmount.set(obj.currency + obj.totalAmount.toString())
        } else if (apiCall == 1) {
            val responseData = Gson().fromJson(
                Gson().toJson(response.data), BaseResponse.DataObjectsAllApi::class.java
            )
//            getNavigator().openPaymentGateway(responseData)

        } else {
            amountToBeAdded.set("")
            getNavigator().showToast("Amount added successfully")
            getListApi()
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showMessage(e.exception!!)
    }

    fun getListApi() {
        apiCall = 0
        getWalletListApiBase()
    }

    /* Getting oder id from backend to open payment gateway */
    private fun getOrderIdAPi() {
//        apiCall = 1
//        map[Config.amount] = "" + (amountToBeAdded.get()?.toInt())!! * 100
//        map[Config.currency] = "INR"
//        orderApi(map)
//        map[Config.purpose] = "walletRecharge"
        // addWalletAmountBase(map)
    }

    fun setAmount(amount: Int) {
        if (amount == 1)
            amountToBeAdded.set(predefinedAmount1)
        else
            amountToBeAdded.set(predefinedAmount2)
    }

    fun onClickAdd() {
        if (amountToBeAdded.get()!!.isEmpty() || amountToBeAdded.get()!!.toDouble() <= 0.0) {
            getNavigator().showMessage("Kindly add a valid amount")
        } else
            getOrderIdAPi()
    }

}