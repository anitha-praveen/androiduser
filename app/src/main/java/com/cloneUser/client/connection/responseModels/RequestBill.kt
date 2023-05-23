package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class RequestBill : Serializable {
    @SerializedName("data")
    @Expose
     val data: Bill? = null

    class Bill : Serializable {
        @SerializedName("id")
        @Expose
         val id: Int? = null

        @SerializedName("base_price")
        @Expose
         val basePrice: Double? = null

        @SerializedName("base_distance")
        @Expose
         val baseDistance: Double? = null

        @SerializedName("price_per_distance")
        @Expose
         val pricePerDistance: Double? = null

        @SerializedName("distance_price")
        @Expose
         val distancePrice: Double? = null

        @SerializedName("price_per_time")
        @Expose
         val pricePerTime: Double? = null

        @SerializedName("time_price")
        @Expose
         val timePrice: Double? = null

        @SerializedName("waiting_charge")
        @Expose
         val waitingCharge: Double? = null

        @SerializedName("cancellation_fee")
        @Expose
         val cancellationFee: Double? = null

        @SerializedName("admin_commision")
        @Expose
         val adminCommision: Double? = null

        @SerializedName("driver_commision")
        @Expose
         val driverCommision: Double? = null

        @SerializedName("total_amount")
        @Expose
         val totalAmount: Double? = null

        @SerializedName("total_distance")
        @Expose
         val totalDistance: Double? = null

        @SerializedName("total_time")
        @Expose
         val totalTime: Double? = null

        @SerializedName("promo_discount")
        @Expose
         val promoDiscount: Double? = null

        @SerializedName("requested_currency_code")
        @Expose
         val requestedCurrencyCode: String? = null

        @SerializedName("requested_currency_symbol")
        @Expose
         val requestedCurrencySymbol: String? = null

        @SerializedName("out_of_zone_price")
        @Expose
        val outZonePrice:Double? = null

        @SerializedName("booking_fees")
        @Expose
        val bookingFees:Double? = null

        @SerializedName("service_tax")
        @Expose
        val serviceTax:Double? = null

        @SerializedName("hill_station_price")
        @Expose
        val hillStationPrice: Double? = null

        @SerializedName("pending_km")
        @Expose
        val pendingKm: String? = null

        @SerializedName("package_hours")
        @Expose
        val packageHours: String? = null

        @SerializedName("package_km")
        @Expose
        val packageKm: String? = null
    }
}