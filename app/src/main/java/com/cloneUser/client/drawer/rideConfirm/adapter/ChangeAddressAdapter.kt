package com.cloneUser.client.drawer.rideConfirm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.RideChildSearchVmBinding
import com.cloneUser.client.drawer.rideConfirm.changeAddress.RideConfirmChangeNavigator

class ChangeAddressAdapter
    (
    private val favPlaceList: MutableList<FavPlace.Favourite>,
    private val navigator: RideConfirmChangeNavigator
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = RideChildSearchVmBinding.inflate(
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
        private val mBinding: RideChildSearchVmBinding,
        private val favPlaceList: MutableList<FavPlace.Favourite>,
        private val navigator: RideConfirmChangeNavigator
    ) :
        BaseViewHolder(mBinding.root), RideChildSearchVm.ChildSearchPlaceItemListener {
        private var childSearchVM: RideChildSearchVm? = null
        override fun onBind(position: Int) {
            val favPlace: FavPlace.Favourite = favPlaceList[position]
            childSearchVM = RideChildSearchVm(
                favPlace,
                this,
            )
            mBinding.viewModel = childSearchVM
            mBinding.executePendingBindings()
        }

        override fun itemSelected(place: FavPlace.Favourite) {
            navigator.onItemSelected(place)
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