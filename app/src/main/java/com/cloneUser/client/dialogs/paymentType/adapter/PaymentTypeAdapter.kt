package com.cloneUser.client.dialogs.paymentType.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.databinding.ChildPaymentTypeBinding
import com.cloneUser.client.dialogs.paymentType.PaymentTypeNavigator

class PaymentTypeAdapter(
    private val paymentList: MutableList<String>,
    private val navigator: PaymentTypeNavigator,
   private val previousSelection : String) :
    RecyclerView.Adapter<BaseViewHolder>() {

    private var selectedItem = -1

    init {
        paymentList.forEachIndexed { index, element ->
            if(element == previousSelection)
                selectedItem = index
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ChildPaymentTypeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, paymentList,this,navigator)
    }


    override fun getItemCount(): Int {
        return paymentList.size
    }

    fun addList(promoList: List<String>) {
        paymentList.clear()
        paymentList.addAll(promoList)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ChildPaymentTypeBinding,
        private val payTypeList: MutableList<String>,
        private val paymentTypeAdapter: PaymentTypeAdapter,
        private val navigator: PaymentTypeNavigator
    ) : BaseViewHolder(mBinding.root), ChildPaymentTypeVM.ChildPayTypeItemListener {

        private var childPaymentTypeVM: ChildPaymentTypeVM? = null

        override fun onBind(position: Int) {
            val payType: String = payTypeList[position]
            childPaymentTypeVM = ChildPaymentTypeVM(payType, paymentTypeAdapter.selectedItem == position,this)
            mBinding.viewModel = childPaymentTypeVM
            mBinding.executePendingBindings()
        }



        override fun itemSelected(payTypes: String) {
            paymentTypeAdapter.selectedItem = adapterPosition
            paymentTypeAdapter.notifyDataSetChanged()
            navigator.setPaymentMethod(payTypes)

        }


        override fun isNetworkConnected(): Boolean {
            /*
            This method is not used in this adapter
             */
            return false
        }

        override fun showNetworkUnAvailable() {

        }

        override fun showCustomDialog(message: String) {

        }


    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

}