package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class TypesModel {
    @SerializedName("zone_name")
    @Expose
     val zoneName: String? = null

    @SerializedName("country_name")
    @Expose
     val countryName: String? = null

    @SerializedName("currency_symble")
    @Expose
     val currencySymble: String? = null

    @SerializedName("zone_slug")
    @Expose
     val zoneSlug: String? = null

    @SerializedName("payment_types")
    @Expose
     val paymentTypes: List<String>? = null

    @SerializedName("unit")
    @Expose
     val unit: String? = null

    @SerializedName("country_id")
    @Expose
     val countryId: String? = null

    @SerializedName("zone_type_price")
    @Expose
     val zoneTypePrice: List<ZoneTypePrice>? = null





     class ZoneTypePrice {
         @SerializedName("type_name")
         @Expose
          val typeName: String? = null

         @SerializedName("type_slug")
         @Expose
          val typeSlug: String? = null

         @SerializedName("capacity")
         @Expose
          val capacity: String? = null

         @SerializedName("type_image")
         @Expose
          val typeImage: String? = null

         @SerializedName("type_image_select")
         @Expose
          val typeImageSelect: String? = null

         @SerializedName("promo_total_amount")
         @Expose
         val promoTotalAmount: String? = null

         @SerializedName("promo_amount")
         @Expose
         val promoAmount: String? = null

         @SerializedName("promo_code")
         @Expose
          val promoCode: Int? = null

         @SerializedName("out_of_zone_price")
         @Expose
         val outZonePrice:String? = null

         @SerializedName("base_price")
         @Expose
          val basePrice: String? = null

         @SerializedName("distance")
         @Expose
          val distance: Double? = null

         @SerializedName("total_amount")
         @Expose
          val totalAmount: String? = null

         @SerializedName("free_waiting_time")
         @Expose
          val freeWaitingTime: String? = null

         @SerializedName("waiting_charge")
         @Expose
          val waitingCharge: String? = null

         @SerializedName("price_per_time")
         @Expose
          val pricePerTime: String? = null

         @SerializedName("base_distance")
         @Expose
          val baseDistance: String? = null

         @SerializedName("booking_fees")
         @Expose
         val bookingFees: String? = null

         @SerializedName("computed_price")
         @Expose
         val computedPrice: String? = null

         @SerializedName("computed_distance")
         @Expose
         val computedDistance : Double? = null

         @SerializedName("price_per_distance")
         @Expose
          val pricePerDistance: String? = null

         var isselected: Boolean? = null

         var eta : String? = "N/A"

     }
}
