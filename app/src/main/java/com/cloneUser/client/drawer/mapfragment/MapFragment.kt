package com.cloneUser.client.drawer.mapfragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.FragmentMapBinding
import com.cloneUser.client.dialogs.addressEdit.AddressEditDialog
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.drawer.adapters.fav_adapter.FavoritesAdapter
import com.cloneUser.client.drawer.adapters.recent_loc_adapter.RecentLocationsAdapter
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationData
import com.cloneUser.client.splash.SplashActivity
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import kotlinx.coroutines.launch
import javax.inject.Inject

class MapFragment : BaseFragment<FragmentMapBinding, MapFragmentVM>(),
    MapFragmentNavigator, OnMapReadyCallback {
    companion object {
        const val TAG = "MapFragment"
    }

    private var backPressed = false
    lateinit var _binding: FragmentMapBinding
    lateinit var googleMap: GoogleMap
    var isTripAddressChange = false
    var outStationError = false
    var isFavoShown = false

    /*
    The below variable used to decide should(initialize map when return from search place fragment).
    By default its value is true and only changed inside receiver
     */
    var initializeMapWithCurrentLocation = true

    @Inject
    lateinit var viewModelFagtory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFagtory).get(MapFragmentVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = getmBinding()
        vm.setNavigator(this)
        vm.onClickDaily()
        if (arguments != null) {
            initializeMapWithCurrentLocation = false
            isTripAddressChange = requireArguments().getBoolean(Config.isTripPickupChange)
            if (isTripAddressChange)
                vm.requestId = requireArguments().getString(Config.request_id)!!
            showConfirmLocationDialogue()
            vm.address.set(requireArguments().getString(Config.pickup_address))
            vm.vmLatLng.set(
                LatLng(
                    requireArguments().getDouble(Config.pickup_lat, 0.00),
                    requireArguments().getDouble(Config.pickup_long, 0.00)
                )
            )

        } else {
            initializeMapWithCurrentLocation = true
        }
        initializeMapAndLocation()
        _binding.pickupAddress.isSelected = true
    }


    private val editedAddressReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getIntExtra("addressChangeValue", 0) == 1) {
                vm.address.set(intent.getStringExtra("address"))
                vm.vmLatLng.set(
                    LatLng(
                        intent.getStringExtra("lat")!!.toDouble(),
                        intent.getStringExtra("lng")!!.toDouble()
                    )
                )
                googleMap.let { vm.moveCamera(it, vm.vmLatLng.get(), 16f) }
                vm.checkZone(
                    vm.vmLatLng.get()?.latitude.toString(),
                    vm.vmLatLng.get()?.longitude.toString()
                )

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    isTripAddressChange -> callRequestInProgressFromDrawer()
                    vm.isAskingConfirmLocation.get() -> {
                        vm.onClickCurrentLocation()
                        vm.isAskingConfirmLocation.set(false)
                    }
                    else -> {
                        if (backPressed)
                            requireActivity().finishAffinity()
                        backPressed = true
                        showMessage("Press back again to exit")
                        Handler(Looper.getMainLooper()).postDelayed(
                            { backPressed = false }, 2000

                        )
                    }
                }


            }
        })

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            editedAddressReceiver,
            IntentFilter(Config.ADDRESS_EDIT_TO_MAP)
        )

    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(editedAddressReceiver)
        super.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        hideKeyboard(requireActivity())
    }


    override fun getLayoutId() = R.layout.fragment_map

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm
    override fun goToSplash() {
        startActivity(Intent(requireActivity(), SplashActivity::class.java))
    }

    override fun getCtx(): Context? {
        return if (isAdded) requireContext() else null
    }

    override fun getFrgAct(): FragmentActivity? {
        return if (isAdded)
            requireActivity()
        else null
    }

    override fun getGMap(): GoogleMap {
        return googleMap
    }

    override fun goToConfirmDestination() {
        val action = MapFragmentDirections.mapToConfirmDestination(
            vm.vmLatLng.get()!!.latitude.toString(),
            vm.vmLatLng.get()!!.longitude.toString(),
            vm.address.get()!!
        )
        findNavController().navigate(action)
    }

    override fun callRequestInProgressFromDrawer() {
        if (isAdded)
            (requireActivity() as DrawerActivity).callRequestInProgress()
    }

    override fun checkIsTripAddressChange(): Boolean {
        return isTripAddressChange
    }

    override fun openSos() {
        findNavController().navigate(R.id.map_to_sos)
    }

    override fun avoidScrolling() {
        googleMap.uiSettings.isScrollGesturesEnabled = false

    }

    override fun enableScrolling() {
        googleMap.uiSettings.isScrollGesturesEnabled = true
    }

    override fun openOutstation() {
        findNavController().navigate(R.id.map_to_outStation)
    }

    override fun setFavDetails(lastTripHistory: FavPlace.LastTripHistory) {
        vm.fAddress = lastTripHistory.dropAddress ?: ""
        vm.fLat = "${lastTripHistory.dropLat}"
        vm.fLng = "${lastTripHistory.dropLng}"
        openFavBottomSheet()
    }


    private fun initializeMapAndLocation() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
        vm.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        if (initializeMapWithCurrentLocation) {
            setInitialLocation()
        } else {
            vm.moveCamera(googleMap, vm.vmLatLng.get(), vm.zoom)
        }
        vm.userDragged.set(false)
