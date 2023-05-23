package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class PackageModel {

    @SerializedName("data")
    @Expose
    val packageList: List<Package>? = null

    class Package{
        @SerializedName("id")
        @Expose
        val id: Int? = null

        @SerializedName("name")
        @Expose
        val name: String? = null

        @SerializedName("hours")
        @Expose
        val hours: String? = null

        @SerializedName("time_cost_type")
        @Expose
        val timeCostType: String? = null

        @SerializedName("km")
        @Expose
        val km: String? = null

        @SerializedName("slug")
        @Expose
        val slug: String? = null

        var isSelected = false
    }

}