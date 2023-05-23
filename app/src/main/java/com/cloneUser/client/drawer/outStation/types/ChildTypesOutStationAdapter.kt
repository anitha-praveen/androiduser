package com.cloneUser.client.drawer.outStation.types

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.OutStationTypes
import com.cloneUser.client.databinding.OutstationChildTypesBinding
import com.cloneUser.client.drawer.outStation.OutStationNavigator

class ChildTypesOutStationAdapter(
    private val outStationTypesList: MutableList<OutStationTypes>,
    private val navigator: OutStationNavigator,
    private val translationModel: TranslationModel
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    var id:Int? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = OutstationChildTypesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding,outStationTypesList,navigator,this,translationModel)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return outStationTypesList.size
    }
    var isTwoWaySelect = false
    fun addList(typesmodel: MutableList<OutStationTypes> , isTwoWay : Boolean) {
        isTwoWaySelect = isTwoWay
        outStationTypesList.clear()
        outStationTypesList.addAll(typesmodel)
        notifyDataSetChanged()
    }

    class ChildViewHolder(
        private val mBinding: OutstationChildTypesBinding,
        private val outStationTypesModelList: MutableList<OutStationTypes>,
        private val navigator: OutStationNavigator,
        private val adapter:ChildTypesOutStationAdapter,
        private val translationModel: TranslationModel
    ) : BaseViewHolder(mBinding.root), ChildTypesOutStationVm.ChildOutStationTypesItemListener {

        private var childOutstationTypesListVm: ChildTypesOutStationVm? = null

        override fun onBind(position: Int) {
            if (adapter.id == null){
                adapter.id = outStationTypesModelList[position].id
            }
            outStationTypesModelList[position].isSelected = (outStationTypesModelList[position].id == adapter.id)
            val outstationTypesModel: OutStationTypes = outStationTypesModelList[position]
            childOutstationTypesListVm = ChildTypesOutStationVm(outstationTypesModel, this,translationModel , adapter.isTwoWaySelect)
            mBinding.viewModel = childOutstationTypesListVm
            mBinding.executePendingBindings()
        }

        override fun itemSelected(model:OutStationTypes) {
            adapter.id = model.id
            navigator.typesSelected(model)
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
}
