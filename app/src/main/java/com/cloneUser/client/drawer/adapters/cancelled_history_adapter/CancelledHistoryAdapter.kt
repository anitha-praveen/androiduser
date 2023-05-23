package com.cloneUser.client.drawer.adapters.cancelled_history_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.responseModels.HistoryModel
import com.cloneUser.client.databinding.ItemCancelledHistoryBinding
import com.cloneUser.client.databinding.PaginationLoadingBinding
import com.cloneUser.client.drawer.myRides.cancelled.CancelledHistoryNavigator

class CancelledHistoryAdapter (
    private val historyList: MutableList<HistoryModel.History>,
    private val navigator: CancelledHistoryNavigator) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var isLoadingAdded = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {

        return when (viewType) {
            0 -> {
                val binding = ItemCancelledHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                ChildViewHolder(binding, historyList, navigator)
            }
            1 -> {
                val paginationLoadingBinding = PaginationLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                PaginationViewHolder(paginationLoadingBinding, navigator)
            }
            else -> {
                val paginationLoadingBinding = PaginationLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                PaginationViewHolder(paginationLoadingBinding, navigator)
            }
        }

    }


    override fun getItemCount(): Int {
        return historyList.size
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(HistoryModel.History())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        val position: Int = historyList.size - 1
        if (historyList.size != 0){
            val result: HistoryModel.History ? = getItem(position)
            if (result != null) {
                historyList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    private fun getItem(position: Int?): HistoryModel.History ?{
        return if(position != null)
            historyList[position]
        else
            null
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == historyList.size - 1 && isLoadingAdded) 1 else 0
    }

    fun addList(mHistoryList: MutableList<HistoryModel.History>) {
        for (ff in mHistoryList) {
            add(ff)
        }
    }

    fun add(r: HistoryModel.History) {
        historyList.add(r)
        notifyItemInserted(historyList.size - 1)
    }


    private class PaginationViewHolder(
        paginationLoadingBinding: PaginationLoadingBinding,
        val navigator: CancelledHistoryNavigator
    ) :
        BaseViewHolder(paginationLoadingBinding.root) {
        override fun onBind(position: Int) {}

        override fun isNetworkConnected(): Boolean {
            return navigator.isNetworkConnected()
        }

        override fun showNetworkUnAvailable() {
        }

        override fun showCustomDialog(message: String) {
        }
    }


    class ChildViewHolder(
        private val mBinding: ItemCancelledHistoryBinding,
        private val historyList: MutableList<HistoryModel.History>,
        private val navigator: CancelledHistoryNavigator) : BaseViewHolder(mBinding.root), CancelledAdapterVM.CancelledItemListener {

        private var cancelledAdapterVM: CancelledAdapterVM? = null

        override fun onBind(position: Int) {
            val history: HistoryModel.History = historyList[position]
            cancelledAdapterVM = history.data?.let {
                CancelledAdapterVM(
                    it,navigator.getTranslation()
                )
            }
            mBinding.viewModel = cancelledAdapterVM
            mBinding.executePendingBindings()
        }



        override fun isNetworkConnected(): Boolean {
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