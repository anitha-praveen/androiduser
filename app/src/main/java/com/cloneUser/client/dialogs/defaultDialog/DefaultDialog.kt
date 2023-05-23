package com.cloneUser.client.dialogs.defaultDialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.databinding.DialogDefaultBinding
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject

class DefaultDialog(private val message: String) :
    BaseDialog<DialogDefaultBinding, DefaultDialogVM>(),
    DefaultDialogNavigator {
    companion object {
        const val TAG = "DefaultDialog"
    }

    private lateinit var binding: DialogDefaultBinding


    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(DefaultDialogVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        vm.msg.set(message)
    }

    override fun getLayout(): Int = R.layout.dialog_default


    override fun getBindingVariable(): Int = BR.viewModel


    override fun getViewModel(): DefaultDialogVM = vm
    override fun close() {
        dismiss()
    }
    override fun showSnackBar(message: String) =
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun isNetworkConnected(): Boolean = Utilz.checkForInternet(requireActivity())

    override fun showCustomDialog(message: String) {

    }

}