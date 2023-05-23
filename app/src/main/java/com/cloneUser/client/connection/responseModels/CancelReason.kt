package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class CancelReason : Serializable{
    @SerializedName("reasons")
    @Expose
    val reasons: MutableList<Reason>? = null

    class Reason : Serializable{
        @SerializedName("id")
        @Expose
        val id: Int? = null

        @SerializedName("reason")
        @Expose
        val reason: String? = null

        @SerializedName("user_type")
        @Expose
        val userType: String? = null

        @SerializedName("trip_status")
        @Expose
        val tripStatus: String? = null

        @SerializedName("pay_status")
        @Expose
        val payStatus: Int? = null

        @SerializedName("active")
        @Expose
        val active: Int? = null

        var isselected: Boolean? = null

    }
}