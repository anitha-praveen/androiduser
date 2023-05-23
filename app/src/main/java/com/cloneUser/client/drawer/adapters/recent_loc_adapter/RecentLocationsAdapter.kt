package com.cloneUser.client.drawer.adapters.recent_loc_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.ItemRecentLocationsBinding
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationNavigator
import com.cloneUser.client.drawer.mapfragment.MapFragmentNavigator

class RecentLocationsAdapter(
    private val lastHistory: MutableList<FavPlace.LastTripHistory>,
    private val navigator: Any
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ItemRecentLocationsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, lastHistory, navigator)
    }


    override fun getItemCount(): Int {
        return lastHistory.size
    }

    fun addList(lastTrips: List<FavPlace.LastTripHistory>) {
        lastHistory.clear()
        lastHistory.addAll(lastTrips)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ItemRecentLocationsBinding,
        private val lastTrip: MutableList<FavPlace.LastTripHistory>,
        private val navigator: Any
    ) :
        BaseViewHolder(mBinding.root), ChildRecentLocVM.RecentLocItemListener {
        private var childVM: ChildRecentLocVM? = null
        override fun onBind(position: Int) {
            val lastTrip: FavPlace.LastTripHistory = lastTrip[position]
            childVM = ChildRecentLocVM(lastTrip, this)
            mBinding.viewModel = childVM
            mBinding.executePendingBindings()
        }

        override fun itemSelected(lastTrip: FavPlace.LastTripHistory) {
            if (navigator is ConfirmDestinationNavigator)
                navigator.setSelectedRecentLocation(lastTrip)
            else if (navigator is MapFragmentNavigator)
                navigator.setDropFromRecent(lastTrip)
        }

        override fun heartClicked(lastTrip: FavPlace.LastTripHistory) {
            if (navigator is MapFragmentNavigator)
                navigator.setFavDetails(lastTrip)
            else if (navigator is ConfirmDestinationNavigator)
                navigator.setFavDetails(lastTrip)
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

