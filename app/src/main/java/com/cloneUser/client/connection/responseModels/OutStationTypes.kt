package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.SerializedName

data class OutStationTypes(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("type_id") var typeId: Int? = null,
    @SerializedName("distance_price") var distancePrice: Double? = null,
    @SerializedName("distance_price_two_way") var distancePriceTwoWay: Double? = null,
    @SerializedName("base_fare") var baseFare: Double? = null,
    @SerializedName("day_rent_two_way") var twoWaydriverBeta:Double? = null,
    @SerializedName("admin_commission") var adminCommission: Double? = null,
    @SerializedName("admin_commission_type") var adminCommissionType: Double? = null,
    @SerializedName("driver_price") var driverPrice: Double? = null,
    @SerializedName("grace_time") var graceTime: String? = null,
    @SerializedName("waiting_charge") var waitingCharge: Double? = null,
    @SerializedName("status") var status: Int? = null,
    @SerializedName("total_amount") var totalAmount: Double? = null,
    @SerializedName("distance") var distance: Double? = null,
    @SerializedName("currency_symbol") var currencySymbol: String? = null,
    @SerializedName("promo_amount") var promoAmount: String? = null,
    @SerializedName("total_amount_promo") var totalAmountPromo: String? = null,
    @SerializedName("promo_code") var promoCode: Double? = null,
    @SerializedName("get_vehicle") var getVehicle: GetVehicle? = GetVehicle(),
    @SerializedName("hill_station_price") var hillStationPrice : Double? = null,
    var isSelected: Boolean? = null
)

data class GetVehicle(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("vehicle_name") var vehicleName: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("highlight_image") var highlightImage: String? = null,
    @SerializedName("capacity") var capacity: String? = null,
    @SerializedName("service_type") var serviceType: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("status") var status: Int? = null,
    @SerializedName("deleted_at") var deletedAt: String? = null,
    @SerializedName("slug") var slug: String? = null,
    @SerializedName("category_id") var categoryId: Int? = null,
    @SerializedName("sorting_order") var sortingOrder: String? = null,
    @SerializedName("service_types") var serviceTypes: ArrayList<String> = arrayListOf()

)
