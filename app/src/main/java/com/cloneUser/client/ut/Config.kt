package com.cloneUser.client.ut

import android.Manifest
import android.os.Build

class Config {
    companion object {

        val error: String = "error"
        val stops: String = "stops"


        /**
         * Common values
         */
        const val CURRENT_COUNTRY_SHORT_CODE = "IN"


        val CodeValue: String = "V-b6ea1128db9cf21e2808dfc1d2f02ae5"         /* development */
        var Array_permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        var storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val sharedPrefName = "secret_shared_prefs_roda_clone_user"


        /**
         * Permission codes
         */
        const val REQUEST_PERMISSION = 9000
        const val CHANGE_LOCATION = "request/change-location"

        /**
         * Api URLS
         */

        const val BASEURL = "http://3.216.234.12/roda/public/api/V1/"
        const val SOCKET_URL = "http://3.216.234.12:5001"
        const val GooglBaseURL = "https://maps.googleapis.com/"
        const val DirectionURL = "maps/api/directions/json?"
        const val LANGUAGE_CODE_API = "languages"
        const val language_selected = "Language Selected"
        const val SIGN_IN_API = "user/signin"
        const val SIGN_UP_API = "user/signup"
        const val CUSTOM_OTP_URL = "user/sendotp"
        const val TOKEN_API = "auth/token"
        const val PROFILE_API = "user/profile"
        const val CHECK_PHONE_NUMBER_AVAILABLE = "user/verifynewuser"
        const val FAVORITE_PLACES = "favourite"
        const val DELETE_FAV_PLACES = "favourite/delete/{slug}"
        const val GET_TYPES = "get/types"
        const val GET_PROMO_LIST = "user/promocode"
        const val CREATE_REQUEST = "request/create"
        const val APPLY_PROMO = "user/promoapply"
        const val GET_SOS = "sos"
        const val CANCEL_REQUEST = "request/cancel/user"
        const val REQUEST_IN_PROGRESS = "user/request_in_progress"
        const val CANCEL_REASONS = "cancellation/list"
        const val RATE = "request/rating"
        const val HISTORY_LIST = "request/user/trip/history"
        const val SAVE_SOS = "sos/store"
        const val DELETE_SOS = "sos/delete/{id}"

        const val SUGGESTION_lIST = "suggestions/list"
        const val ADD_COMPLAINTS = "complaints/add"
        const val COMPLAINTS_lIST = "complaints/list"
        const val LOG_OUT_URL = "user/logout"
        const val FAQ_URL = "faq"
        const val WALLET_LIST_URL = "wallet"
        const val WALLET_ADD_AMOUNT = "wallet/add-amount"
        const val DISPUTE = "complaints/trip-list"
        const val NOTIFIACTION_URL = "notification/list"
        const val CHECKZONE_URL = "checkzone"
        const val SELECTED_LANGUAGE = "user/profile/language"
        const val REFERRAL_DETAILS = "get/user/referral"
        const val ERRORLOG = "errorlog"
        const val RENTAL_PACKAGE_LIST = "rental/list"
        const val SELECTED_PACKAGE_DETAILS = "rental/eta"
        const val OUTSTATION_URL = "outstation/list"
        const val OUTSTATION_TYPES = "outstation/eta"
        const val GET_ADMIN_NUMBER = "customer-care"
        const val CHEKCOUTSTATION = "check/outstation"
        const val NIGHT_IMAGE_UPLOAD = "request/image-upload"
        const val INVOICE_QUESTIONS = "invoice/question"
        const val RETAKE_NIGHT = "request/retake-image"
        const val COMPLAINTS_HISTORY = "complaints/history/complaints"
        const val SUGGESTION_HISTORY = "complaints/history/suggestion"
        /**
         * Network parameters
         */
        const val googleMap = "googleMap"
        const val Code = "code"
        const val country = "country"
        const val phone_number = "phone_number"
        const val description = "description"
        const val country_code = "country_code"
        const val country_code_id = "country_code_id"
        const val otp = "otp"
        const val device_info_hash = "device_info_hash"
        const val is_primary = "is_primary"
        const val email = "email"
        const val firstname = "firstname"
        const val lastname = "lastname"
        const val device_type = "device_type"
        const val client_id = "client_id"
        const val client_secret = "client_secret"
        const val grant_type = "grant_type"
        const val title = "title"
        const val address = "address"
        const val latitude = "latitude"
        const val longitude = "longitude"
        const val drop_lat = "drop_lat"
        const val drop_long = "drop_long"
        const val drop_address = "drop_address"
        const val ride_type = "ride_type"
        const val ride_date = "ride_date"
        const val ride_time = "ride_time"
        const val pick_lat = "pick_lat"
        const val pick_address = "pick_address"
        const val drop_lng = "drop_lng"
        const val vehicle_type = "vehicle_type"
        const val payment_opt = "payment_opt"
        const val pick_lng = "pick_lng"
        const val promo_code = "promo_code"
        const val request_id = "request_id"
        const val USER_ID = "USER_ID"
        const val start_connect = "start_connect"
        const val accept_status = "accept_status"
        const val arrive_status = "arrive_status"
        const val rating = "rating"
        const val feedback = "feedback"
        const val reason = "reason"
        const val user_lat = "user_lat"
        const val user_lng = "user_lng"
        const val driver_lat = "driver_lat"
        const val driver_lng = "driver_lng"
        const val user_location = "user_location"
        const val driver_location = "driver_location"
        const val trip_type = "trip_type"
        const val type = "type"
        const val complaint_id = "complaint_id"
        const val answer = "answer"
        const val amount = "amount"
        const val purpose = "purpose"
        const val is_later = "is_later"
        const val trip_start_time = "trip_start_time"
        const val trip_end_time = "trip_end_time"
        const val referral_code = "referral_code"
        const val poly_string = "poly_string"
        const val language = "language"
        const val outstation_id = "outstation_id"

        const val pickup_lat = "pickup_lat"
        const val pickup_long = "pickup_long"

        const val package_id = "package_id"
        const val package_item_id = "package_item_id"
        const val package_changed_ = "package_changed_"
        const val driver_notes = "driver_notes"
        const val booking_for = "booking_for"
        const val others_name = "others_name"
        const val others_number = "others_number"
        const val trip_way_type = "trip_way_type"
        const val images = "images"
        const val photo_upload_ = "photo_upload_"
        const val upload_status = "upload_status"
        const val skip_photo_upload_ = "skip_photo_upload_"
        const val kilometer_upload_ = "kilometer_upload_"
        const val from_date = "from_date"
        const val to_date = "to_date"
        const val question_id = "question_id"
        const val yes = "YES"
        const val no = "NO"
        const val id = "id"
        const val retake_image = "retake_image"
        /**
         * Event names
         */
        const val RECEIVE_COUNTRY = "RECEIVE_COUNTRY"
        const val RECEIVE_LOCATION_PERMISSION_RESULT = "RECEIVE_LOCATION_PERMISSION_RESULT"
        const val NOTIFY_DRAWER_IMAGE_CHANGED = "NOTIFY_DRAWER_IMAGE_CHANGED"
        const val RECEIVE_PICKUP_DETAILS = "RECEIVE_PICKUP_DETAILS"
        const val RECEIVE_PAYMENT_TYPE = "RECEIVE_PAYMENT_TYPE"
        const val RECEIVE_PROMO_CODE = "RECEIVE_PROMO_CODE"
        const val RECEIVE_TRIP_STATUS = "RECEIVE_TRIP_STATUS"
        const val RECEIVE_CANCEL_REASON = "RECEIVE_CANCEL_REASON"
        const val RECEIVE_DIRECTION_INVOICE = "RECEIVE_DIRECTION_INVOICE"
        const val RECEIVE_CLOSE_TRIP = "RECEIVE_CLOSE_TRIP"
        const val RECEIVE_NO_ITEM_FOUND = "RECEIVE_NO_ITEM_FOUND"
        const val locationchanged_ = "locationchanged_"
        const val ADDRESS_CHANGE: String = "ADDRESS_CHANGE"
        const val GETOUTSTATIONSELECTEDPLACE = "GetOutstationSelectedPlace"
        const val NO_DRIVER_FOUND: String = "NO_DRIVER_FOUND"
        const val TRIP_EVENTS: String = "TRIP_EVENTS"
        const val ADDRESS_EDIT_DIALOG: String = "ADDRESS_EDIT_DIALOG"
        const val ADDRESS_EDIT_TO_MAP: String = "ADDRESS_EDIT_TO_MAP"
        const val SHOW_NO_DATA_FOUND_OUTSTATION = "NODATAFOUNDOUTSTATION"
        const val PROFILE_NAME = "PROFILE_NAME"
        const val SMSAUTOFILL = "SMSAUTOFILL"
        const val LOC_TO_RENT_PUSH = "LOC_TO_RENT_PUSH"
        const val TRIP_ACCEPTED = "TRIP_ACCEPTED"
        const val TRIP_ARRIVED = "TRIP_ARRIVED"
        const val TRIP_STARTED = "TRIP_STARTED"
        const val TRIP_COMPLETED = "TRIP_COMPLETED"
        const val TRIP_CANCEL = "TRIP_CANCEL"
        const val RECEIVE_PROFILE_IMAGE = "RECEIVE_PROFILE_IMAGE"
        const val DRIVER_NIGHT_IMAGE = "DRIVER_NIGHT_IMAGE"
        const val CLOSE_NIGHT_IMG = "CLOSE_NIGHT_IMG"
        const val PUSH_UPLOAD_NIGHT = "PUSH_UPLOAD_NIGHT"
        const val SEND_SOS_CONTACT = "SEND_SOS_CONTACT"
        /*
        Intent params
         */
        const val profile_picture = "profile_picture"
        const val pickup_address = "pickup_address"
        const val isPromoApplied = "isPromoApplied"
        const val isTripPickupChange = "isTripPickupChange"
        const val outStationSelcetedPlace = "OutstationSelectedPlace"
        const val showNoDataFoundOutstation = "NoDataFoundInOutStation"
        var isTripRefreshed = false
        var isReqCalled = false
        const val PASS_URI = "PASS_URI"

        /*
        strings
         */

        const val rental = "RENTAL"
        const val outstation = "OUTSTATION"
    }
}
