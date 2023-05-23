package com.cloneUser.client.drawer.rideConfirm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.responseModels.TypesModel
import com.cloneUser.client.databinding.ChildTypesBinding
import com.cloneUser.client.drawer.rideConfirm.RideConfirmNavigator
import com.cloneUser.client.ut.SessionMaintainence


class TypesAdapter(
    private val typesList: MutableList<TypesModel.ZoneTypePrice>,
    private val session: SessionMaintainence,
    private val mConnect: ConnectionHelper,
    private val navigator: RideConfirmNavigator
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var selectedSlug: String? = null
    var eta = ""

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ):  ChildViewHolder {
        val binding = ChildTypesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, typesList, navigator, this)
    }


    override fun getItemCount(): Int {
        return typesList.size
    }

    fun addList(typeList: List<TypesModel.ZoneTypePrice>) {
        typesList.clear()
        typesList.addAll(typeList)
        notifyDataSetChanged()
    }

    fun setETA(eta: String) {
        this.eta = eta
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ChildTypesBinding,
        private val typesList: MutableList<TypesModel.ZoneTypePrice>,
        private val navigator: RideConfirmNavigator,
        private val adapter: TypesAdapter
    ) : BaseViewHolder(mBinding.root), ChildTypesVM.ChildTypesItemListener {

        private var childTypesVM: ChildTypesVM? = null


        override fun onBind(position: Int) {
            if (adapter.selectedSlug == null) {
                adapter.selectedSlug = typesList[position].typeSlug
            }
            typesList[position].isselected = (typesList[position].typeSlug == adapter.selectedSlug)
            typesList[position].eta =
                if (adapter.selectedSlug == typesList[position].typeSlug) adapter.eta else ""
            val types: TypesModel.ZoneTypePrice = typesList[position]
            childTypesVM = ChildTypesVM(mBinding, types, this)
            mBinding.viewModel = childTypesVM
            mBinding.executePendingBindings()
        }

        override fun itemSelected(zoneTypePrice: TypesModel.ZoneTypePrice) {
            adapter.selectedSlug = zoneTypePrice.typeSlug
            navigator.selectedSlug(zoneTypePrice)
            adapter.notifyDataSetChanged()
        }


        override fun isNetworkConnected(): Boolean {
            /*
            This method is not used in this adapter
             */
            return navigator.isNetworkConnected()
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