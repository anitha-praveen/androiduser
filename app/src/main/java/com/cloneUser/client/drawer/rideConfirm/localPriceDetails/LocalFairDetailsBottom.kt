package com.cloneUser.client.drawer.rideConfirm.localPriceDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.cloneUser.client.R
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.TypesModel
import com.cloneUser.client.databinding.BottomSheetShowPriceBinding
import com.cloneUser.client.drawer.rideConfirm.RideConfirmFragment
import com.cloneUser.client.ut.Utilz

class LocalFairDetailsBottom : BottomSheetDialogFragment(), LocalFairDetailsBottomNav {

    companion object {
        const val TAG = "LocalFairDetailsBottom"
        private lateinit var mTranslationModel: TranslationModel
        private lateinit var mType: TypesModel.ZoneTypePrice
        private lateinit var mTypesModel: TypesModel
        fun newInstance(
            translationModel: TranslationModel,
            type: TypesModel.ZoneTypePrice,
            typesModel: TypesModel
        ) =
            LocalFairDetailsBottom().apply {
                mTranslationModel = translationModel
                mType = type
                mTypesModel = typesModel
            }
    }

    lateinit var binding: BottomSheetShowPriceBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.bottom_sheet_show_price, container, false
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
            LocalFairDetailsBottomVM.Factory(
                requireActivity().application,
                mTranslationModel,
                mType,
                mTypesModel,this
            )
        val model: LocalFairDetailsBottomVM = ViewModelProvider(this, factory)
            .get(LocalFairDetailsBottomVM::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = model
        Glide.with(requireContext()).load(mType.typeImageSelect ?: "")
            .into(binding.carImage)
        model.isPromoApplied.value = (parentFragment as RideConfirmFragment).getVMClass().isPromoApplied.get()
        model.total.value = if(model.isPromoApplied.value == true) mTypesModel.currencySymble+" "+Utilz.removeZero(mType.promoTotalAmount?:"")  else mTypesModel.currencySymble+" "+ Utilz.removeZero(mType.totalAmount?:"")

    }

    override fun gotIt() {
        dialog?.dismiss()
    }

}