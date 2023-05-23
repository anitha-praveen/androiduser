package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class ReferralModel {
    @SerializedName("referral")
    @Expose
    val referral: List<Any>? = null

    @SerializedName("referral_code")
    @Expose
    val referralCode: String? = null

    @SerializedName("referral_amount")
    @Expose
    val referralAmount: Double? = null

    @SerializedName("refer_by_driver_amount")
    @Expose
    val referByDriverAmount: Double? = null

    @SerializedName("refer_by_user_amount")
    @Expose
    val referByUserAmount: Double? = null

    @SerializedName("currency_symbol")
    @Expose
    val currencySymbol: String? = null

}