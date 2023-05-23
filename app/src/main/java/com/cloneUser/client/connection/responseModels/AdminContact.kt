package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AdminContact {
    @SerializedName("data")
    var data: Data? = null

    class Data{
        @SerializedName("customer_care_number")
        @Expose
        var customerCareNumber: String? = null

    }

}