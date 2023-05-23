package com.cloneUser.client.drawer.invoice

import android.content.Context
import com.google.android.gms.maps.model.BitmapDescriptor
import com.cloneUser.client.base.BaseViewOperator

interface InvoiceNavigator : BaseViewOperator {
    fun chooseDirection()
    fun getCtx() : Context
    fun getMarkerIcon(icPickPin: Int): BitmapDescriptor?
    fun close()
    fun gotoComplaints()
}