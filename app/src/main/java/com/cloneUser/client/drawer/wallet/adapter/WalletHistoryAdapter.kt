package com.cloneUser.client.drawer.wallet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.R
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.responseModels.WalletResponsModel

class WalletHistoryAdapter (private val historyList:ArrayList<WalletResponsModel>,
                            private val currType: BaseResponse.DataObjectsAllApi): RecyclerView.Adapter<WalletHistoryAdapter.ViewModel>() {
    inner class ViewModel(view: View): RecyclerView.ViewHolder(view) {
        val typeTv: TextView = view.findViewById(R.id.type_wallet_helper)
        val totalAmountTv: TextView = view.findViewById(R.id.total_amount_wallet_helper)
        val createdAt: TextView = view.findViewById(R.id.created_at_wallet_helper)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WalletHistoryAdapter.ViewModel {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.wallet_histroy_list_helper, parent, false)
        return ViewModel(itemView)
    }

    override fun onBindViewHolder(holder: WalletHistoryAdapter.ViewModel, position: Int) {
        holder.typeTv.text = historyList[position].type
        holder.totalAmountTv.text = "${currType.currency} ${historyList[position].amount}"
        holder.createdAt.text = historyList[position].createdAt
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

}