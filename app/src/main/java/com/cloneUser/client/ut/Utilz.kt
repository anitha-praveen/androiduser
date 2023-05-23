package com.cloneUser.client.ut

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.maps.android.PolyUtil
import com.cloneUser.client.R
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.Route
import com.cloneUser.client.connection.responseModels.Step
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * @source https://www.apriorit.com/dev-blog/612-mobile-cybersecurity-encryption-in-android
 * */
object Utilz {
    var data: BaseResponse.NewUser? = null
    val gson = Gson()

    val searchCondition: IntArray = intArrayOf(4, 6, 8, 10)
    fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection

        // Returns a Network object corresponding to
        // the currently active default data network.
        val network = connectivityManager.activeNetwork ?: return false

        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    }


    /* check empty and null  */
    fun isEmpty(value: String): Boolean {
        return value.isEmpty() || value.isBlank()
    }

    fun isEmailValid(email: String?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun convertToException(response: ResponseBody, code: Int): CustomException? {
        var value: String? = ""
        var exception: CustomException? = null
        try {
            Log.e("MyTEST_RESPONSE", response.toString())

            val baseResponse: BaseResponse =
                StringToObject(response.string(), BaseResponse::class.java) as BaseResponse//
            Log.e("MyTEST_RESPONSE", Gson().toJson(baseResponse.message))
            if (baseResponse.data != null) {
                val customError: CustomError =
                    Gson().fromJson(Gson().toJson(baseResponse), CustomError::class.java)
                if (customError.data?.errorCode != null && customError.data.errorCode == 1001) {
                    value = baseResponse.message
                    exception = CustomException(1001, value)
                } else {
                    val errorList: ValidationError =
                        Gson().fromJson(Gson().toJson(baseResponse), ValidationError::class.java)
                    errorList.errors?.let {
                        for (error in it.entries)
                            value = KeySearchClass.KeySearch(error)
                    }
                    if (value.isNullOrEmpty()) {
                        value = baseResponse.message
                    }
                    exception = CustomException(code, value)

                }
            } else {
                if (!TextUtils.isEmpty(baseResponse.message)) value = baseResponse.message
                exception = CustomException(code, value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(
                "MyTEST_RESPONSE",
                "${e.message}   , ${e.localizedMessage}   , ${e.printStackTrace()}"
            )
        }
        return exception
    }

    fun StringToObject(message: String?, objectClassName: Class<*>?): Any? {
        return Gson().fromJson<Any>(message, objectClassName)
    }

    /**
     * @param phoneNumber is string from user entered.
     * @return a string value of remove first zero.
     */
    fun removeFirstZeros(phoneNumber: String): String {
        var resolvedPhoeNumber = phoneNumber
        do {
            if (resolvedPhoeNumber.startsWith("0")) resolvedPhoeNumber =
                resolvedPhoeNumber.substring(
                    1,
                    if (resolvedPhoeNumber.length == 1) resolvedPhoeNumber.length else resolvedPhoeNumber.length
                )
        } while (resolvedPhoeNumber.startsWith("0"))
        return resolvedPhoeNumber
    }

    /**
     * @param inImage is Bitmap image and get a absolute path of the Image.
     * @return
     */
    fun getImageUri(context: Context, inImage: Bitmap): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.ENGLISH).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            val realFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
            )
            val out = FileOutputStream(realFile)
            inImage.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            realFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    /*
     Only conversion with vector drawable
    */
    fun getBitmapDescriptor(context: Context?, @DrawableRes id: Int): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            getBitmapFromDrawable(context, id)!!
        )
    }

    fun getBitmapFromDrawable(context: Context?, @DrawableRes drawableId: Int): Bitmap? {
        var drawable: Drawable? = null
        if (context != null) drawable = ContextCompat.getDrawable(context, drawableId)
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }


    /**
     * @param response  is array of latlng for draw a route.
     * @param routeBean
     * @return
     */
    fun parseRoute(response: JsonObject, routeBean: Route?): Route? {
        var stepBean: Step
        //   JSONObject jObject = new JSONObject(response);
        val jArray = response.getAsJsonArray("routes")
        for (i in 0 until jArray.size()) {
            val innerjObject = jArray[i].asJsonObject
            if (innerjObject != null) {
                if (innerjObject.has("overview_polyline"))
                    routeBean?.polyPoints =
                        innerjObject.getAsJsonObject("overview_polyline")["points"].asString

                val innerJarry = innerjObject.getAsJsonArray("legs")
                for (j in 0 until innerJarry.size()) {
                    val jObjectLegs = innerJarry[j].asJsonObject
                    routeBean?.setDistanceText(jObjectLegs.getAsJsonObject("distance")["text"].asString)
                    routeBean?.setDistanceValue(jObjectLegs.getAsJsonObject("distance")["value"].asInt)
                    routeBean?.setDurationText(jObjectLegs.getAsJsonObject("duration")["text"].asString)
                    routeBean?.setDurationValue(jObjectLegs.getAsJsonObject("duration")["value"].asInt)
                    routeBean?.setStartAddress(jObjectLegs["start_address"].asString)
                    if (jObjectLegs.has("end_address")) routeBean?.setEndAddress(jObjectLegs["end_address"].asString)
                    routeBean?.setStartLat(jObjectLegs.getAsJsonObject("start_location")["lat"].asDouble)
                    routeBean?.setStartLon(jObjectLegs.getAsJsonObject("start_location")["lng"].asDouble)
                    routeBean?.setEndLat(jObjectLegs.getAsJsonObject("end_location")["lat"].asDouble)
                    routeBean?.setEndLon(jObjectLegs.getAsJsonObject("end_location")["lng"].asDouble)
                    val jstepArray = jObjectLegs
                        .getAsJsonArray("steps")
                    if (jstepArray != null) {
                        for (k in 0 until jstepArray.size()) {
                            stepBean = Step()
                            val jStepObject = jstepArray[k].asJsonObject
                            if (jStepObject != null) {
                                stepBean.setHtml_instructions(
                                    jStepObject["html_instructions"].asString
                                )
                                stepBean.setStrPoint(
                                    jStepObject
                                        .getAsJsonObject("polyline")["points"].asString
                                )
                                stepBean.setStartLat(
                                    jStepObject
                                        .getAsJsonObject("start_location")["lat"].asDouble
                                )
                                stepBean.setStartLon(
                                    jStepObject
                                        .getAsJsonObject("start_location")["lng"].asDouble
                                )
                                stepBean.setEndLat(
                                    jStepObject
                                        .getAsJsonObject("end_location")["lat"].asDouble
                                )
                                stepBean.setEndLong(
                                    jStepObject
                                        .getAsJsonObject("end_location")["lng"].asDouble
                                )
                                stepBean.setListPoints(
                                    PolyLineUtils()
                                        .decodePoly(stepBean.getStrPoint()!!)
                                )
                                routeBean?.getListStep()!!.add(stepBean)
                            }
                        }
                    }
                }
            }
        }
        return routeBean
    }


    fun decodeOverviewPolyLinePoints(encoded: String): List<LatLng> {
        return try {
            PolyUtil.decode(encoded)
        } catch (e: Exception) {
            val modifiedEncodedPath = "$encoded@"
            PolyUtil.decode(modifiedEncodedPath)
        }
    }

    class ValidationError {
        @SerializedName("data")
        var errors: Map<String, List<String>>? = null
    }

    class CustomError {
        @SerializedName("data")
        @Expose
        val data: Data? = null

        class Data {
            @SerializedName("error_code")
            @Expose
            val errorCode: Int? = null
        }
    }

    fun openKeyBoard(activity: Activity) {
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }

    fun <T> getSingleObject(json: String?, modelClass: Class<T>?): T? {
        try {
            return Gson().fromJson(json, modelClass)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    var dialog: Dialog? = null
    fun showProgress(context: Context) {
        if (dialog != null)
            dialog!!.dismiss()
        dialog = Dialog(context)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(R.layout.loader_layout)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog!!.setCancelable(false)
        dialog!!.setCanceledOnTouchOutside(false)
        if (dialog != null) {
            dialog!!.dismiss()
            dialog!!.show()
        }
    }

    fun dismissProgress() {
        if (dialog != null) {
            dialog!!.dismiss()
        }
    }

    fun isTimeBetweenTwoTime(
        initialTime: String, finalTime: String,
        currentTime: String
    ): Boolean {
        return try {
            val reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$".toRegex()
            if (initialTime.matches(reg) && finalTime.matches(reg) &&
                currentTime.matches(reg)
            ) {
                var valid = false
                //Start Time
                //all times are from java.util.Date
                val inTime = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(initialTime)
                val calendar1 = Calendar.getInstance()
                calendar1.time = inTime

                //Current Time
                val checkTime = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(currentTime)
                val calendar3 = Calendar.getInstance()
                calendar3.time = checkTime

                //End Time
                val finTime = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(finalTime)
                val calendar2 = Calendar.getInstance()
                calendar2.time = finTime
                if (finalTime.compareTo(initialTime) < 0) {
                    calendar2.add(Calendar.DATE, 1)
                    calendar3.add(Calendar.DATE, 1)
                }
                val actualTime = calendar3.time
                if ((actualTime.after(calendar1.time) ||
                            actualTime.compareTo(calendar1.time) == 0) &&
                    actualTime.before(calendar2.time)
                ) {
                    valid = true
                    valid
                } else {
                    false
                }
            } else false
        } catch (e: ParseException) {
            e.printStackTrace()
            false
        }
    }

    fun removeZero(value : String) : String{
        return if(value.contains(".")){
            val array = value.split(".")
            if(allCharactersZero(array[1]))
                array[0]
            else
                value
        }else
            value
    }

    private fun allCharactersZero(s: String): Boolean {
        var msg = 0
        val chars: MutableList<Char> = s.toMutableList()
        for (char in chars){
            if(char != '0')
                msg += 1
        }
        return msg==0
    }

}