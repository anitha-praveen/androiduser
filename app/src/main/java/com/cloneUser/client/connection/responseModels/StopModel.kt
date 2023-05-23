package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.SerializedName

//class StopModel {
//    @Expose
//    var place_id: String? = null
//
//    @Expose
//    var address: String? = null
//
//    @Expose
//    var latitude: Double? = null
//
//    @Expose
//    var longitude: Double? = null
//
//    @Expose
//    var stopsCount: Int? = null
//}

data class StopModel(
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val lat: Double,
    @SerializedName("longitude") val lng: Double,
    @SerializedName("stopsCount") val count: Int,
)