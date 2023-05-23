package com.cloneUser.client.ut

object KeySearchClass {
    private var searchValue: String? = null
    fun KeySearch(search: Map.Entry<String?, List<String?>>): String? {
        if (search.key != null)
            when {
                search.key.equals(Config.phone_number, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.device_info_hash, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.country_code, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.is_primary, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.Code, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.country, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.description, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.country_code, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.otp, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.email, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.firstname, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.lastname, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.device_type, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.client_id, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.client_secret, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.grant_type, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.title, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.address, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.latitude, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.longitude, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.drop_lat, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.drop_long, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.drop_address, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.ride_type, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.ride_date, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.ride_time, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.pick_lat, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.pick_address, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.drop_lng, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.vehicle_type, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.payment_opt, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.pick_lng, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.promo_code, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.request_id, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.rating, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.feedback, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.reason, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.user_lat, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.user_lng, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.driver_lat, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.driver_lng, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.user_location, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.driver_location, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.trip_type, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.type, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.complaint_id, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.answer, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.amount, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.purpose, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.is_later, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.trip_start_time, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.referral_code, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.poly_string, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.language, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.ERRORLOG, ignoreCase = true) -> searchValue = search.value[0]
                search.key.equals(Config.package_id, ignoreCase = true) -> searchValue = search.value[0]
            }
        return searchValue
    }
}


