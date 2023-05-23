package com.cloneUser.client.drawer.sos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.responseModels.SosModel
import com.cloneUser.client.databinding.ChildSosBinding
import com.cloneUser.client.drawer.sos.SosNavigator


class SosAdapter(
    private val sosModelList: MutableList<SosModel.Sos>,
    private val navigator: SosNavigator
) :
    RecyclerView.Adapter<BaseViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ChildSosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, sosModelList, navigator)
    }


    override fun getItemCount(): Int {
        return sosModelList.size
    }

    fun addList(sosModelList: List<SosModel.Sos>) {
        this.sosModelList.clear()
        this.sosModelList.addAll(sosModelList)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ChildSosBinding,
        private val sosModelList: MutableList<SosModel.Sos>,
        private val navigator: SosNavigator
    ) : BaseViewHolder(mBinding.root), ChildSosVM.ChildSosItemListener {

        private var childSosVM: ChildSosVM? = null

        override fun onBind(position: Int) {

            val sosModel: SosModel.Sos = sosModelList[position]
            childSosVM = ChildSosVM(sosModel, this)
            mBinding.viewModel = childSosVM
            mBinding.executePendingBindings()
        }

        override fun itemSelected(sos: String) {
            navigator.onPhoneClick(sos)
        }

        override fun itemDeleted(slug: String) {
            navigator.deleteSosNav(slug)

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