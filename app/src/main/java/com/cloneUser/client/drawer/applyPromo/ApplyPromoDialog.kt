package com.cloneUser.client.drawer.applyPromo

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textview.MaterialTextView
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.connection.responseModels.PromoCodeModel
import com.cloneUser.client.databinding.FragmentApplyPromoBinding
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.drawer.applyPromo.adapter.PromoAdapter
import com.cloneUser.client.ut.Config
import java.util.*
import javax.inject.Inject

class ApplyPromoDialog(private val promoCode: String, private val isPromoApplied: Boolean, private val tripType: String) :
    BaseDialog<FragmentApplyPromoBinding, ApplyPromoVM>(),
    ApplyPromoNavigator {
    companion object {
        const val TAG = "ApplyPromoFragment"
    }

    private lateinit var binding: FragmentApplyPromoBinding
    var adapter: PromoAdapter? = null

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(ApplyPromoVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setDialogFullSCreen()

        dialog?.setOnKeyListener { arg0, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                sendDetails()
                dismiss()
            }
            false
        }

        binding.backImg.setOnClickListener {
            sendDetails()
            dismiss()
        }
        vm.promoCode.set(promoCode)
        vm.isPromoApplied.set(isPromoApplied)
        if (!vm.isPromoApplied.get() && vm.promoCode.get()!!.isNotEmpty())
            vm.showApply.set(true)
        else if (vm.isPromoApplied.get())
            vm.showApply.set(false)
        setupAdapter()
    }


    override fun addList(promos: List<PromoCodeModel.Promocode>?) {
        adapter!!.addList(promos!!)
    }

    override fun onPromoSelected(promo: PromoCodeModel.Promocode) {
        promo.countrySymbol?.let { vm.currency = it }
        vm.promoCode.set(promo.promoCode)
        promo.promoCode?.let { vm.applyPromo(it) }
    }

    override fun showPromoSuccess() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                mActivity as Context,
                R.color.clr_trans
            )
        )

        dialog.setContentView(R.layout.dialog_promo_applied)
        val promoCode = dialog.findViewById(R.id.promocode) as MaterialTextView
        val promoAmount = dialog.findViewById(R.id.promoAmount) as MaterialTextView
        promoCode.text = "'" + vm.promoCode.get() + "'"
        promoAmount.text = if (vm.promoModel.promoType == 2) "${
            try {
                "${vm.promoModel.percentage?.toDouble()?.toInt()}"
            } catch (e: NumberFormatException) {
                "${vm.promoModel.percentage}"
            }
        }%" else "" + vm.currency + vm.promoModel.amount
        val continueBtn = dialog.findViewById(R.id.continue_txt) as MaterialTextView
        continueBtn.setOnClickListener {
            dialog.dismiss()
            sendDetails()
            dismiss()
        }
        dialog.show()

    }

    override fun showNoPromo() {

    }

    override fun getTripType(): String {
        return tripType
    }

    override fun showSnackBar(message: String) {
        (requireActivity() as DrawerActivity).showSnackBar(message)
    }

    override fun isNetworkConnected(): Boolean {
        return (requireActivity() as DrawerActivity).isNetworkConnected()
    }

    override fun showCustomDialog(message: String) {}

    private fun setupAdapter() {
        adapter = PromoAdapter(ArrayList<PromoCodeModel.Promocode>(), vm.session, vm.mConnect, this)
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.promocodeRecycler.layoutManager = mLayoutManager
        binding.promocodeRecycler.itemAnimator = DefaultItemAnimator()
        binding.promocodeRecycler.adapter = adapter
        vm.getPromoList(tripType)
    }


    fun sendDetails() {
        val intent = Intent(Config.RECEIVE_PROMO_CODE)
        intent.putExtra(Config.isPromoApplied, vm.isPromoApplied.get())
        intent.putExtra(Config.RECEIVE_PROMO_CODE, vm.promoCode.get())
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    override fun getLayout() = R.layout.fragment_apply_promo

    override fun getBindingVariable() = BR.viewModel

    override fun getViewModel() = vm


}