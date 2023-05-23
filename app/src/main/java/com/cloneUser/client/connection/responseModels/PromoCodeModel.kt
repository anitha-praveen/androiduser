package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class PromoCodeModel {
    @SerializedName("promocode")
    @Expose
    val promocode: List<Promocode>? = null

    @SerializedName("user")
    @Expose
    val user: UserModel.User? = null


    class Promocode {
        @SerializedName("id")
        @Expose
        val id: Int? = null

        @SerializedName("zone_id")
        @Expose
        val zoneId: Int? = null

        @SerializedName("promo_code")
        @Expose
        val promoCode: String? = null

        @SerializedName("select_offer_option")
        @Expose
        val selectOfferOption: String? = null

        @SerializedName("promo_icon")
        @Expose
        val promoIcon: String? = null

        @SerializedName("description")
        @Expose
        val description: String? = null

        @SerializedName("promo_offer_no_of_ride")
        @Expose
        val promoOfferNoOfRide: String? = null

        @SerializedName("target_amount")
        @Expose
        val targetAmount: String? = null

        @SerializedName("promo_type")
        @Expose
        var promoType: Int? = null

        @SerializedName("amount")
        @Expose
        var amount: String? = null

        @SerializedName("percentage")
        @Expose
        var percentage: String? = null

        @SerializedName("user_id")
        @Expose
        val userId: Any? = null

        @SerializedName("distance_km")
        @Expose
        val distanceKm: Int? = null

        @SerializedName("from_date")
        @Expose
        val fromDate: String? = null

        @SerializedName("to_date")
        @Expose
        val toDate: String? = null

        @SerializedName("slug")
        @Expose
        val slug: String? = null

        @SerializedName("status")
        @Expose
        val status: Int? = null

        @SerializedName("select_offer_option_title")
        @Expose
        val selectOfferOptionTitle: String? = null

        @SerializedName("country_symbol")
        @Expose
        val countrySymbol: String? = null

    }

}

