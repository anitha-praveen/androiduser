package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class SuggestionHistoryModel {
    @SerializedName("suggestion")
    @Expose
    val suggestion: List<Suggestion>? = null

    class Suggestion{
        @SerializedName("answer")
        @Expose
         val answer: String? = null

        @SerializedName("suggestion_type")
        @Expose
         val suggestionType: Int? = null

        @SerializedName("title")
        @Expose
         val title: String? = null
        
        @SerializedName("date")
        @Expose
         val date: String? = null
    }
}