package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import com.cloneUser.client.connection.FavPlace
import java.io.Serializable


class UserModel {

    @SerializedName("user")
    @Expose
    val user: User? = null

    class User : Serializable{

        @SerializedName("slug")
        @Expose
        val slug: String? = null

        @SerializedName("firstname")
        @Expose
        val firstname: String? = null

        @SerializedName("lastname")
        @Expose
        val lastname: String? = null

        @SerializedName("email")
        @Expose
        val email: String? = null

        @SerializedName("phone_number")
        @Expose
        val phoneNumber: String? = null

        @SerializedName("currency")
        @Expose
        val currency: String? = null

        @SerializedName("country")
        @Expose
        val country: String? = null

        @SerializedName("profile_pic")
        @Expose
        val profile_pic: String? = null

        @SerializedName("amount")
        @Expose
        val amount: String? = null

        @SerializedName("promo_type")
        @Expose
        val promoType: Int? = null

        @SerializedName("percentage")
        @Expose
        val percentage: String? = null

        @SerializedName("search_radius")
        @Expose
        val searchRadius: String? = null

    }
}