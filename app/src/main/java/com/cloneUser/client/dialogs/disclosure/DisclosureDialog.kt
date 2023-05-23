package com.cloneUser.client.dialogs.disclosure

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cloneUser.client.BR
import com.cloneUser.client.MyApplication
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.databinding.DialogDisclosureBinding
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject

class DisclosureDialog(permissions: Array<String>, requestCode: Int) :
    BaseDialog<DialogDisclosureBinding, DisclosureVM>(),
    DisclosureNavigator {
    companion object {
        const val TAG = "DisclosureDialog"
    }

    private val myApp = MyApplication()
    val permission = permissions
    val reqCode = requestCode
    private lateinit var binding: DialogDisclosureBinding
    val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
// returns Map<String, Boolean> where String represents the
            // permission requested and boolean represents the
            // permission granted or not
// iterate over each entry of map and take action needed for
            // each permission requested
            permissions.forEach { actionMap ->
                when (actionMap.key) {
                    Manifest.permission.ACCESS_COARSE_LOCATION -> {
                        if (actionMap.value) {
// permission granted continue the normal
                            // workflow of app
                            val intent = Intent(Config.RECEIVE_LOCATION_PERMISSION_RESULT)
                            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                            dismiss()
                        } else {
// if permission denied then check whether never
                            // ask again is selected or not by making use of
                            // !ActivityCompat.shouldShowRequest
                            // PermissionRationale(requireActivity(),
                            // Manifest.permission.CAMERA)

                            val showRationale = shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)
                            if(showRationale){
                                vm.heading.set("Location permission required")
                                vm.subHeading.set("Turn on Location to quickly see available rides near you.")
                            }else{
                              redirectToSettings()
                            }

                        }
                    }
                }
            }
        }

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(DisclosureVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setDialogFullSCreen()
    }

    override fun getLayout(): Int = R.layout.dialog_disclosure


    override fun getBindingVariable(): Int = BR.viewModel


    override fun getViewModel(): DisclosureVM = vm


    override fun showSnackBar(message: String) =
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun isNetworkConnected(): Boolean = Utilz.checkForInternet(requireActivity())

    override fun showCustomDialog(message: String) {

    }

    override fun requestPermission() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
    private fun redirectToSettings(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if(checkGranted(Config.Array_permissions)){
            val intent = Intent(Config.RECEIVE_LOCATION_PERMISSION_RESULT)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()

        }
    }

}