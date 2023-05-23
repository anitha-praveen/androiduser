package com.cloneUser.client.drawer.profile

import android.app.Activity
import android.app.Dialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.connection.responseModels.Languages
import com.cloneUser.client.databinding.FragmentProfileBinding
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.drawer.adapters.fav_adapter.FavoritesAdapter
import com.cloneUser.client.loginOrSignup.LoginOrSignupActivity
import com.cloneUser.client.loginOrSignup.getStarteedScrn.LanguageAdapter
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import javax.inject.Inject


class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileVM>(),
    ProfileNavigator {
    companion object {
        const val TAG = "ProfileFragment"
    }

    var permissionCamera = false
    var permissionReadExternalStorage = false
    var permissionWriteExternalStorage = false
    var camera: Boolean = false
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: FragmentProfileBinding
    var dialog: Dialog? = null
    lateinit var adapter: FavoritesAdapter

    /*
    The below declaration receive the result of camera captured image
     */
    var resultLauncherCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                vm.onCaptureImageResult(data!!)
            }
        }

    /*
    The below declaration receive the result of selected image from gallery
     */
    var resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
               val action = ProfileFragmentDirections.toCropImage(data?.data.toString())
                findNavController().navigate(action)
            }
        }

    /*
    The below declaration receive the result of permission allowed or not
     */
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

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(ProfileVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backHandling()
            }
        })

    }

    override fun onStart() {
        super.onStart()
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(profileImageUri,
                IntentFilter(Config.RECEIVE_PROFILE_IMAGE)
            )
        }
    }
    private val profileImageUri: BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            val uri = Uri.parse(intent?.getStringExtra(Config.PASS_URI))
            vm.onSelectFromGalleryResult(uri)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        // binding.layoutToolbar.toolbar.setNavigationOnClickListener { v -> closeFragment() }
        vm.getProfile()
        vm.countryCode.set(vm.session.getString(SessionMaintainence.SelectedCountryCode))
        //favouritesClickListner()
        binding.logOutPro.setOnClickListener {
            showLogOutDialog()
        }
        binding.backImg.setOnClickListener { backHandling() }
        clickListners()
        setAdapter()

    }

    override fun onDestroy() {
        super.onDestroy()
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(profileImageUri) }
    }
    private fun showLogOutDialog() {
        val logDialog = context?.let { BottomSheetDialog(it, R.style.AppBottomSheetDialogTheme) }
        logDialog?.setContentView(R.layout.logout_dialog)
        logDialog?.show()


        val title = logDialog?.findViewById<TextView>(R.id.log_title)
        val desc = logDialog?.findViewById<TextView>(R.id.desc_log)
        val cancelButton = logDialog?.findViewById<MaterialButton>(R.id.cancel_log)
        val logOUtButton = logDialog?.findViewById<MaterialButton>(R.id.logOut_button)
        if (title != null) {
            title.text = vm.translationModel.txt_logout
        }
        if (desc != null) {
            desc.text = vm.translationModel.text_desc_logout
        }
        if (cancelButton != null) {
            cancelButton.text = vm.translationModel.text_cancel
        }
        if (logOUtButton != null) {
            logOUtButton.text = vm.translationModel.txt_logout
        }
        logOUtButton?.setOnClickListener {
            vm.getLogutApiVm()
            logDialog.dismiss()

        }
        cancelButton?.setOnClickListener {
            logDialog.dismiss()
        }
        logDialog?.setCancelable(true)
        logDialog?.show()
        logDialog!!.setOnKeyListener { arg0, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                logDialog.dismiss()
            }
            true
        }

    }


    override fun getLayoutId() = R.layout.fragment_profile

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    override fun editProfile(mode: Int, value: String) {
//        val action = ProfileFragmentDirections.toEditProfile(mode, value)
//        findNavController().navigate(action)
    }

    override fun alertSelectCameraGallery() {
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
        return requireContext()
    }

    override fun setImage(url: String) {
        if (isAdded) {
            Glide.with(requireContext()).load(url).apply(
                RequestOptions.circleCropTransform().error(R.drawable.simple_profile_bg)
                    .placeholder(R.drawable.simple_profile_bg)
            ).into(binding.imgProfilePicProfile)
        }
    }

    /*
    The below method "openLanguageSelection()" opens dialogue for selecting languages
     */

    override fun openLanguageSelection() {
        vm.data = vm.session.getAvailableCountryAndLanguages()!!
        dialog = Dialog(requireContext())
        dialog!!.setContentView(R.layout.dialogue_language)

        val recyclerView: RecyclerView = dialog!!.findViewById(R.id.lang_recycler)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val languageAdapter = LanguageAdapter(
            vm.data.languages!!,
            this,
            vm.session.getString(SessionMaintainence.CURRENT_LANGUAGE) ?: ""
        )
        recyclerView.adapter = languageAdapter
        val setLanguage: MaterialTextView = dialog!!.findViewById(R.id.set_lang)
        val choose: MaterialTextView = dialog!!.findViewById(R.id.title_txt)
        setLanguage.text = vm.translationModel.txt_set_lang
        choose.text = vm.translationModel.txt_choose_language
        setLanguage.setOnClickListener {
            if (vm.langCode.get()?.length == 0) {
                dialog!!.dismiss()
            } else {
                vm.langCode.get()?.let { it1 -> vm.getSelectedLanguageTranslation(it1) }
                dialog!!.dismiss()
            }
        }
        dialog!!.show()
    }

    override fun setSelectedLanguage(languages: Languages) {
        vm.langCode.set(languages.code!!)
        vm.session.saveLong(
            SessionMaintainence.TRANSLATION_TIME_NOW,
            languages.updatedDate!!.toLong()
        )
    }

    override fun refresh() {
        startActivity(Intent(requireContext(), DrawerActivity::class.java))
        requireActivity().finish()
    }

    override fun logOut() {
        vm.session.saveString(SessionMaintainence.AccessToken, "")
        startActivity(Intent(context, LoginOrSignupActivity::class.java))
        activity?.finish()
    }

    override fun loadAdapter(places: List<FavPlace.Favourite>) {
        this.adapter.addList(places)
        if (places.isNotEmpty()) {
            binding.proFavo.visibility = View.VISIBLE
            val homeTxt = places.find { it ->
                vm.translationModel.txt_Home.equals(it.title)
            }
            val workTxt = places.find { it ->
                vm.translationModel.txt_Work.equals(it.title)
            }
            if (homeTxt?.title.equals(vm.translationModel.txt_Home))
                vm.showAddHome.set(false)
            if (workTxt?.title.equals(vm.translationModel.txt_Work))
                vm.showAddWork.set(false)
        } else {
            vm.showAddWork.set(true)
            vm.showAddHome.set(true)
            binding.proFavo.visibility = View.GONE
        }

    }

    override fun deleteFavorite(favPlace: FavPlace.Favourite) {
        vm.deleteFavoritePlace(favPlace.slug!!)
    }

    var imageDialog: Dialog? = null
    override fun showProfileImg(url: String) {
        if (imageDialog?.isShowing == true)
            imageDialog?.dismiss()
        imageDialog = Dialog(requireContext())
        imageDialog?.setContentView(R.layout.dialog_show_profile)
        val profileTitle = imageDialog?.findViewById<TextView>(R.id.profile_txt)
        val camera = imageDialog?.findViewById<TextView>(R.id.camera)
        val gallery = imageDialog?.findViewById<TextView>(R.id.gallery)
        val profile = imageDialog?.findViewById<ImageView>(R.id.profile_image)
        camera?.text = vm.translationModel.text_camera
        gallery?.text = vm.translationModel.text_galary
        profileTitle?.text = vm.FirstName.get()
        if (isAdded) {
            if (profile != null) {
                Glide.with(requireContext()).load(url).apply(
                    RequestOptions.errorOf(R.drawable.ic_user)
                        .placeholder(R.drawable.ic_user)
                ).into(profile)
            }
        }
        camera?.setOnClickListener {
            imageDialog?.dismiss()
            cameraIntent()
        }

        gallery?.setOnClickListener {
            imageDialog?.dismiss()
            galleryIntent()
        }
        imageDialog?.show()

    }


    private fun closeFragment() {
        findNavController().popBackStack()
    }

    private fun clickListners() {
        binding.addWorkTxt.setOnClickListener {
            val action = vm.translationModel.txt_Work?.let { it1 ->
                ProfileFragmentDirections.toProfileAddFavourites(it1)
            }
            if (action != null) {
                findNavController().navigate(action)
            }
        }
        binding.addOtherTxt.setOnClickListener {
            val action = vm.translationModel.txt_Other?.let { it1 ->
                ProfileFragmentDirections.toProfileAddFavourites(it1)
            }
            if (action != null) {
                findNavController().navigate(action)
            }
        }
        binding.addHomeTxt.setOnClickListener {
            val action = vm.translationModel.txt_Home?.let { it1 ->
                ProfileFragmentDirections.toProfileAddFavourites(it1)
            }
            if (action != null) {
                findNavController().navigate(action)
            }
        }
    }


    /** Open camera  */
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

    /** Open Gallery */
    private fun galleryIntent() {
        if (!hasPermissions(requireContext(), Config.storagePermission)) {
            requestMultiplePermissions.launch(Config.storagePermission)
        } else {
            val pickIntent = Intent(Intent.ACTION_PICK)
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            resultLauncherGallery.launch(pickIntent)
        }
    }

    fun setAdapter() {
        val sLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = FavoritesAdapter(ArrayList(), this, 1)
        binding.proFavo.layoutManager = sLayoutManager
        binding.proFavo.itemAnimator = DefaultItemAnimator()
        binding.proFavo.adapter = adapter
    }

    fun backHandling() {
        findNavController().popBackStack()
    }


}