package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SuggestionModel {

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("category")
    @Expose
    var category: String? = null

    @SerializedName("type")
    var type: String? = null

    @SerializedName("status")
    var status: Int? = null

    @SerializedName("created_at")
    var createdAt: String? = null

    @SerializedName("updated_at")
    var updatedAt: String? = null

    @SerializedName("deleted_at")
    var deletedAt: String? = null

    @SerializedName("slug")
    var slug: String? = null

    var isSelected: Boolean = false

}