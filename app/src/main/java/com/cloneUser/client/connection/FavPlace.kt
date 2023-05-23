package com.cloneUser.client.connection

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FavPlace {

    @SerializedName("FavouriteList")
    @Expose
    val favouriteList: List<Favourite>? = null

    @SerializedName("Last_trip_history")
    @Expose
    val lastTripHistory: List<LastTripHistory>? = null

    class Favourite {
        @SerializedName("id")
        @Expose
        val id: Int? = null

        @SerializedName("user_id")
        @Expose
        val userId: Int? = null

        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("latitude")
        @Expose
        val latitude: String? = null

        @SerializedName("longitude")
        @Expose
        val longitude: String? = null

        @SerializedName("address")
        @Expose
        var address: String? = null

        @SerializedName("status")
        @Expose
        val status: Int? = null

        @SerializedName("slug")
        @Expose
        val slug: String? = null

        @SerializedName("place_id")
        var placeId : String? = null

    }


    class LastTripHistory {
        @SerializedName("pick_lat")
        @Expose
        val pickLat: Double? = null

        @SerializedName("pick_lng")
        @Expose
        val pickLng: Double? = null

        @SerializedName("drop_lat")
        @Expose
        val dropLat: Double? = null

        @SerializedName("drop_lng")
        @Expose
        val dropLng: Double? = null

        @SerializedName("pick_address")
        @Expose
        val pickAddress: String? = null

        @SerializedName("drop_address")
        @Expose
        val dropAddress: String? = null

    }
}