package com.cloneUser.client.drawer.rating

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.databinding.ObservableFloat
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.regions.Region
//import com.amazonaws.services.s3.AmazonS3
//import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.Gson
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import java.net.URL
import java.util.*
import javax.inject.Inject

class RatingVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, RatingNavigator>(session, mConnect) {
    var requestId = ""
    var driverPic = ObservableField("")
    var driverName = ObservableField("")
    var savedRating = ObservableFloat(5f)
    var carNumber = ObservableField("")
    var carModel = ObservableField("")
    var txt_comments = ObservableField("")
    var userReview = ObservableFloat(5f)
    var hashMap = HashMap<String, String>()
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var requestData: RequestData.Data? = null
    var list = ArrayList<BaseResponse.InvoiceQuestionsList>()
    var invoiceList = ArrayList<BaseResponse.InvoiceQuestionsList>()
    val apiMap = HashMap<String, String>()

    private val map = HashMap<String, Any>()


    var apiCall = -1

    /*
    apicall -> 1 = get questions
                2 = submit rating
     */

    var textToShow =
        arrayOf("Main HeadLine", "Your Message", "New In Technology", "hellow World", "dude hellow")

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCall == 1) {
            val data = Gson().toJson(response?.data)
            val obj = Gson().fromJson(data, BaseResponse.DataObjectsAllApi::class.java)
            invoiceList = obj.invoiceQuestionsList
            getNavigator().loadList(obj.invoiceQuestionsList)

        }
        else if (apiCall == 2) {
            getNavigator().goToHome()
        }
        /*
        {"success":true,"data":{"RequestRating":{"request_id":"a551868f-62d8-4960-bdf7-9ba75509e087","rating":"0.0","feedback":null,"user_id":54,"id":33}},"message":"Data Found"}
         */
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }

    fun hideKeyboard() {

    }

    fun updateReview(view: View) {
        apiCall = 2
        var jsonArray = JSONArray()
        if (list.isNotEmpty()) {
            list.forEach { it ->
                map[Config.id] = it.id as Int
                map[Config.answer] = if (it.isSelected) Config.yes else Config.no
                val obj = JSONObject(map as Map<*, *>?)
                jsonArray.put(obj)
            }
        } else {
            if (invoiceList.isNotEmpty()) {
                invoiceList.forEach { it ->
                    map[Config.id] = it.id as Int
                    map[Config.answer] = if (it.isSelected) Config.yes else Config.no
                    val obj = JSONObject(map as Map<*, *>?)
                    jsonArray.put(obj)
                }

            }
        }
        apiMap[Config.question_id] = jsonArray.toString()
        apiMap[Config.request_id] = requestId
        apiMap[Config.rating] = savedRating.get().toString()
        rateDriverBase(apiMap)

    }

//    fun getImageFromS3(profilePic: String?) {
//        val viewModelJob = Job()
//        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
//
//        uiScope.launch {
//            withContext(Dispatchers.IO) {
//                val cal = Calendar.getInstance()
//                cal.time = Date()
//                cal.add(Calendar.HOUR, +1)
//                val oneHourLater: Date = cal.time
//                val s3: AmazonS3 = AmazonS3Client(
//                    BasicAWSCredentials(
//                        session.getString(SessionMaintainence.AWS_ACCESS_KEY_ID),
//                        session.getString(SessionMaintainence.AWS_SECRET_ACCESS_KEY)
//                    )
//                )
//                s3.setRegion(Region.getRegion(session.getString(SessionMaintainence.AWS_DEFAULT_REGION)))
//                val url: URL = s3.generatePresignedUrl(
//                    session.getString(SessionMaintainence.AWS_BUCKET),
//                    profilePic,
//                    oneHourLater
//                )
//                //setImage(url.toString())
//                //vm.refURL.set(url.toString())
//                withContext(Dispatchers.Main) {
//                    // update here in UI
//                    driverPic.set(url.toString())
//
//                }
//            }
//        }
//
//    }

    fun setData() {
        requestData?.let { data ->
            data.driverOverallRating?.let { savedRating.set(it.toFloat()) }
            data.id?.let { requestId = it }
            data.carDetails?.let { details ->
                details.carNumber?.let { carNumber.set(it) }
                details.carModel?.let { carModel.set(it) }
            }
            data.driver?.let { driver ->
                driver.profilePic?.let {  driverPic.set(it) }
                driver.firstname?.let { first ->
                    driver.lastname?.let { driverName.set("$first $it") }
                }
            }

        }
        printE("ratedlsjadfl", savedRating.get().toString())
    }

    fun getQuestions() {
        apiCall = 1
        getRatingQuestionsBase()
    }
}

@BindingAdapter("imageUrlFeedback")
fun setImageUrl(imageView: ImageView, url: String?) {
    val context = imageView.context
    Glide.with(context).load(url).apply(
        RequestOptions().error(R.drawable.profile_place_holder)
            .placeholder(R.drawable.profile_place_holder)
    ).into(imageView)
}