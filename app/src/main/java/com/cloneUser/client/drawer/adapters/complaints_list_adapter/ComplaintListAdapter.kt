package com.cloneUser.client.drawer.adapters.complaints_list_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.responseModels.ComplaintsModel
import com.cloneUser.client.databinding.ItemComplaintsBinding
import com.cloneUser.client.drawer.complaint.addComplaints.ComplaintNavigator

class ComplaintListAdapter(private val complaints : MutableList<ComplaintsModel> , private val navigator: ComplaintNavigator) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var selectedSlug: String? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ItemComplaintsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, complaints, navigator,this)
    }


    override fun getItemCount(): Int {
        return complaints.size
    }

    fun addList(complaints: List<ComplaintsModel>) {
        this.complaints.clear()
        this.complaints.addAll(complaints)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ItemComplaintsBinding,
        private val complaints: MutableList<ComplaintsModel>,
        private val navigator: ComplaintNavigator,
        private val adapter: ComplaintListAdapter
    ) :
        BaseViewHolder(mBinding.root), ComplaintListAdapterVM.AdapterViewModelListener {
        private var adapterVM: ComplaintListAdapterVM? = null
        override fun onBind(position: Int) {
            complaints [position].isSelected = (complaints[position].slug == adapter.selectedSlug)
            val complaint: ComplaintsModel = complaints[position]
            adapterVM = ComplaintListAdapterVM(complaint, this)
            mBinding.viewModel = adapterVM
            mBinding.executePendingBindings()
        }

        override fun itemSelected(complaint: ComplaintsModel) {
            navigator.setSelected(complaint)
            adapter.selectedSlug = complaint.slug
            adapter.notifyDataSetChanged()
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