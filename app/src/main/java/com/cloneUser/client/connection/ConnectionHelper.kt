package com.cloneUser.client.connection

import com.cloneUser.client.ut.Config
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Interface used to do all the apis
 * using the endpoints from base url
 * */
interface ConnectionHelper {


    @FormUrlEncoded
    @POST(Config.LANGUAGE_CODE_API)
    fun getAvailableLanguageApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>


    @GET(Config.LANGUAGE_CODE_API + "/{" + Config.Code + "}")
    fun getSelectedLangBaseApi(
        @Path(Config.Code) requestId: String?
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.SELECTED_LANGUAGE)
    fun getPostSelectedLanguageBase(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.SIGN_IN_API)
    fun signInBaseApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @Multipart
    @POST(Config.SIGN_UP_API)
    fun signUpBaseApi(
        @HeaderMap headerMap: Map<String, String>,
        @PartMap() partMap: Map<String, @JvmSuppressWildcards RequestBody>
    ): Call<BaseResponse>

    @Multipart
    @POST(Config.SIGN_UP_API)
    fun signUpBaseWithImageApi(
        @HeaderMap headerMap: Map<String, String>,
        @PartMap() partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part
    ): Call<BaseResponse>


    @FormUrlEncoded
    @POST(Config.CUSTOM_OTP_URL)
    fun sendCustomOtp(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>


    @FormUrlEncoded
    @POST(Config.TOKEN_API)
    fun getTokenBaseApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>


    @GET(Config.PROFILE_API)
    fun getProfileBaseApi(
        @HeaderMap headerMap: Map<String, String>
    ): Call<BaseResponse>

    @Multipart
    @POST(Config.PROFILE_API)
    fun updateProfileApi(
        @HeaderMap headerMap: Map<String, String>,
        @PartMap() partMap: Map<String, @JvmSuppressWildcards RequestBody>
    ): Call<BaseResponse>

    @Multipart
    @POST(Config.PROFILE_API)
    fun updateProfileImageApi(
        @HeaderMap headerMap: Map<String, String>,
        @Part file: MultipartBody.Part
    ): Call<BaseResponse>


    @FormUrlEncoded
    @POST(Config.CHECK_PHONE_NUMBER_AVAILABLE)
    fun checkPhoneNumberApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @GET(Config.FAVORITE_PLACES)
    fun getFavoritePlaceApi(
        @HeaderMap headerMap: Map<String, String>
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.FAVORITE_PLACES)
    fun saveFavoritePlaceApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @POST(Config.DELETE_FAV_PLACES)
    fun deleteFavoriteApi(
        @HeaderMap headerMap: Map<String, String>,
        @Path("slug") inputId: String
    ): Call<BaseResponse>


    @FormUrlEncoded
    @POST(Config.GET_TYPES)
    fun getVehicleTypesApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @GET(Config.GET_PROMO_LIST)
    fun getPromoListApi(@HeaderMap headerMap: Map<String, String> ,  @Query("type") type: String?): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.CREATE_REQUEST)
    fun createRequestApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.APPLY_PROMO)
    fun applyPromoApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>


    @GET(Config.GET_SOS)
    fun getSOSListApi(@HeaderMap headerMap: Map<String, String>): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.CANCEL_REQUEST)
    fun cancelRequestApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @GET(Config.REQUEST_IN_PROGRESS)
    fun requestInProgressApi(@HeaderMap headerMap: Map<String, String>): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.CANCEL_REASONS)
    fun getCancelReasonsApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.RATE)
    fun rateDriverApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.HISTORY_LIST)
    fun getHistoryListApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>,
        @Query("page") pageNo: String
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.SAVE_SOS)
    fun saveSosApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @DELETE(Config.DELETE_SOS)
    fun deleteSosApi(
        @HeaderMap headerMap: Map<String, String>,
        @Path("id") inputId: String
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.CHANGE_LOCATION)
    fun changeLocationApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @GET(Config.SUGGESTION_lIST)
    fun getSuggestionListApi(@HeaderMap headerMap: Map<String, String>): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.ADD_COMPLAINTS)
    fun submitSuggestionApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @GET(Config.COMPLAINTS_lIST)
    fun getComplaintsListApi(@HeaderMap headerMap: Map<String, String>): Call<BaseResponse>

    @GET(Config.DISPUTE)
    fun getTripComplaintsListApi(@HeaderMap headerMap: Map<String, String>): Call<BaseResponse>


    @GET(Config.LOG_OUT_URL)
    fun getLogOutApi(@HeaderMap headerMap: Map<String, String>): Call<BaseResponse>

    @GET(Config.FAQ_URL)
    fun getFaqApi(@HeaderMap headerMap: Map<String, String>): Call<BaseResponse>

    @GET(Config.WALLET_LIST_URL)
    fun getWalletListApi(@HeaderMap headerMap: Map<String, String>): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.WALLET_ADD_AMOUNT)
    fun getWalletAddAmount(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>,
    ): Call<BaseResponse>

    @GET(Config.NOTIFIACTION_URL)
    fun getNotificationApi(@HeaderMap headerMap: Map<String, String>): Call<BaseResponse>

    @GET(Config.NOTIFIACTION_URL)
    fun getNotificationApi(
        @HeaderMap headerMap: Map<String, String>,
        @Query("page") pageNo: String
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.CHECKZONE_URL)
    fun getCheckZone(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>,
    ): Call<BaseResponse>


    @GET(Config.REFERRAL_DETAILS)
    fun getReferralDetailsApi(
        @HeaderMap headerMap: Map<String, String>,
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.ERRORLOG)
    fun updateErrorApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>,
    ): Call<BaseResponse>

    @GET(Config.OUTSTATION_URL)
    fun getOutStationapi(
        @HeaderMap headerMap: Map<String, String>,
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.OUTSTATION_TYPES)
    fun getOutStationTypesApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @GET(Config.RENTAL_PACKAGE_LIST)
    fun getRentalPackageListApi(
        @HeaderMap headerMap: Map<String, String>,
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.SELECTED_PACKAGE_DETAILS)
    fun getSelectedPackageDetailsApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap fieldMap: Map<String,String>
    ): Call<BaseResponse>

    @GET(Config.GET_ADMIN_NUMBER)
    fun getAdminContactApi(
        @HeaderMap headerMap: Map<String, String>,
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.CHEKCOUTSTATION)
    fun getcheckOutstationApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap fieldMap: Map<String,String>
    ): Call<BaseResponse>

    @Multipart
    @POST(Config.NIGHT_IMAGE_UPLOAD)
    fun updateNightImage(
        @HeaderMap headerMap: Map<String, String>,
        @PartMap() partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part
    ): Call<BaseResponse>

    @GET(Config.INVOICE_QUESTIONS)
    fun getRatingQuestions(
        @HeaderMap headerMap: Map<String, String>,
    ): Call<BaseResponse>

    @FormUrlEncoded
    @POST(Config.RETAKE_NIGHT)
    fun requestRetakeApi(
        @HeaderMap headerMap: Map<String, String>,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<BaseResponse>

    @GET(Config.COMPLAINTS_HISTORY)
    fun getComplaintsHistory(
        @HeaderMap headerMap: Map<String, String>
    ):Call<BaseResponse>

    @GET(Config.SUGGESTION_HISTORY)
    fun getSuggestionHistory(
        @HeaderMap headerMap: Map<String, String>
    ):Call<BaseResponse>

}