package com.cloneUser.client.connection

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.cloneUser.client.connection.responseModels.*
import com.cloneUser.client.ut.SessionMaintainence
import org.json.JSONException
import org.json.JSONObject


/**
 * Base/Common response objects which will be in all API response
 * are used here
 * */
class BaseResponse {

    @Expose
    var success: Any? = null

    @Expose
    var success_message: String? = null

    @SerializedName("notification_enum")
    @Expose
    var notificationEnum: String? = null


    @Expose
    var error: Any? = null

    @Expose
    var data: Any? = null

    @SerializedName("current_page")
    @Expose
    var currentPage: String? = null


    @SerializedName("token_type")
    @Expose
    var tokenType: String? = null

    @SerializedName("access_token")
    @Expose
    var accessToken: String? = null


    @SerializedName("errors")
    var errors: Map<String, List<String>>? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("result")
    @Expose
    var result: RequestData? = null

    @SerializedName("lng")
    @Expose
    var lng: Double? = null

    @SerializedName("lat")
    @Expose
    var lat: Double? = null

    @SerializedName("waiting_time")
    @Expose
    var waitingTime: String? = null

    @SerializedName("rental_trip_time")
    @Expose
    var rentalTripTime: String? = null

    @SerializedName("distancee")
    @Expose
    val totalDistancee: String? = null

    @SerializedName("bearing")
    @Expose
    var bearing: Float? = null

    @SerializedName("trip_start")
    @Expose
    var trip_start: Int? = null

    @SerializedName("suggestion")
    @Expose
    var suggestion: ArrayList<SuggestionModel>? = null

    @SerializedName("complaint")
    @Expose
    var complaint: ArrayList<ComplaintsModel>? = null

    @SerializedName("faq")
    @Expose
    var faq: ArrayList<FaqModel>? = null


    fun saveLanguageTranslations(
        sessionMaintainence: SessionMaintainence,
        gson: Gson,
        dataObject: Any
    ) {
        val map: JSONObject?
        val languages: MutableList<String> = ArrayList()
        try {
            map = JSONObject(gson.toJson(dataObject))
            val iterator = map.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                sessionMaintainence.saveString(key, map[key].toString())
                languages.add(key)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        sessionMaintainence.saveString(SessionMaintainence.TRANSLATED_DATA, gson.toJson(languages))
    }

    class DataObjectsAllApi {
        /**
         * its for wallet response
         */

        @SerializedName("total_amount")
        var totalAmount: Int? = null

        @SerializedName("currency")
        var currency: String? = null

        @SerializedName("wallet_transaction")
        var walletTransaction: java.util.ArrayList<WalletResponsModel>? = arrayListOf()

        @SerializedName("zone")
        var zone: Boolean? = null
        @SerializedName("outstation")
        var outstation: Boolean? = null

        @SerializedName("request_id")
        var requestId: String? = null

        @SerializedName("error_code")
        var errorCode: Int? = null

        @SerializedName("data")
        var outStationModel: ArrayList<OutstationModel> = arrayListOf()

        //to get Questions
        @SerializedName("invoice_questions_list")
        var invoiceQuestionsList: ArrayList<InvoiceQuestionsList> = arrayListOf()

    }

    class NewUser {
        @SerializedName("new_user")
        @Expose
        var newUser: Boolean? = null

        @SerializedName("client_id")
        @Expose
        var clientId: String? = null

        @SerializedName("client_secret")
        @Expose
        var clientSecret: String? = null

        @SerializedName("error_code")
        @Expose
        var errorCode: Int? = null
    }
    class BaseOutstationTypes {
        @SerializedName("data")
        var outStationTypes: ArrayList<OutStationTypes> = arrayListOf()
    }
    data class InvoiceQuestionsList(

        @SerializedName("id") var id: Int? = null,
        @SerializedName("questions") var questions: String? = null,
        @SerializedName("status") var status: Int? = null,
        @SerializedName("created_at") var createdAt: String? = null,
        @SerializedName("updated_at") var updatedAt: String? = null,
        @SerializedName("slug") var slug: String? = null,
        var isSelected:Boolean = true

    )

}
