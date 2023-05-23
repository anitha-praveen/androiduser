package com.cloneUser.client.drawer.confirmDestination

import androidx.navigation.fragment.findNavController

object ConfirmDestinationData {

    var pickupLat = ""
    var pickupLng = ""

    var drop1Lat = ""
    var drop1Lng = ""

    var drop2Lat = ""
    var drop2Lng = ""

    var pickAddress = ""
    var drop1Address = ""
    var drop2Address = ""

    var multipleDrop = false

    fun setToDefault() {

        pickupLat = ""
        pickupLng = ""

        drop1Lat = ""
        drop1Lng = ""

        drop2Lat = ""
        drop2Lng = ""

        pickAddress = ""
        drop1Address = ""
        drop2Address = ""

        multipleDrop = false
    }

}