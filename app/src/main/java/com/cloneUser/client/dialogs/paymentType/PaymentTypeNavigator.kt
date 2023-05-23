package com.cloneUser.client.dialogs.paymentType

import com.cloneUser.client.connection.TranslationModel

interface PaymentTypeNavigator  {
    fun setPaymentMethod(payMethod : String)
    fun sendPaymentType()
    fun getTranslation() : TranslationModel
    fun showMessage(message : String)
}