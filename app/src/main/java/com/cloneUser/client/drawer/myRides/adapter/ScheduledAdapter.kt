package com.cloneUser.client.drawer.myRides.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.responseModels.HistoryModel
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.databinding.ItemHistoryBinding
import com.cloneUser.client.databinding.PaginationLoadingBinding
import com.cloneUser.client.drawer.myRides.cancelled.CancelledHistoryNavigator
import com.cloneUser.client.drawer.myRides.completed.CompletedHistoryNavigator
import com.cloneUser.client.drawer.myRides.scheduled.ScheduledHistoryNavigator

class ScheduledAdapter(
    private val historyList: MutableList<HistoryModel.History>,
    private val navigator: Any,
    private val mode: Int
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var isLoadingAdded = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {

        return when (viewType) {
            0 -> {
                val binding = ItemHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                ChildViewHolder(binding, historyList, navigator, mode,parent.context)
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
        val navigator: Any
    ) :
        BaseViewHolder(paginationLoadingBinding.root) {
        override fun onBind(position: Int) {}

        override fun isNetworkConnected(): Boolean {
            return when (navigator) {
                is ScheduledHistoryNavigator -> navigator.isNetworkConnected()
                is CompletedHistoryNavigator -> navigator.isNetworkConnected()
                else -> (navigator as CancelledHistoryNavigator).isNetworkConnected()
            }
        }

        override fun showNetworkUnAvailable() {
        }

        override fun showCustomDialog(message: String) {
        }
    }


    class ChildViewHolder(
        private val mBinding: ItemHistoryBinding,
        private val historyList: MutableList<HistoryModel.History>,
        private val navigator: Any,
        private val mode: Int,
        private val context:Context
    ) : BaseViewHolder(mBinding.root), ItemHistoryVM.HistoryItemListener {

        private var itemHistoryVM: ItemHistoryVM? = null

        override fun onBind(position: Int) {
            val history: HistoryModel.History = historyList[position]
            itemHistoryVM = history.data?.let {
                ItemHistoryVM(
                    it, this, when (navigator) {
                        is ScheduledHistoryNavigator -> navigator.getTranslation()
                        is CompletedHistoryNavigator -> navigator.getTranslation()
                        else -> (navigator as CancelledHistoryNavigator).getTranslation()
                    },context
                )
            }
            mBinding.viewModel = itemHistoryVM
            mBinding.executePendingBindings()
        }

        override fun itemSelected(history: RequestData.Data) {
            if(navigator is CompletedHistoryNavigator)
                navigator.invoice(history)
//            when (mode) {
//                0 /* scheduled */ -> (navigator as ScheduledHistoryNavigator).invoice(history)
//                1 /* completed */ -> (navigator as CompletedHistoryNavigator).invoice(history)
//                else /* cancelled */ -> (navigator as CancelledHistoryNavigator).invoice(history)
//            }
        }

        override fun onDisputeClicked(history: RequestData.Data) {
            if(mode==1)
                (navigator as CompletedHistoryNavigator).openDispute(history)
        }

        override fun cancel(history: RequestData.Data) {
            if(mode == 0) (navigator as ScheduledHistoryNavigator).cancelTrip(history.id?:"")
        }


        override fun isNetworkConnected(): Boolean {
            return when (mode) {
                0 /* scheduled */ -> (navigator as ScheduledHistoryNavigator).isNetworkConnected()
                1 /* completed */ -> (navigator as CompletedHistoryNavigator).isNetworkConnected()
                else /* cancelled */ -> (navigator as CancelledHistoryNavigator).isNetworkConnected()
            }
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