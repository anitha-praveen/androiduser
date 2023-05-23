package com.cloneUser.client.dialogs.cancelReasons.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.responseModels.CancelReason
import com.cloneUser.client.databinding.ChildCancelReasonBinding
import com.cloneUser.client.dialogs.cancelReasons.CancelReasonsNavigator
import com.cloneUser.client.ut.SessionMaintainence

class CancelReasonsAdapter (
    private val reasonList: MutableList<CancelReason.Reason>,
    private val session: SessionMaintainence,
    private val mConnect: ConnectionHelper,
    private val navigator: CancelReasonsNavigator
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    private var selectedItem = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ChildCancelReasonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, reasonList,this)
    }


    override fun getItemCount(): Int {
        return reasonList.size
    }

    fun addList(cancelList: List<CancelReason.Reason>) {
        reasonList.clear()
        reasonList.addAll(cancelList)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ChildCancelReasonBinding,
        private val reasonList: MutableList<CancelReason.Reason>,
        private val cancelAdapter: CancelReasonsAdapter
    ) : BaseViewHolder(mBinding.root), ChildCancelVM.ChildCancelItemListener {

        private var childVM: ChildCancelVM? = null

        override fun onBind(position: Int) {
            val reason: CancelReason.Reason = reasonList[position]
            childVM = ChildCancelVM(reason, cancelAdapter.selectedItem == position,this)
            mBinding.viewModel = childVM
            mBinding.executePendingBindings()
        }



        override fun itemSelected(reason: CancelReason.Reason) {
            cancelAdapter.selectedItem = adapterPosition
            cancelAdapter.notifyDataSetChanged()
            cancelAdapter.navigator.setCancelReason(reason)
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