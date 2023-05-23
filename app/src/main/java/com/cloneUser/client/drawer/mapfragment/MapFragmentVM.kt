package com.cloneUser.client.drawer.mapfragment

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseVM
import com.cloneUser.client.connection.*
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.FirebaseHelper
import com.cloneUser.client.ut.SessionMaintainence
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class MapFragmentVM @Inject constructor(
    val application: Application,
    val session: SessionMaintainence,
    val mConnect: ConnectionHelper,
    private val mapsHelper: MapsHelper
) :
    BaseVM<BaseResponse, MapFragmentNavigator>(session, mConnect), FirebaseHelper.FirebaseObserver {
    companion object {
        const val TAG = "MapFragmentVM"
    }

    var address = ObservableField("")
    var isLocationAvailable = ObservableBoolean(false)
    var isAskingConfirmLocation = ObservableBoolean(false)
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var vmLatLng = ObservableField<LatLng>()
    val zoom = 19f
    var driverPins = HashMap<String, Marker>()
    var driverDatas = HashMap<String, String>()
    var bitmapDescriptorFactory = BitmapDescriptorFactory.fromResource(R.drawable.car_icon_us)
    var markeroption = MarkerOptions()
    var firebaseHelper: FirebaseHelper? = null
    var userDragged = ObservableBoolean(false)
    var requestId = ""
    var hashMap = HashMap<String, String>()
    var apiCode = -1
    val isinsideZoneView = ObservableBoolean(true)
    val isInsideZone = ObservableBoolean(true)
    var favPlaces:FavPlace.Favourite? = null

    /*
    calledFrom 1-> when confirm button pressed
     */
    var calledFrom = 0

    /*
    If apiCode = 0 -> tripAddressChangeApi
                 1 -> checkZone api
                 2 -> favorite places api call
                 3 -> addFavorite api call
                 4 -> check outstation
     */
    var isDialySelected = ObservableBoolean(true)
    var isRentalSelected = ObservableBoolean(false)
    var isOutstationSelected = ObservableBoolean(false)
    var haveFavorites = ObservableBoolean(false)
    var haveRecentLocations = ObservableBoolean(false)

    override fun onSuccessfulApi(response: BaseResponse?) {
        isLoading.value = false
        if (apiCode == 0)
            getNavigator().callRequestInProgressFromDrawer()
        else if (apiCode == 1) {
            if (response != null) {
                val data = Gson().toJson(response.data)
                val obj = Gson().fromJson(data, BaseResponse.DataObjectsAllApi::class.java)
                obj.zone?.let { isInsideZone.set(it) }
                if (isInsideZone.get()) {
                    if (calledFrom == 1) {
                        queryDrivers()
                        isinsideZoneView.set(true)
                    } else {
                        isinsideZoneView.set(true)
                        getNavigator().enableScrolling()
                    }
                    getNavigator().setupAdapter()
                } else {
                    if (calledFrom == 1) {
                        calledFrom = 0
                        getNavigator().showMessage("Selected place is out of zone")
                        isinsideZoneView.set(false)
                    } else {
                        isinsideZoneView.set(false)
                        getNavigator().avoidScrolling()
                    }
                }
            }
        } else if (apiCode == 2 && response != null) {
            val fav: FavPlace? = Gson().fromJson(Gson().toJson(response.data), FavPlace::class.java)
            if (fav?.favouriteList?.isNullOrEmpty() == false) {
                haveFavorites.set(true)
                getNavigator().loadFavoriteLocations(fav.favouriteList)
            } else haveFavorites.set(false)
            if (fav?.lastTripHistory?.isNullOrEmpty() == false) {
                getNavigator().loadRecentLocations(fav.lastTripHistory)
                haveRecentLocations.set(true)
            } else {
                haveRecentLocations.set(false)
            }
        } else if (apiCode == 3 && response != null) {
            getFavoritePlaceApiCall()
        }
        else
            favPlaces?.let { getNavigator().goTorideConfirmFragement(it) }

    }

    override fun onFailureApi(call: Call<BaseResponse?>?, e: CustomException) {
        isLoading.value = false
        getNavigator().showCustomDialog(e.exception!!)
    }


    fun onClickPickLocation() {
        if (isLoading.value == false)
            getNavigator().openPickupEdit()
    }


    fun onClickCurrentLocation() {
        if (isLoading.value == false)
            viewModelScope.launch {
                getNavigator().getCtx()?.let { getLastLocation(it) }
            }

    }

    fun chooseDestination() {
        getNavigator().goToConfirmDestination()
    }

    fun getLastLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                isLocationAvailable.set(true)
                session.saveString(SessionMaintainence.CURRENT_LATITUDE, "" + location.latitude)
                session.saveString(
                    SessionMaintainence.CURRENT_LONGITUDE,
                    "" + location.longitude
                )
                vmLatLng.set(LatLng(location.latitude, location.longitude))
                checkZone(vmLatLng.get()?.latitude.toString(), vmLatLng.get()?.longitude.toString())
                getAddressFromLatLng()
                moveCamera(getNavigator().getGMap(), vmLatLng.get(), zoom)
            } else {
                getNavigator().getFrgAct()?.let { requestLocationUpdates(it) }
            }
        }

    }

    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (location != null) {
                    vmLatLng.set(LatLng(location.latitude, location.longitude))
                    stopCallBack()
                }
            }
            if (vmLatLng.get() != null) {
                isLocationAvailable.set(true)
                session.saveString(
                    SessionMaintainence.CURRENT_LATITUDE,
                    "" + vmLatLng.get()!!.latitude
                )
                session.saveString(
                    SessionMaintainence.CURRENT_LONGITUDE,
                    "" + vmLatLng.get()!!.longitude
                )
                checkZone(vmLatLng.get()?.latitude.toString(), vmLatLng.get()?.longitude.toString())
                getAddressFromLatLng()
                moveCamera(getNavigator().getGMap(), vmLatLng.get(), zoom)
            }
        }
    }

    private fun stopCallBack() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
    }


    var mLocationRequest: LocationRequest? = null
    fun requestLocationUpdates(requireActivity: FragmentActivity) {
        mLocationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            isWaitForAccurateLocation = true
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
        if (ContextCompat.checkSelfPermission(
                requireActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                mLocationRequest!!,
                mLocationCallback,
                Looper.myLooper()!!
            )
        }
    }


    /** gets address for the given [LatLng]  */
    fun getAddressFromLatLng() {
        if (Geocoder.isPresent()) {
            try {
                val geocoder = getNavigator().getCtx()?.let { Geocoder(it) }
                var mAddress = ""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder?.getFromLocation(
                        vmLatLng.get()!!.latitude,
                        vmLatLng.get()!!.longitude,
                        1
                    ) { addresses ->
                        if (addresses.size > 0) {
                            if (addresses[0].getAddressLine(0) != null) {
                                mAddress = addresses[0].getAddressLine(0)
                            }
                            if (addresses[0].getAddressLine(1) != null) {
                                mAddress = mAddress + ", " + addresses[0].getAddressLine(1)
                            }
                            if (addresses[0].getAddressLine(2) != null) {
                                mAddress = mAddress + ", " + addresses[0].getAddressLine(2)
                            }
                        }
                        address.set(mAddress)  }
                }
                else {
                    val addresses = geocoder?.getFromLocation(vmLatLng.get()!!.latitude,
                        vmLatLng.get()!!.longitude,
                        1)
                    if (addresses != null && addresses.size > 0) {
                        if (addresses[0].getAddressLine(0) != null) {
                            mAddress = addresses[0].getAddressLine(0)
                        }
                        if (addresses[0].getAddressLine(1) != null) {
                            mAddress = mAddress + ", " + addresses[0].getAddressLine(1)
                        }
                        if (addresses[0].getAddressLine(2) != null) {
                            mAddress = mAddress + ", " + addresses[0].getAddressLine(2)
                        }
                    }
                }
                address.set(
                    mAddress
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            mapsHelper.GetAddressFromLatLng(
                vmLatLng.get()!!.latitude.toString() + "," + vmLatLng.get()!!.longitude,
                false, session.getString(SessionMaintainence.GEOCODE_DYNAMIC_KEY)
            )!!.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    if (response.isSuccessful) {
                        if (response.body() != null && response.body()!!
                                .getAsJsonArray("results") != null
                        ) {
                            val status = response.body()!!["status"].asString
                            if (status == "OK") {
                                address.set(
                                    response.body()!!
                                        .getAsJsonArray("results")[0].asJsonObject["formatted_address"].asString
                                )
                            } else if (status == "OVER_QUERY_LIMIT") {
                                printE(MapFragment.TAG, "QUERY OVER LIMIT")
                            }
                        }
                    } else {
                        Log.d(TAG, "GetAddressFromLatlng$response")
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Log.d(TAG, "GetAddressFromLatlng$t")
                }
            })
        }


    }

    fun onClickOutstation() {
        if (isLoading.value == false){
            isDialySelected.set(false)
            isRentalSelected.set(false)
            isOutstationSelected.set(true)
            getNavigator().getCtx()?.let {
                val bold = ResourcesCompat.getFont(it, R.font.roboto_bold)
                val regular = ResourcesCompat.getFont(it, R.font.roboto_regular)
                getNavigator().getBinds().dailyText.typeface = regular
                getNavigator().getBinds().rentalText.typeface = regular
                getNavigator().getBinds().outstationText.typeface = bold
            }
            getNavigator().openOutstation()
        }
    }

    fun onClickConfirmPickup() {
        if (isLoading.value == false){
            isAskingConfirmLocation.set(false)
            if (getNavigator().checkIsTripAddressChange()) {
                apiCode = 0
                hashMap.clear()
                hashMap[Config.request_id] = requestId
                hashMap[Config.latitude] = "${vmLatLng.get()!!.latitude}"
                hashMap[Config.longitude] = "${vmLatLng.get()!!.longitude}"
                hashMap[Config.address] = address.get().toString()
                hashMap[Config.type] = "PICKUP"
                changeLocationBase(hashMap)
            } else {
                calledFrom = 1
                checkZone(vmLatLng.get()?.latitude.toString(), vmLatLng.get()?.longitude.toString())
            }
        }
    }

    fun onClickFav() {
//        getNavigator().openFavBottomSheet()
    }


    //converting vector to bitmap
    private fun bitMapFromVector(vectorResID: Int): BitmapDescriptor {
        val vectorDrawable = getNavigator().getCtx()
            ?.let { ContextCompat.getDrawable(it, vectorResID) }
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    var driversLocations: HashMap<String, GeoLocation> = HashMap()
    private val driverImages: HashMap<String, Marker> = HashMap()

    override fun driverEnteredFence(key: String?, location: GeoLocation?, response: String?) {
        Log.e("Fire", "driverEnteredFence ${key}")
        activeInactiveDrivers(key!!, location!!, response!!)
    }

    override fun driverExitedFence(key: String?, response: String?) {
        try {
            if (driverImages.containsKey(key)) {
                val driverPin = driverImages[key]
                driverPin!!.remove()
                driversLocations.remove(key)
                driverImages.remove(key)
                driverDatas.remove(key)
                firebaseHelper?.removeObserverFor(key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun driverMovesInFence(key: String?, location: GeoLocation?, response: String?) {}

    override fun driverWentOffline(key: String?) {
        Log.e("Fire", "Driver went offline ${key}")
        try {
            if (driverImages.containsKey(key)) {
                val driverPin = driverImages[key]
                driverPin!!.remove()
                driverImages.remove(key)
                driverDatas.remove(key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun driverDataUpdated(key: String?, latLng: LatLng, response: String?) {
        Log.e("Fire", "driverDataUpdated ${key}")
        val location = GeoLocation(latLng.latitude, latLng.longitude)
        if (!driverImages.containsKey(key)) {
            Log.e("driverUpdated---", "new driver : $key")
            activeInactiveDrivers(key!!, location, response!!)
        }
    }


    private fun activeInactiveDrivers(key: String, location: GeoLocation, response: String) {
        try {
            val jsonObject = JSONObject(response)
            if (jsonObject.has("is_active")) {
                val isActive = jsonObject.getBoolean("is_active")
                if (isActive) {
                    val updatedAt = jsonObject.getLong("updated_at")
                    val currentTime = Date().time
                    val diff = (currentTime - updatedAt) / 1000
                    if (diff < 5 * 60) {
                        /*here driver passed all conditions
                          check whether he is already in our list
                          If he is already in our list -> update his data , location and remove his marker and show marker with updated data
                          else add his data , location and remove his marker and show marker with updated data
                         */
                        val bearing = jsonObject.getDouble("bearing")
                        if (driverDatas.containsKey(key) && driversLocations.containsKey(key)) {
                            /* driver is already in our data
                               so update his data , location and remove his marker and show marker with updated data
                             */
                            driverDatas[key] = response
                            driversLocations[key] = location

                            if (driverImages.containsKey(key)) {
                                val driverPin = driverImages[key]
                                driverPin!!.remove()
                                driverImages.remove(key)
                            }

                            if (jsonObject.has("type") && jsonObject.getString("type")
                                    .equals("bajaj-auto")
                            ) {
                                bitmapDescriptorFactory =
                                    BitmapDescriptorFactory.fromResource(R.drawable.roda_auto)

                                markeroption.position(
                                    LatLng(
                                        location.latitude,
                                        location.longitude
                                    )
                                )
                                    .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                    .icon(bitmapDescriptorFactory)
                                driverImages[key] =
                                    getNavigator().getGMap()!!.addMarker(markeroption)!!
                            } else {
//                                bitmapDescriptorFactory =
//                                    BitmapDescriptorFactory.fromResource(R.drawable.new_car)
                                markeroption.position(LatLng(location.latitude, location.longitude))
                                    .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                    .icon(bitMapFromVector(R.drawable.car_icon_us))
                                driverImages[key] =
                                    getNavigator().getGMap()!!.addMarker(markeroption)!!
                            }
                        } else {
                            /* driver data is not present ,so make new */
                            driverDatas[key] = response
                            driversLocations[key] = location

                            if (jsonObject.has("type") && jsonObject.getString("type")
                                    .equals("bajaj-auto")
                            ) {
                                bitmapDescriptorFactory =
                                    BitmapDescriptorFactory.fromResource(R.drawable.roda_auto)

                                markeroption.position(
                                    LatLng(
                                        location.latitude,
                                        location.longitude
                                    )
                                )
                                    .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                    .icon(bitmapDescriptorFactory)
                                driverImages[key] =
                                    getNavigator().getGMap()!!.addMarker(markeroption)!!
                            } else {
//                                bitmapDescriptorFactory =
//                                    BitmapDescriptorFactory.fromResource(R.drawable.new_car)
                                markeroption.position(LatLng(location.latitude, location.longitude))
                                    .anchor(0.5f, 0.5f).rotation(bearing.toFloat())
                                    .icon(bitMapFromVector(R.drawable.car_icon_us))
                                driverImages[key] =
                                    getNavigator().getGMap()!!.addMarker(markeroption)!!
                            }
                            firebaseHelper?.addObserverFor(key)
                        }

                    }
                } else {
                    if (driverImages.containsKey(key))
                        driverImages.remove(key)
                    if (driversLocations.containsKey(key))
                        driversLocations.remove(key)
                    if (driverDatas.containsKey(key))
                        driverDatas.remove(key)
                }
            }
        } catch (e: Exception) {

        }

    }


    override fun tripStatusReceived(response: String?) {

    }

    fun initializeFireBaseObserver() {
        Log.e(MapFragment.TAG,"initializeFireBaseObserver")
        firebaseHelper = FirebaseHelper(session, this, false)
        if (vmLatLng.get() != null)
            firebaseHelper?.queryDrivers(vmLatLng.get()!! ,if( session.getString(SessionMaintainence.SEARCH_RADIUS).isNullOrEmpty()) 3.0 else session.getString(SessionMaintainence.SEARCH_RADIUS)!!.toDouble())
    }

    fun openSos() {
        getNavigator().openSos()
    }

    fun checkZone(lat: String, long: String) {
        if (getNavigator().isNetworkConnected() && isLoading.value == false) {
            apiCode = 1
            val map = HashMap<String, String>()
            map[Config.pickup_lat] = lat
            map[Config.pickup_long] = long
            checkZoneBase(map)
        }
    }

    fun queryDrivers() {
        firebaseHelper?.queryDrivers(vmLatLng.get()!!,if( session.getString(SessionMaintainence.SEARCH_RADIUS).isNullOrEmpty()) 3.0 else session.getString(SessionMaintainence.SEARCH_RADIUS)!!.toDouble())
    }

    fun onMenuClick() {
        getNavigator().openSideMenu()
    }

    fun onNotificationClick() {
        if (isLoading.value == false)
            getNavigator().openNotificationPage()
    }

    fun onClickDaily() {
        isDialySelected.set(true)
        isRentalSelected.set(false)
        isOutstationSelected.set(false)
        getNavigator().getCtx()?.let {
            val bold = ResourcesCompat.getFont(it, R.font.roboto_bold)
            val regular = ResourcesCompat.getFont(it, R.font.roboto_regular)
            getNavigator().getBinds().dailyText.typeface = bold
            getNavigator().getBinds().rentalText.typeface = regular
            getNavigator().getBinds().outstationText.typeface = regular
        }
    }


    fun getFavoritePlaceApiCall() {
        if (getNavigator().isNetworkConnected() && isLoading.value == false) {
            apiCode = 2
            getFavoritePlaceBase()
        } else if (!getNavigator().isNetworkConnected())
            getNavigator().showNetworkUnAvailable()
    }

    var fPlaceName = ""
    var fAddress = ""
    var fLat = ""
    var fLng = ""
    fun addFavorite() {
        apiCode = 3
        if (getNavigator().isNetworkConnected()) {
            hashMap.clear()
            hashMap[Config.title] = fPlaceName
            hashMap[Config.address] = fAddress
            hashMap[Config.latitude] = fLat
            hashMap[Config.longitude] = fLng
            saveFavoritePlaceBase(hashMap)
        } else
            getNavigator().showNetworkUnAvailable()
    }


    fun onCurrentFavoClicked(view: View) {
        getNavigator().addCurrentToFavo()
    }

    fun selectPickupClick(view: View){
        getNavigator().openPickupEdit()
    }

    fun onClickCall(view: View) {
        translationModel.txt_admin_number.let {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:$it")
            view.context.startActivity(callIntent)
        }

    }

    fun onClickRental() {
        if(isLoading.value == false){
            isDialySelected.set(false)
            isRentalSelected.set(true)
            isOutstationSelected.set(false)
            getNavigator().getCtx()?.let {
                val bold = ResourcesCompat.getFont(it, R.font.roboto_bold)
                val regular = ResourcesCompat.getFont(it, R.font.roboto_regular)
                getNavigator().getBinds().dailyText.typeface = regular
                getNavigator().getBinds().rentalText.typeface = bold
                getNavigator().getBinds().outstationText.typeface = regular
            }
            getNavigator().openRental()
        }
    }

}