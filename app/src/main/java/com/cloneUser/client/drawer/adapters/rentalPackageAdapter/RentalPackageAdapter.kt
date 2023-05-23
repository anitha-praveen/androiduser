package com.cloneUser.client.drawer.adapters.rentalPackageAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.responseModels.PackageModel
import com.cloneUser.client.databinding.ItemPackgeBinding
import com.cloneUser.client.drawer.rental.RentalNavigator

class RentalPackageAdapter (
    private val packageList: MutableList<PackageModel.Package>,
    private val navigator: RentalNavigator
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    var selectedSlug: String? = ""

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = ItemPackgeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding, packageList, navigator,this)
    }


    override fun getItemCount(): Int {
        return packageList.size
    }

    fun addList(packagesList: List<PackageModel.Package>) {
        packageList.clear()
        packageList.addAll(packagesList)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: ItemPackgeBinding,
        private val pkgList: MutableList<PackageModel.Package>,
        private val navigator: RentalNavigator,
        private val adapter: RentalPackageAdapter
    ) :
        BaseViewHolder(mBinding.root), RentalPackageAdapterVM.RentalPackageAdapterListener {
        private var childVM: RentalPackageAdapterVM? = null
        override fun onBind(position: Int) {
            if (adapter.selectedSlug.isNullOrEmpty())
                adapter.selectedSlug = pkgList[0].slug
            pkgList[position].isSelected = (adapter.selectedSlug == pkgList[position].slug)
            val pkg: PackageModel.Package = pkgList[position]
            childVM = RentalPackageAdapterVM(pkg, this)
            mBinding.viewModel = childVM
            mBinding.executePendingBindings()
        }

        override fun itemSelected(pkg: PackageModel.Package) {
            adapter.selectedSlug = pkg.slug
            navigator.setSelectedPackage(pkg)
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