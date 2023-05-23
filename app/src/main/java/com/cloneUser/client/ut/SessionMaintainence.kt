package com.cloneUser.client.ut

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.cloneUser.client.R
import com.cloneUser.client.connection.responseModels.AvailableCountryAndKLang
import javax.inject.Inject

/**
 * You can edit shared preferences in a more secure way by calling the edit() method
 * on an EncryptedSharedPreferences object instead of on a SharedPreferences object
 * @sample https://developer.android.com/training/data-storage/shared-preferences#WriteSharedPreference
 * */
class SessionMaintainence {
    companion object {
        val FCM_TOKEN = "fcm_token"
        const val AVAILABLE_COUNTRY_AND_LANGUAGES = "AVAILABLE_COUNTRY_AND_LANGUAGES"
        const val GetStartedScrnLoaded = "GetStartedScrnLoaded"
        const val TRANSLATED_DATA = "TRANSLATED_DATA"
        const val CURRENT_LANGUAGE = "CURRENT_LANGUAGE"
        const val TOUR_SHOWN = "TOUR_SHOWN"
        const val AccessToken ="AccessToken"
        const val TokenType = "TokenType"
        const val TRANSLATION_TIME_OLD = "TRANSLATION_TIME_OLD"
        const val TRANSLATION_TIME_NOW = "TRANSLATION_TIME_NOW"
        const val UserProfile = "UserProfile"
        const val SelectedCountryCode = "SelectedCountryCode"
        const val CURRENT_LATITUDE = "CURRENT_LATITUDE"
        const val CURRENT_LONGITUDE = "CURRENT_LONGITUDE"
        const val USER_ID = "USER_ID"
        const val FirstTimeEnglishCalled = "FirstTimeEnglishCalled"
        const val SEARCH_RADIUS = "SEARCH_RADIUS"
        const val REQ_ID = "REQ_ID"
        const val AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID"
        const val AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY"
        const val AWS_DEFAULT_REGION = "AWS_DEFAULT_REGION"
        const val AWS_BUCKET = "AWS_BUCKET"
        const val PLACES_DYNAMIC_KEY = "PLACES_DYNAMIC_KEY"
        const val DISTANCE_DYNAMIC_KEY = "DISTANCE_DYNAMIC_KEY"
        const val GEOCODE_DYNAMIC_KEY = "GEOCODE_DYNAMIC_KEY"
        const val DIRECTION_DYNAMIC_KEY = "DIRECTION_DYNAMIC_KEY"
    }

    @Inject
    constructor(activity: Context) {
        val keyGenParameterSpec=
            MasterKey.Builder(activity).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        sessionM = EncryptedSharedPreferences.create(activity,Config.sharedPrefName,keyGenParameterSpec,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }

    fun enterMID(mID: String) {
        with(sessionM.edit()) {
            putString(id_mid, mID)
            apply()
        }
    }

    fun getMID(): String? {
        return sessionM.getString(id_mid, "")
    }

    fun saveAvailableCountryAndLanguages(dataObj: Any?) {
        sessionM.edit().apply {
            putString(AVAILABLE_COUNTRY_AND_LANGUAGES, Gson().toJson(dataObj))
            apply()
        }
    }

    fun getAvailableCountryAndLanguages(): AvailableCountryAndKLang? {
        var result: AvailableCountryAndKLang? = null
        val sr = sessionM.getString(AVAILABLE_COUNTRY_AND_LANGUAGES, null)
        try {
            result = Gson().fromJson(sr, AvailableCountryAndKLang::class.java)
        } catch (ex: Exception) {
            return null
        }
        return result
    }




    fun saveString(key: String, str: String) {
        sessionM.edit().apply {
            putString(key, str)
            apply()
        }
    }

    fun getString(key: String?): String? {
        return sessionM.getString(key, "")
    }

    fun saveInt(key: String, int: Int) {
        sessionM.edit().apply {
            putInt(key, int)
            apply()
        }
    }



    fun getInt(key: String?): Int {
        return sessionM.getInt(key, 0)
    }

    fun saveLong(key: String, long: Long) {
        sessionM.edit().apply {
            putLong(key, long)
            apply()
        }
    }

    fun getLong(key: String): Long{
        return sessionM.getLong(key,0)
    }

    fun saveBoolean(key: String, value: Boolean) {
        sessionM.edit().apply() {
            putBoolean(key, value)
            apply()
        }
    }

    fun getBoolean(key: String?): Boolean {
        return sessionM.getBoolean(key, false)
    }

    private var sessionM: SharedPreferences
    val id_mid = "id_mid"
    val jid_transl = "jid_transl"
}