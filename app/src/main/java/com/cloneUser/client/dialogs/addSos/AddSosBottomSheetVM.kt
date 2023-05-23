package com.cloneUser.client.dialogs.addSos

import android.app.Application
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cloneUser.client.connection.TranslationModel

class AddSosBottomSheetVM(
    application: Application,
    val translationModel: TranslationModel,
    val nav: AddSosBottomNav
) : AndroidViewModel(application) {

    val name = ObservableField("")
    val phone = ObservableField("")

    fun onclickSubmit(view: View) {
        var msg = ""
        if (name.get()!!.isEmpty()) {
            msg = translationModel.txt_enter_contact_name ?: ""
        } else if (phone.get()!!.isEmpty()) {
            msg = translationModel.txt_enter_contact_number ?: ""
        }

        if (msg.isNotEmpty())
            Toast.makeText(view.context, msg, Toast.LENGTH_SHORT).show()
        else
            nav.sendDetails()
    }

    class Factory(
        val application: Application,
        val translationModel: TranslationModel,
        val nav: AddSosBottomNav
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddSosBottomSheetVM(application, translationModel, nav) as T
        }

    }

}