package com.cloneUser.client.drawer.trip.nightPhotoBottom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.cloneUser.client.R
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.databinding.TakePhotoDialogBinding
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.drawer.trip.TripFragment
import com.cloneUser.client.ut.Config

class NightPhotoBottom : BottomSheetDialogFragment(), NightPhotoBottomNav {
    companion object {
        const val TAG = "NightPhotoBottom"
        private lateinit var mTranslationModel: TranslationModel
        private lateinit var image: String
        private var isRetake: Boolean = false
        fun newInstance(translationModel: TranslationModel, img: String, retake: Boolean) =
            NightPhotoBottom().apply {
                mTranslationModel = translationModel
                image = img
                isRetake = retake
            }
    }

    lateinit var binding: TakePhotoDialogBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.take_photo_dialog, container, false)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = false
        }

        val factory = NightPhotoBottomVM.Factory(
            requireActivity().application, mTranslationModel,
            image,
            isRetake,
            this
        )
        val model: NightPhotoBottomVM = ViewModelProvider(this, factory)
            .get(NightPhotoBottomVM::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = model
        binding.driverImg.setPadding(0, 0, 0, 0)
        Glide.with(requireContext()).load(image).apply(
            RequestOptions().error(R.drawable.ic_user)
                .placeholder(R.drawable.ic_user).circleCrop()
        ).into(binding.driverImg)
    }

    private val skipNightImg: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            dialog?.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            skipNightImg, IntentFilter(Config.CLOSE_NIGHT_IMG)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(skipNightImg)
    }

    override fun proceed() {
        (requireParentFragment() as TripFragment).openCamera()
        dialog?.dismiss()
    }

    override fun openPhoto(img: String) {
        (requireActivity() as DrawerActivity).showImage(
            mTranslationModel.txt_driver ?: "",
            img,
            false
        )
    }

    override fun retake() {
        (requireParentFragment() as TripFragment).getVMClass().requestRetakeDriver()
        dialog?.dismiss()
    }
}