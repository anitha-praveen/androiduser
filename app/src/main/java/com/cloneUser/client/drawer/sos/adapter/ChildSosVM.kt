package com.cloneUser.client.drawer.sos.adapter

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.SosModel

class ChildSosVM(
    private val sosModel: SosModel.Sos,
    val adapterLister: ChildSosItemListener,
) {
    var title = sosModel.title
    var number = sosModel.phoneNumber
    var showDelete = sosModel.createdBy != null
    fun onItemSelected() {
        adapterLister.itemSelected(number!!)
    }

    fun onItemDeleted(){
        adapterLister.itemDeleted("${sosModel.slug}")
    }


    interface ChildSosItemListener : BaseViewOperator {
        fun itemSelected(sos: String)
        fun itemDeleted(slug:String)

    }




}
