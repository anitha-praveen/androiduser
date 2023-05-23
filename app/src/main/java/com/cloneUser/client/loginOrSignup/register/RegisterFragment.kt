package com.cloneUser.client.loginOrSignup.register

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cloneUser.client.BR
import com.cloneUser.client.MyApplication
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.FragmentRegisterBinding
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.ut.Config
import javax.inject.Inject

class RegisterFragment : BaseFragment<FragmentRegisterBinding, RegisterVM>(),
    RegisterNavigator {
    companion object {
        const val TAG = "RegisterFragment"
    }

    private lateinit var binding: FragmentRegisterBinding

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(RegisterVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                close()
            }
        })

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        arguments?.let {
            vm.phoneForDisplay.set(it.getString(Config.phone_number))
            vm.countryForDisplay.set(it.getString(Config.country_code))
            vm.countryForApi.set(it.getString(Config.country_code_id))
        }
    }

    override fun getLayoutId() = R.layout.fragment_register

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm
    override fun getAct(): Activity {
        return requireActivity()
    }

    override fun goToDrawer() {
        startActivity(Intent(requireActivity(), DrawerActivity::class.java))
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun openImageDialog() {
        val items = arrayOf(
            vm.translationModel.text_camera ?: "Camera",
            vm.translationModel.text_galary ?: "Gallery"
        )
        val builder1 = AlertDialog.Builder(requireContext())
        builder1.setTitle(vm.translationModel.txt_Choose)
        builder1.setCancelable(true)
        builder1.setItems(
            items
        ) { dialog: DialogInterface?, which: Int ->
            if (which == 0) {
                cameraIntent()
                camera = true
            } else if (which == 1) {
                galleryIntent()
                camera = false
            }
        }.show()

    }

    override fun getCtx(): Context {
        return if (isAdded) requireContext()
        else MyApplication.appContext!!
    }

    override fun setImage(url: String) {
            Glide.with(getCtx()).load(url).apply(
                RequestOptions.circleCropTransform().error(R.drawable.ic_profile_dummy_icon)
                    .placeholder(R.drawable.ic_profile_dummy_icon)
            ).into(binding.profile)

    }

    private fun cameraIntent() {
        if (!hasPermissions(requireContext(), Config.storagePermission)) {
            requestMultiplePermissions.launch(Config.storagePermission)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("com.google.assistant.extra.USE_FRONT_CAMERA", true)
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)

            // Samsung
            intent.putExtra("camerafacing", "front")
            intent.putExtra("previous_mode", "front")

            // Huawei
            intent.putExtra("default_camera", "1")
            intent.putExtra("default_mode", "com.huawei.camera2.mode.photo.PhotoMode")
            resultLauncherCamera.launch(intent)
        }
    }

    private fun galleryIntent() {
        if (!hasPermissions(requireContext(), Config.storagePermission)) {
            requestMultiplePermissions.launch(Config.storagePermission)
        } else {
            val pickIntent = Intent(Intent.ACTION_PICK)
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            resultLauncherGallery.launch(pickIntent)
        }
    }


    val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach { actionMap ->
                val permissionName = actionMap.key
                val isGranted = actionMap.value
                if (permissionName.equals("android.permission.CAMERA")) {
                    permissionCamera = isGranted
                } else if (permissionName.equals("android.permission.READ_EXTERNAL_STORAGE")) {
                    permissionReadExternalStorage = isGranted
                } else {
                    permissionWriteExternalStorage = isGranted
                }
            }
            if (permissionCamera && permissionReadExternalStorage && permissionWriteExternalStorage) {
                if (camera) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {

                        putExtra("com.google.assistant.extra.USE_FRONT_CAMERA", true)
                        putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                        putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                        putExtra("android.intent.extras.CAMERA_FACING", 1)

                        // Samsung
                        putExtra("camerafacing", "front")
                        putExtra("previous_mode", "front")

                        // Huawei
                        putExtra("default_camera", "1")
                        putExtra("default_mode", "com.huawei.camera2.mode.photo.PhotoMode")
                    }
                    resultLauncherCamera.launch(intent)
                } else {
                    val pickIntent = Intent(Intent.ACTION_PICK)
                    pickIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*"
                    )
                    resultLauncherGallery.launch(pickIntent)
                }
            }

        }


    var resultLauncherCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                vm.onCaptureImageResult(data)
            }
        }

    var resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                vm.onSelectFromGalleryResult(data)
            }
        }

    var permissionCamera = false
    var permissionReadExternalStorage = false
    var permissionWriteExternalStorage = false
    var camera: Boolean = false

}