//        vm.checkZone(vm.vmLatLng.get()?.latitude.toString(),vm.vmLatLng.get()?.longitude.toString())
        vm.initializeFireBaseObserver()
        vm.changeMapStyle(googleMap, requireContext())
        setCameraListener()
    }

    /*
    The below method is called from onMapReady
     */
    private fun setInitialLocation() {
        if (!vm.session.getString(SessionMaintainence.CURRENT_LATITUDE).isNullOrEmpty() &&
            !vm.session.getString(SessionMaintainence.CURRENT_LONGITUDE).isNullOrEmpty()
        ) {
            val latLng = LatLng(
                vm.session.getString(SessionMaintainence.CURRENT_LATITUDE)!!.toDouble(),
                vm.session.getString(SessionMaintainence.CURRENT_LONGITUDE)!!.toDouble()
            )
            vm.moveCamera(googleMap, latLng, vm.zoom)
            vm.vmLatLng.set(latLng)
            vm.getAddressFromLatLng()
            vm.isLocationAvailable.set(true)
            vm.checkZone(
                vm.vmLatLng.get()?.latitude.toString(),
                vm.vmLatLng.get()?.longitude.toString()
            )
        } else
            vm.viewModelScope.launch { vm.getLastLocation(requireContext()) }
    }

    /*
    The below method is called from onMapReady
     */
    private fun setCameraListener() {
        googleMap.setOnCameraMoveStartedListener {
            var reasonText = "UNKNOWN_REASON"
            when (it) {
                GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                    reasonText = "GESTURE"
                    vm.userDragged.set(true)

                }
                GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION -> {
                    reasonText = "API_ANIMATION"
                    vm.userDragged.set(false)
                }
                GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> {
                    reasonText = "DEVELOPER_ANIMATION"
                    vm.userDragged.set(false)
                }
            }

            Log.d(TAG, "onCameraMoveStarted($reasonText)")
        }

        googleMap.setOnCameraIdleListener {
            vm.vmLatLng.set(googleMap.cameraPosition.target)
            vm.getAddressFromLatLng()
            if (vm.userDragged.get())
                showConfirmLocationDialogue()
        }
    }

    /*
    The below method is to show dialog whether the location is confirmed and to fetch drivers from firebase
     */
    private fun showConfirmLocationDialogue() {
        vm.isAskingConfirmLocation.set(true)
    }


    override fun openFavBottomSheet() {
        vm.fPlaceName = ""
        val saveFav = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        saveFav.setContentView(R.layout.save_favorite_bottom_sheet)
        if (!isFavoShown) {
            saveFav.show()
            isFavoShown = true
        }

        val title = saveFav.findViewById<TextView>(R.id.title)
        val address = saveFav.findViewById<TextView>(R.id.address)
        val radioHome = saveFav.findViewById<RadioButton>(R.id.radio_button_home)
        val radioWork = saveFav.findViewById<RadioButton>(R.id.radio_button_work)
        val radioOther = saveFav.findViewById<RadioButton>(R.id.radio_button_other)
        val placeName = saveFav.findViewById<EditText>(R.id.place_name)
        val cancel = saveFav.findViewById<MaterialButton>(R.id.cancel)
        val save = saveFav.findViewById<MaterialButton>(R.id.save)

        title?.text = vm.translationModel.txt_save_as_fav
        address?.text = vm.fAddress
        radioHome?.text = vm.translationModel.txt_Home
        radioWork?.text = vm.translationModel.txt_Work
        radioOther?.text = vm.translationModel.txt_Other
        placeName?.hint = vm.translationModel.txt_place_name_enter
        cancel?.text = vm.translationModel.text_cancel
        save?.text = vm.translationModel.txt_save
        var otherSelected = false
        cancel?.setOnClickListener {
            saveFav.dismiss()
        }

        save?.setOnClickListener {
            if (otherSelected && placeName?.text?.isEmpty()!!) {
                placeName.error = vm.translationModel.txt_enter_short_name
            } else if (!otherSelected && vm.fPlaceName.isEmpty()) {
                showMessage(vm.translationModel.text_Please_Select_an_Option ?: "")
            } else if (otherSelected && placeName?.text?.isNotEmpty()!!) {
                vm.fPlaceName = placeName.text.toString()
                vm.addFavorite()
                saveFav.dismiss()
            } else {
                if (vm.fPlaceName.isNotEmpty()) {
                    vm.addFavorite()
                    saveFav.dismiss()
                }
            }
        }

        radioHome?.setOnClickListener {
            vm.fPlaceName = "Home"
            placeName?.visibility = View.GONE
            otherSelected = false
        }
        radioWork?.setOnClickListener {
            vm.fPlaceName = "Work"
            placeName?.visibility = View.GONE
            otherSelected = false
        }
        radioOther?.setOnClickListener {
            vm.fPlaceName = ""
            placeName?.visibility = View.VISIBLE
            otherSelected = true
        }
        saveFav.setOnDismissListener { isFavoShown = false }
    }

    override fun getBinds(): FragmentMapBinding {
        return _binding
    }

    var favoritesAdapter: FavoritesAdapter? = null
    var recentLocAdapter: RecentLocationsAdapter? = null

    override fun setupAdapter() {
        favoritesAdapter = FavoritesAdapter(ArrayList<FavPlace.Favourite>(), this, 0)
        val mLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        _binding.favListRecycler.layoutManager = mLayoutManager
        _binding.favListRecycler.itemAnimator = DefaultItemAnimator()
        _binding.favListRecycler.adapter = favoritesAdapter


        /*setting up recent locations adapter */
        val rLayoutManager =
            LinearLayoutManager(getCtx(), LinearLayoutManager.VERTICAL, false)
        recentLocAdapter =
            RecentLocationsAdapter(ArrayList<FavPlace.LastTripHistory>(), this)
        _binding.recentListRecycler.layoutManager = rLayoutManager
        _binding.recentListRecycler.itemAnimator = DefaultItemAnimator()
        _binding.recentListRecycler.adapter = recentLocAdapter


        vm.getFavoritePlaceApiCall()
    }

    override fun openSideMenu() {
        (requireActivity() as DrawerActivity).openSideMenu()
    }


    override fun openPickupEdit() {
        val addressEdit =
            AddressEditDialog.newInstance(vm.vmLatLng.get()!!, "pickup", vm.address.get()!!, TAG)
        addressEdit.show(requireActivity().supportFragmentManager, AddressEditDialog.TAG)
    }

    override fun addCurrentToFavo() {
        vm.fAddress = vm.address.get()!!
        vm.fLat = vm.vmLatLng.get()?.latitude.toString()
        vm.fLng = vm.vmLatLng.get()?.longitude.toString()
        openFavBottomSheet()

    }

    override fun openNotificationPage() {
        findNavController().navigate(R.id.map_to_notification)
    }

    override fun loadFavoriteLocations(favPlace: List<FavPlace.Favourite>?) {
        favoritesAdapter!!.addList(favPlace!!)
    }

    override fun loadRecentLocations(lastTrips: List<FavPlace.LastTripHistory>) {
        recentLocAdapter!!.addList(lastTrips)
    }

    override fun setDropFromFavorites(place: FavPlace.Favourite) {
        vm.favPlaces = place
        if (vm.vmLatLng.get() != null && vm.vmLatLng.get()?.longitude != 0.0 && vm.address.get() != null && vm.address.get()
                ?.isNotEmpty()!!
        ) {
           // vm.checkOutstation(place.latitude.toString(), place.longitude.toString())
        }
    }

    override fun setDropFromRecent(place: FavPlace.LastTripHistory) {
        if (vm.vmLatLng.get() != null && vm.vmLatLng.get()?.longitude != 0.0 && vm.address.get() != null && vm.address.get()
                ?.isNotEmpty()!! && place.dropLat != null && place.dropLng != null
        ) {
            ConfirmDestinationData.setToDefault()

            ConfirmDestinationData.pickupLat = vm.vmLatLng.get()?.latitude.toString()
            ConfirmDestinationData.pickupLng = vm.vmLatLng.get()?.longitude.toString()

            ConfirmDestinationData.drop1Lat = place.dropLat.toString()
            ConfirmDestinationData.drop1Lng = place.dropLng.toString()

            ConfirmDestinationData.pickAddress = vm.address.get() ?: ""

            ConfirmDestinationData.drop1Address = place.dropAddress ?: ""

            findNavController().navigate(R.id.map_to_ride_confirm)
        }

    }

    override fun getMarkerIcon(drawable: Int): BitmapDescriptor? {
        return Utilz.getBitmapFromDrawable(
            requireContext(),
            drawable
        )?.let {
            BitmapDescriptorFactory.fromBitmap(
                it
            )
        }
    }

    override fun goTorideConfirmFragement(place: FavPlace.Favourite) {
        ConfirmDestinationData.setToDefault()
        ConfirmDestinationData.pickupLat = vm.vmLatLng.get()?.latitude.toString()
        ConfirmDestinationData.pickupLng = vm.vmLatLng.get()?.longitude.toString()


        ConfirmDestinationData.drop1Lat = place.latitude ?: ""
        ConfirmDestinationData.drop1Lng = place.longitude ?: ""

        ConfirmDestinationData.pickAddress = vm.address.get() ?: ""

        ConfirmDestinationData.drop1Address = place.address ?: ""


        findNavController().navigate(R.id.map_to_ride_confirm)

    }

    override fun openRental() {
        if (vm.vmLatLng.get() != null && vm.address.get()!!.isNotEmpty()) {
            val action = MapFragmentDirections.mapToRental(
                "" + vm.vmLatLng.get()!!.latitude,
                "" + vm.vmLatLng.get()!!.longitude,
                vm.address.get()!!
            )
            findNavController().navigate(action)
        }
    }


}