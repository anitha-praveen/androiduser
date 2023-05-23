package com.cloneUser.client.drawer.adapters.search_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.ChildSearchItemBinding
import com.cloneUser.client.dialogs.addressEdit.AddressEditNavigator
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationNavigator
import com.cloneUser.client.drawer.profile.proAddFvourites.ProAddFavoNavigator
import com.cloneUser.client.drawer.tripAddressChange.TripAddressChangeNavigator


class SearchAdapter(
    private val favPlaceList: MutableList<FavPlace.Favourite>,
    private val navigator: Any
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ChildSearchItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, favPlaceList, navigator)
    }

    override fun getItemCount(): Int {
        return favPlaceList.size
    }

    fun addList(favPlace: List<FavPlace.Favourite>) {
        favPlaceList.clear()
        favPlaceList.addAll(favPlace)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ChildSearchItemBinding,
        private val favPlaceList: MutableList<FavPlace.Favourite>,
        private val navigator: Any
    ) :
        BaseViewHolder(mBinding.root), ChildSearchVM.ChildSearchPlaceItemListener {
        private var childSearchVM: ChildSearchVM? = null
        override fun onBind(position: Int) {
            val favPlace: FavPlace.Favourite = favPlaceList[position]
            childSearchVM = ChildSearchVM(
                favPlace,
                this,
                getDistance(favPlaceList[position].latitude, favPlaceList[position].longitude)
            )
            mBinding.viewModel = childSearchVM
            mBinding.executePendingBindings()
        }

        override fun itemSelected(place: FavPlace.Favourite) {
            when (navigator) {
                is ConfirmDestinationNavigator -> navigator.setSelectedSearchResult(place)
                is TripAddressChangeNavigator -> navigator.setSelectedSearchResult(place)
                is ProAddFavoNavigator -> navigator.setPlace(place)
                is AddressEditNavigator -> navigator.setSelectedSearchResult(place)
            }
        }

        fun getDistance(lat: String?, lng: String?): Float? {
            var distance: Float? = null
//            if(lat != null && lng != null){
//                val destination = Location("destination")
//                destination.latitude = lat.toDouble()
//                destination.longitude = lng.toDouble()
//
//                    when (navigator) {
//                        is SearchPlaceNavigator -> {
//                            distance = navigator.getCurrentLoc()?.distanceTo(destination)
//                            Log.e("Distance","$distance")
//                        }
//                        is ConfirmDestinationNavigator -> {
//                            distance = navigator.getCurrentLoc()?.distanceTo(destination)
//                            Log.e("Distance","$distance")
//                        }
//                        is TripAddressChangeNavigator -> {
//                        }
//                        is ProAddFavoNavigator -> {
//                        }
//                        is OutStationSearchNavigator -> {
//                        }
//                        is AddressEditNavigator -> {
//                        }
//                    }
//
//
//            }
            return distance
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