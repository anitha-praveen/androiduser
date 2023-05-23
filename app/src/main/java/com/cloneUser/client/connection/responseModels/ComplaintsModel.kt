package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ComplaintsModel(

    @SerializedName("id")
    @Expose
    var id: Int? = null,
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("category")
    var category: String? = null,
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("status")
    var status: Int? = null,
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("updated_at")
    var updatedAt: String? = null,
    @SerializedName("deleted_at")
    var deletedAt: String? = null,
    @SerializedName("slug")
    var slug: String? = null,
    @SerializedName("complaint_type")
    var complaintType: Int? = null,

    var isSelected : Boolean = false

)