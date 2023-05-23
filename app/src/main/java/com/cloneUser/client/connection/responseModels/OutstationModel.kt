package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class OutstationModel: Serializable {

    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("pick_up")
    @Expose
    var pickUp: String? = null
    @SerializedName("pick_lat")
    @Expose
    var pickLat: Double? = null
    @SerializedName("pick_lng")
    @Expose
    var pickLng: Double? = null
    @SerializedName("drop")
    @Expose
    var drop: String? = null
    @SerializedName("drop_lat")
    @Expose
    var dropLat: Double? = null
    @SerializedName("drop_lng")
    @Expose
    var dropLng: Double? = null
    @SerializedName("country")
    @Expose
    var country: String? = null
    @SerializedName("distance")
    @Expose
    var distance: String? = null
    @SerializedName("price")
    @Expose
    var price: String? = null
    @SerializedName("status")
    @Expose
    var status: String? = null
    @SerializedName("hill_station")
    @Expose
    var hillStation: String? = null

}