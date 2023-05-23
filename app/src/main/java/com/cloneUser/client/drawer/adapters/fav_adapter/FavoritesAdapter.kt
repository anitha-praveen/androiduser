package com.cloneUser.client.drawer.adapters.fav_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.ChildFavPlaceBinding
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationNavigator
import com.cloneUser.client.drawer.favorites.listFavorite.FavoriteNavigator
import com.cloneUser.client.drawer.mapfragment.MapFragmentNavigator
import com.cloneUser.client.drawer.profile.ProfileNavigator
import com.cloneUser.client.drawer.tripAddressChange.TripAddressChangeNavigator

class FavoritesAdapter(
    private val favPlaceList: MutableList<FavPlace.Favourite>,
    private val navigator: Any,
    private val mode:Int
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ChildFavPlaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, favPlaceList, navigator,mode)
    }


    override fun getItemCount(): Int {
        return if(navigator is MapFragmentNavigator){
            if(favPlaceList.size>2) 2
            else favPlaceList.size
        }else
            favPlaceList.size
    }


    fun addList(favPlace: List<FavPlace.Favourite>) {
        favPlaceList.clear()
        favPlaceList.addAll(favPlace)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ChildFavPlaceBinding,
        private val favPlaceList: MutableList<FavPlace.Favourite>,
        private val navigator: Any,
        private val mode:Int
    ) :
        BaseViewHolder(mBinding.root), ChildFavoritePlaceVM.ChildSearchPlaceItemListener {
        private var childPlaceFavViewModel: ChildFavoritePlaceVM? = null
        override fun onBind(position: Int) {
            val favPlace: FavPlace.Favourite = favPlaceList[position]
            childPlaceFavViewModel = ChildFavoritePlaceVM(favPlace, this, mode)
            mBinding.viewModel = childPlaceFavViewModel
            mBinding.executePendingBindings()
        }

        override fun itemSelected(favPlace: FavPlace.Favourite) {
            when (navigator) {
                is ConfirmDestinationNavigator -> navigator.setSelectedFavPlace(favPlace)
                is MapFragmentNavigator -> navigator.setDropFromFavorites(favPlace)
            }
        }

        override fun itemDeleted(favPlace: FavPlace.Favourite) {
            if (navigator is ProfileNavigator)
                navigator.deleteFavorite(favPlace)
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