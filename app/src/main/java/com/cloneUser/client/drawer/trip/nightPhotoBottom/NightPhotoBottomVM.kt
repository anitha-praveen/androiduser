package com.cloneUser.client.drawer.trip.nightPhotoBottom

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cloneUser.client.connection.TranslationModel

class NightPhotoBottomVM(
    application: Application,
    val translationModel: TranslationModel,
    val img: String,
    val retake: Boolean,
    val nav: NightPhotoBottomNav
) : AndroidViewModel(application) {
    val isRetake = MutableLiveData(retake)
    fun onClickDriver() {
        nav.openPhoto(img)
    }

    fun onClickRetake() {
        nav.retake()
    }

    class Factory(
        val application: Application,
        val translationModel: TranslationModel,
        val img: String,
        val retake: Boolean,
        val nav: NightPhotoBottomNav
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NightPhotoBottomVM(application, translationModel, img, retake, nav) as T
        }

    }


    fun onClickProceed() {
        nav.proceed()
    }
}
