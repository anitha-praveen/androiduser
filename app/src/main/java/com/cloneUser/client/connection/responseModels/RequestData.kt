package com.cloneUser.client.connection.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.cloneUser.client.connection.responseModels.UserModel.User
import java.io.Serializable


class RequestData : Serializable {

    @SerializedName("data")
    @Expose
    var data: Data? = null

    @SerializedName("location_type")
    @Expose
    val loc_type: Int? = null

    @SerializedName("upload_status")
    @Expose
    val uploadStatus: Boolean? = null

    @SerializedName("upload_image_url")
    @Expose
    val uploadImageUrl: String? = null

    @SerializedName("retake_image")
    @Expose
    val retake_image: Boolean? = null

    @SerializedName("trip_start_km")
    @Expose
    val tripStartKm: String? = null

    @SerializedName("trip_km")
    @Expose
    val trip_km: String? = null


    open class Data : Serializable {
        @SerializedName("id")
        @Expose
        val id: String? = null

        @SerializedName("request_number")
        @Expose
        val requestNumber: String? = null

        @SerializedName("request_otp")
        @Expose
        val requestOtp: Int? = null


        @SerializedName("is_later")
        @Expose
        val isLater: Int? = null

        @SerializedName("user_id")
        @Expose
        val userId: Int? = null

        @SerializedName("promo_applied")
        @Expose
        val promo_applied: Boolean? = null

        @SerializedName("if_dispatch")
        @Expose
        val ifDispatch: Int? = null

        @SerializedName("trip_start_time")
        @Expose
        val tripStartTime: String? = null

        @SerializedName("outstation_trip_type")
        @Expose
        val oustationTripType: String? = null

        @SerializedName("completed_at")
        @Expose
        val completedAt: String? = null

        @SerializedName("is_driver_started")
        @Expose
        val isDriverStarted: Int? = null

        @SerializedName("is_driver_arrived")
        @Expose
        val isDriverArrived: Int? = null

        @SerializedName("is_trip_start")
        @Expose
        val isTripStart: Int? = null

        @SerializedName("share_path")
        @Expose
        val tripShareLink: String? = null

        @SerializedName("total_distance")
        @Expose
        val totalDistance: String? = null

        @SerializedName("total_time")
        @Expose
        val totalTime: String? = null

        @SerializedName("is_completed")
        @Expose
        val isCompleted: Int? = null

        @SerializedName("is_cancelled")
        @Expose
        val isCancelled: Int? = null

        @SerializedName("cancel_method")
        @Expose
        val cancelMethod: String? = null

        @SerializedName("custom_reason")
        @Expose
        val customReason: String? = null

        @SerializedName("payment_opt")
        @Expose
        val paymentOpt: String? = null

        @SerializedName("is_paid")
        @Expose
        val isPaid: Int? = null

        @SerializedName("user_rated")
        @Expose
        val userRated: Int? = null

        @SerializedName("driver_rated")
        @Expose
        val driverRated: Int? = null

        @SerializedName("unit")
        @Expose
        val unit: String? = null

        @SerializedName("zone_type_id")
        @Expose
        val zoneTypeId: Int? = null

        @SerializedName("pick_lat")
        @Expose
        val pickLat: Double? = null

        @SerializedName("pick_lng")
        @Expose
        val pickLng: Double? = null

        @SerializedName("drop_lat")
        @Expose
        val dropLat: Double? = null

        @SerializedName("drop_lng")
        @Expose
        val dropLng: Double? = null

        @SerializedName("pick_address")
        @Expose
        val pickAddress: String? = null

        @SerializedName("drop_address")
        @Expose
        val dropAddress: String? = null

        @SerializedName("stops")
        @Expose
        val stops: Stops? = null

        @SerializedName("requested_currency_code")
        @Expose
        val requestedCurrencyCode: String? = null

        @SerializedName("requested_currency_symbol")
        @Expose
        val requestedCurrencySymbol: String? = null

        @SerializedName("is_instant_trip")
        @Expose
        val isInstantTrip: Int? = null

        @SerializedName("driver_overall_rating")
        @Expose
        val driverOverallRating: Double? = null

        @SerializedName("dispute_status")
        @Expose
        val disputeStatus: Int? = null

        @SerializedName("location_approve")
        @Expose
        val locationApprove: Int? = null

        @SerializedName("poly_string")
        @Expose
        val polyString: String? = null

        @SerializedName("service_category")
        @Expose
        val serviceCategory: String? = null

        @SerializedName("user")
        @Expose
        val user: User? = null

        @SerializedName("driver")
        @Expose
        val driver: Driver? = null

        @SerializedName("car_details")
        @Expose
        val carDetails: CarDetails? = null

        @SerializedName("vehicle_name")
        @Expose
        val vehical_type: String? = null

        @SerializedName("vehicle_image")
        @Expose
        val vehicleTypeImg: String? = null

        @SerializedName("package_hour")
        @Expose
        val packageHour: String? = null

        @SerializedName("package_km")
        @Expose
        val packageKm: String? = null

        @SerializedName("start_km")
        @Expose
        val startKM: String? = null

        @SerializedName("end_km")
        @Expose
        val endKM: String? = null

        @SerializedName("user_upload_image")
        @Expose
        val userUploadImage : Boolean? = null

        @SerializedName("driver_upload_image")
        @Expose
        val driverUploadImage : Boolean? = null

        @SerializedName("skip_night_photo")
        @Expose
        val skipNightPhoto : Boolean? = null

        @SerializedName("start_night_time")
        @Expose
        val startNightTime : String? = null

        @SerializedName("end_night_time")
        @Expose
        val endNightTime : String? = null

        @SerializedName("trip_start_km_image")
        @Expose
        val tripStartKmImage : String? = null

        @SerializedName("night_photo_driver")
        @Expose
        val nightPhotoDriver : String? = null

        @SerializedName("requestBill")
        @Expose
        val requestBill: RequestBill? = null

        class Driver : Serializable {
            @SerializedName("id")
            @Expose
            val id: Int? = null

            @SerializedName("slug")
            @Expose
            val slug: String? = null

            @SerializedName("firstname")
            @Expose
            val firstname: String? = null

            @SerializedName("lastname")
            @Expose
            val lastname: String? = null

            @SerializedName("email")
            @Expose
            val email: String? = null

            @SerializedName("phone_number")
            @Expose
            val phoneNumber: String? = null

            @SerializedName("avatar")
            @Expose
            val avatar: Any? = null

            @SerializedName("profile_pic")
            @Expose
            val profilePic: String? = null

            @SerializedName("referral_code")
            @Expose
            val referralCode: String? = null
        }

        class CarDetails : Serializable {

            @SerializedName("type")
            @Expose
            val type: Int? = null

            @SerializedName("car_number")
            @Expose
            val carNumber: String? = null

            @SerializedName("car_model")
            @Expose
            val carModel: String? = null

        }

        @SerializedName("vehicle_highlight_image")
        @Expose
        val vehicleHighlighted: String? = null

        @SerializedName("vehicle_model")
        @Expose
        val vehicleModel: String? = null

        @SerializedName("vehicle_number")
        @Expose
        val vehicleNumber: String? = null
    }

}

data class Stops(
    @SerializedName("address")
    @Expose
    val address: String? = null,

    @SerializedName("latitude")
    @Expose
    val latitude: String? = null,

    @SerializedName("longitude")
    @Expose
    val longitude: String? = null,

    )