package com.cloneUser.client.dialogs.paymentType

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.R
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.databinding.DialogPaymentTypeBinding
import com.cloneUser.client.dialogs.paymentType.adapter.PaymentTypeAdapter
import com.cloneUser.client.drawer.rideConfirm.RideConfirmFragment
import com.cloneUser.client.ut.Utilz
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentTypeDialog : BottomSheetDialogFragment(), PaymentTypeNavigator {


    companion object {
        const val TAG = "PaymentTypeDialog"
        private lateinit var mTranslationModel: TranslationModel
        private lateinit var mPayTypes: MutableList<String>
        private lateinit var mPreviouslySelectedPayment: String
        fun newInstance(
            translationModel: TranslationModel,
            payTypes: MutableList<String>,
            previouslySelectedPayment: String
        ) =
            PaymentTypeDialog().apply {
                mTranslationModel = translationModel
                mPayTypes = payTypes
                mPreviouslySelectedPayment = previouslySelectedPayment
            }
    }

    lateinit var binding: DialogPaymentTypeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_payment_type, container, false
        )
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    lateinit var model: PaymentTypeVM
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        val factory =
            PaymentTypeVM.Factory(
                requireActivity().application,
                mTranslationModel, this
            )
        model = ViewModelProvider(this, factory)[PaymentTypeVM::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = model
        model.paymentType.set(mPreviouslySelectedPayment)
        setupAdapter()
    }

    lateinit var adapter: PaymentTypeAdapter
    private fun setupAdapter() {
        adapter = PaymentTypeAdapter(mPayTypes, this, mPreviouslySelectedPayment)
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.paymentMethodRecycler.layoutManager = mLayoutManager
        binding.paymentMethodRecycler.itemAnimator = DefaultItemAnimator()
        binding.paymentMethodRecycler.adapter = adapter
    }

    override fun setPaymentMethod(payMethod: String) {
        model.paymentType.set(payMethod)
    }

    override fun sendPaymentType() {
        when (requireParentFragment()) {
            is RideConfirmFragment -> (requireParentFragment() as RideConfirmFragment).setPaymentType(
                model.paymentType.get() ?: ""
            )
        }
        dismiss()

    }

    override fun getTranslation(): TranslationModel {
        return mTranslationModel
    }

    override fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}