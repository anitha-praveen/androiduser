package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ComplaintHistoryModel {
    @SerializedName("complaints")
    @Expose
    val complaints: List<Complaints>? = null

    class Complaints {
        @SerializedName("answer")
        @Expose
        val answer: String? = null

        @SerializedName("complaint_type")
        @Expose
        val complaintType: Int? = null

        @SerializedName("title")
        @Expose
        val title: String? = null

        @SerializedName("date")
        @Expose
        val date: String? = null
    }
}