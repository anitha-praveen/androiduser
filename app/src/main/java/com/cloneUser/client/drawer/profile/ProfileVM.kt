package com.cloneUser.client.drawer.profile

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.localbroadcastmanager.content.LocalBroadcastManager
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.regions.Region
//import com.amazonaws.services.s3.AmazonS3
//import com.amazonaws.services.s3.AmazonS3Client
import com.google.gson.Gson
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.CustomException
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.connection.responseModels.AvailableCountryAndKLang
import com.cloneUser.client.connection.responseModels.UserModel
import com.cloneUser.client.ut.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File
import java.net.URL
import java.util.*
import javax.inject.Inject

class ProfileVM @Inject constructor(
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper

) :
    BaseVM<BaseResponse, ProfileNavigator>(session, mConnect) {

    var FirstName = ObservableField("")
    var LastName = ObservableField("")
    var Email = ObservableField("")
    var Phone_Number = ObservableField("")
    var countryCode = ObservableField("")
    var txt_Language_update = ObservableField("")
    lateinit var userModelData: UserModel
    lateinit var favList: FavPlace
    val gson = Gson()
    val showAddHome = ObservableBoolean(true)
    val showAddWork = ObservableBoolean(true)
    val deletedIndex = ObservableInt()

    var bitmapProfilePicture: ObservableField<GenericsType> = ObservableField<GenericsType>()
    var realPath: String? = null
    var realFile: File? = null
    var genericsType: GenericsType? = null
    var imageURL = ""

    /*
   apiCode 0 -> get profile api
           1 -> image upload
           2 -> update language to db
           3 -> translation
           4 -> logout
           5 -> delete favorite item
           6 -> change name , email
    */
    var apiCode = -1
    var langCode = ObservableField("")
    lateinit var data: AvailableCountryAndKLang

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false

        if (response!!.data != null) {
            when (apiCode) {
                0 -> {
                    userModelData = gson.fromJson(gson.toJson(response.data), UserModel::class.java)
                    favList = gson.fromJson(gson.toJson(response.data), FavPlace::class.java)
                    setUpProfileDetails(userModelData)
                    favList.favouriteList?.let { getNavigator().loadAdapter(it) }
                }
                1 -> {
                    val intent = Intent(Config.NOTIFY_DRAWER_IMAGE_CHANGED)
                    LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)

                    val data = gson.fromJson(gson.toJson(response.data), UserModel::class.java)
                    (data.user?.profile_pic?.let { getNavigator().setImage(it) })
                    getNavigator().showMessage(translationModel.txt_profile_updated ?: "")
                }
                2 -> {
                    langCode.get()?.let { callLanguagesApi(it) }
                }
                3 -> {
                    session.saveString(
                        SessionMaintainence.TRANSLATED_DATA,
                        gson.toJson(response.data!!)
                    )
                    session.saveString(SessionMaintainence.CURRENT_LANGUAGE, langCode.get()!!)
                    getNavigator().refresh()
                }
                4 -> {
                    getNavigator().logOut()
                }
                5 -> {
                    getProfile()
                    translationModel.txt_favo_removed?.let { getNavigator().showMessage(it) }
                }
                6 -> {
                    userModelData = gson.fromJson(gson.toJson(response.data), UserModel::class.java)
                    FirstName.set(userModelData.user?.firstname)
                    Email.set(userModelData.user?.email)
                    val intent = Intent(Config.PROFILE_NAME)
                    intent.putExtra("NAME", userModelData.user?.firstname ?: "")
                    LocalBroadcastManager.getInstance(getNavigator().getCtx()).sendBroadcast(intent)
                    getNavigator().showMessage(translationModel.txt_profile_updated ?: "")
                }
            }
        }
    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }


    fun openGalleryOrCamera(view: View) {
        if (isPhotoAvailable && imageURL.isNotEmpty())
            getNavigator().showProfileImg(imageURL)
        else
            getNavigator().alertSelectCameraGallery()
    }

    var isPhotoAvailable = false

    private fun setUpProfileDetails(userModel: UserModel) {
        FirstName.set(userModel.user!!.firstname)
        LastName.set(userModel.user.lastname)
        Phone_Number.set(userModel.user.phoneNumber)
        Email.set(userModel.user.email)
        txt_Language_update.set(session.getString(SessionMaintainence.CURRENT_LANGUAGE))
        userModel.user.profile_pic?.let { getNavigator().setImage(it) }
        userModel.user.profile_pic?.let {
            if (it.isNotEmpty())
                isPhotoAvailable = true
        }

    }

//    private fun getImageFromS3(profilePic: String?) {
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
//                    imageURL = url.toString()
//                    getNavigator().setImage(url.toString())
//                }
//            }
//        }
//
//    }

    fun onSelectFromGalleryResult(data: Uri?) {
        if (data != null) {
            realPath = RealPathUtil.getRealPath(getNavigator().getCtx(), data)
            realFile = File(if (realPath == null) "" else realPath)
            genericsType = GenericsType()
            genericsType!!.set(realFile)
            bitmapProfilePicture.set(genericsType)
            uploadProfilePicture()
        }
    }

    fun onCaptureImageResult(data: Intent) {
        if (data != null) {
            if (data.hasExtra("data")) {
                realPath = Utilz.getImageUri(
                    getNavigator().getCtx(), data.extras?.get(
                        "data"
                    ) as Bitmap
                )
                realFile = File(realPath)
                genericsType = GenericsType()
                genericsType!!.set(realFile)
                bitmapProfilePicture.set(genericsType)
                uploadProfilePicture()
            }
        }

    }

    fun getProfile() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 0
            getProfileBase()
        } else
            getNavigator().showNetworkUnAvailable()
    }

    private fun uploadProfilePicture() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 1
            requestbody.clear()
            if (realPath != null) {
                val reqFile = realFile?.let {
                    RequestBody.create(
                        "image/*".toMediaTypeOrNull(),
                        it
                    )
                }
                body = reqFile?.let {
                    MultipartBody.Part.createFormData(
                        "profile_pic", realFile!!.name,
                        it
                    )
                }
                updateProfileImageBase()
            }
        } else getNavigator().showNetworkUnAvailable()
    }

    fun getSelectedLanguageTranslation(code: String) {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 2
            val map = HashMap<String, String>()
            map[Config.language] = code
            getPostSelectedLanguage(map)
        } else getNavigator().showNetworkUnAvailable()

    }

    fun callLanguagesApi(code: String) {
        apiCode = 3
        getSelectedLangBase(code)
    }

    fun getLogutApiVm() {
        apiCode = 4
        getLogOutApiBase()
    }

    fun onClickSave() {
        if (getNavigator().isNetworkConnected()) {
            apiCode = 6
            isLoading.value = true
            requestbody.clear()
            requestbody[Config.firstname] =
                RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    FirstName.get()!!
                )
            if (Email.get() != null)
                requestbody[Config.email] =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), Email.get()!!)
            // To check valid mailid
            if (Email.get().isNullOrEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(Email.get()).matches()) {
                updateProfileBase()
            } else {
                isLoading.value = false
                getNavigator().showMessage("enter valid mailid")
            }
        }
    }

    fun deleteFavoritePlace(slug: String) {
        apiCode = 5
        if (getNavigator().isNetworkConnected())
            deleteFavoritePlaceBase(slug)
        else
            getNavigator().showNetworkUnAvailable()
    }
}


