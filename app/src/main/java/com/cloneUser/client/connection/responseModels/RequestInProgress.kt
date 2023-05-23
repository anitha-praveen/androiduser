package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

import com.cloneUser.client.connection.responseModels.UserModel.User
import java.io.Serializable


class RequestInProgress : Serializable{
    @SerializedName("user")
    @Expose
    val user: User? = null

    @SerializedName("trips")
    @Expose
    val trips: Trips? = null

    class Trips : Serializable{
        @SerializedName("data")
        @Expose
        val data: RequestData.Data? = null
    }
}