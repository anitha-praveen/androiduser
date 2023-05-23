package com.cloneUser.client.drawer.adapters.suggestion_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.responseModels.SuggestionModel
import com.cloneUser.client.databinding.ItemSuggestionBinding
import com.cloneUser.client.drawer.suggestion.SuggestionNavigator

class SuggestionAdapter (private val suggestions : MutableList<SuggestionModel>, private val navigator: SuggestionNavigator) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var selectedSlug: String? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, suggestions, navigator,this)
    }


    override fun getItemCount(): Int {
        return suggestions.size
    }

    fun addList(complaints: List<SuggestionModel>) {
        this.suggestions.clear()
        this.suggestions.addAll(complaints)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ItemSuggestionBinding,
        private val complaints: MutableList<SuggestionModel>,
        private val navigator: SuggestionNavigator,
        private val adapter: SuggestionAdapter
    ) :
        BaseViewHolder(mBinding.root), SuggestionAdapterVM.AdapterViewModelListener {
        private var adapterVM: SuggestionAdapterVM? = null
        override fun onBind(position: Int) {
            complaints [position].isSelected = (complaints[position].slug == adapter.selectedSlug)
            val suggestion: SuggestionModel = complaints[position]
            adapterVM = SuggestionAdapterVM(suggestion, this)
            mBinding.viewModel = adapterVM
            mBinding.executePendingBindings()
        }

        override fun itemSelected(suggestion: SuggestionModel) {
            navigator.setSelected(suggestion)
            adapter.selectedSlug = suggestion.slug
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