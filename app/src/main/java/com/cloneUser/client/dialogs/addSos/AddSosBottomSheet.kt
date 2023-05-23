package com.cloneUser.client.dialogs.addSos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.cloneUser.client.R
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.databinding.AddSosLayoutBinding
import com.cloneUser.client.ut.Config

class AddSosBottomSheet : BottomSheetDialogFragment(), AddSosBottomNav {

    companion object {
        const val TAG = "LocalFairDetailsBottom"
        private lateinit var mTranslationModel: TranslationModel
        fun newInstance(translationModel: TranslationModel) =
            AddSosBottomSheet().apply {
                mTranslationModel = translationModel
            }
    }

    lateinit var binding: AddSosLayoutBinding
    lateinit var model: AddSosBottomSheetVM
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.add_sos_layout, container, false
        )
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        val factory =
            AddSosBottomSheetVM.Factory(
                requireActivity().application,
                mTranslationModel, this
            )
        model = ViewModelProvider(this, factory)[AddSosBottomSheetVM::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = model
    }

    override fun sendDetails() {
        val intent = Intent(Config.SEND_SOS_CONTACT)
        intent.putExtra(Config.firstname, model.name.get())
        intent.putExtra(Config.phone_number, model.phone.get())
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        dismiss()
    }


}