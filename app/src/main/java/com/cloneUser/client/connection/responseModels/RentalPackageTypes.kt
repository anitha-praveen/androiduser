package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class RentalPackageTypes {
    @SerializedName("data")
    @Expose
    val rentalTypes: List<Type>? = null

    class   Type {
        @Expose
        val id: Int? = null

        @SerializedName("package_id")
        @Expose
        val packageId: Int? = null

        @SerializedName("type_id")
        @Expose
        val typeId: Int? = null

        @SerializedName("price")
        @Expose
        val price: Double? = null

        @SerializedName("slug")
        @Expose
        val slug: String? = null

        @SerializedName("status")
        @Expose
        val status: Int? = null

        @SerializedName("total_amount")
        @Expose
        val totalAmount: Double? = null

        @SerializedName("promo_code")
        @Expose
        val promoCode: Int? = null

        @SerializedName("total_amount_promo")
        @Expose
        val totalAmountPromo: String? = null


        @SerializedName("currency_symbol")
        @Expose
        val currencySymbol: String? = null

        @SerializedName("get_vehicle")
        @Expose
        val vehicle: Vehicle? = null

        var isSelected : Boolean = false
        var eta : String? = "N/A"


        class Vehicle {
            @SerializedName("id")
            @Expose
            val id: Int? = null

            @SerializedName("vehicle_name")
            @Expose
            val vehicleName: String? = null

            @SerializedName("highlight_image")
            @Expose
            val highlightImage: String? = null

            @SerializedName("image")
            @Expose
            val image: String? = null

            @SerializedName("capacity")
            @Expose
            val capacity: String? = null

            @SerializedName("slug")
            @Expose
            val slug: String? = null
        }
    }
}