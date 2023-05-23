package com.cloneUser.client.drawer.applyPromo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.responseModels.PromoCodeModel
import com.cloneUser.client.databinding.ChildPromoBinding
import com.cloneUser.client.drawer.applyPromo.ApplyPromoNavigator
import com.cloneUser.client.ut.SessionMaintainence

class PromoAdapter(
    private val promoList: MutableList<PromoCodeModel.Promocode>,
    private val session: SessionMaintainence,
    private val mConnect: ConnectionHelper,
    private val navigator: ApplyPromoNavigator
) :
    RecyclerView.Adapter<BaseViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ChildPromoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, promoList, session, mConnect, navigator)
    }


    override fun getItemCount(): Int {
        return promoList.size
    }

    fun addList(typeList: List<PromoCodeModel.Promocode>) {
        promoList.clear()
        promoList.addAll(typeList)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ChildPromoBinding,
        private val promoList: MutableList<PromoCodeModel.Promocode>,
        private val session: SessionMaintainence,
        private val mConnect: ConnectionHelper,
        private val navigator: ApplyPromoNavigator
    ) : BaseViewHolder(mBinding.root), ChildPromoVM.ChildPromoItemListener {

        private var childPromoVM: ChildPromoVM? = null
        var selectedSlug: String? = null

        override fun onBind(position: Int) {
            val promos: PromoCodeModel.Promocode = promoList[position]
            childPromoVM = ChildPromoVM(promos, this, session, mConnect)
            mBinding.viewModel = childPromoVM
            mBinding.executePendingBindings()
        }





        override fun itemSelected(promo: PromoCodeModel.Promocode) {
            navigator.onPromoSelected(promo)
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