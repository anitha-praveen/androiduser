package com.cloneUser.client.drawer.rental

object RentalData {
    var pickLat: Double = 0.0
    var pickLng: Double = 0.0
    var pickAddress = ""
    var flag = false

    fun reset(){
        pickLat = 0.0
        pickLng = 0.0
        pickAddress = ""
        flag = false
    }
}