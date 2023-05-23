package com.cloneUser.client.drawer.adapters.rentalTypesAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.responseModels.RentalPackageTypes
import com.cloneUser.client.databinding.ItemRentalTypeBinding
import com.cloneUser.client.drawer.adapters.rentalTypesAdapter.RentalTypesAdapterVM
import com.cloneUser.client.drawer.rental.RentalNavigator

class RentalTypesAdapter(
    private val typesList: MutableList<RentalPackageTypes.Type>,
    private val navigator: RentalNavigator
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    //    var selectedSlug: String? = ""
    var slugSelected: String? = null
    var eta = ""

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ItemRentalTypeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, typesList, navigator, this)
    }


    override fun getItemCount(): Int {
        return typesList.size
    }

    fun addList(packagesList: List<RentalPackageTypes.Type>,slug:String?) {
        typesList.clear()
        slugSelected = slug
        typesList.addAll(packagesList)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ItemRentalTypeBinding,
        private val list: MutableList<RentalPackageTypes.Type>,
        private val navigator: RentalNavigator,
        private val adapter: RentalTypesAdapter
    ) :
        BaseViewHolder(mBinding.root), RentalTypesAdapterVM.RentalTypesAdapterItemListener {
        private var childVM: RentalTypesAdapterVM? = null
        override fun onBind(position: Int) {
//            if(adapter.selectedSlug ==null){
//                adapter.selectedSlug = list[position].slug
//            }

            if (adapter.slugSelected == null) {
                adapter.slugSelected = list[position].vehicle?.slug

            }

//            list[position].eta =
//                if (adapter.selectedSlug == list[position].slug) adapter.eta else ""


            list[position].eta =
                if (adapter.slugSelected == list[position].vehicle?.slug) adapter.eta else ""

//            list[position].isSelected = (adapter.selectedSlug == list[position].slug)
            list[position].isSelected = (adapter.slugSelected == list[position].vehicle?.slug)
            val type: RentalPackageTypes.Type = list[position]
            childVM = RentalTypesAdapterVM(type, this)
            mBinding.viewModel = childVM
            mBinding.executePendingBindings()
        }


        override fun itemSelected(type: RentalPackageTypes.Type) {
//            adapter.selectedSlug = type.slug
            adapter.slugSelected = type.vehicle?.slug
            navigator.setSelectedType(type,type.vehicle?.slug)
            adapter.notifyDataSetChanged()
        }


        override fun isNetworkConnected(): Boolean {
            /*
            This method is not used in this adapter
             */
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

    fun setETA(eta: String) {
        this.eta = eta
        notifyDataSetChanged()
    }

}