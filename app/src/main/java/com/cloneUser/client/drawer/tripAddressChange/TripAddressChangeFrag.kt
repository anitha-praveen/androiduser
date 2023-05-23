package com.cloneUser.client.drawer.tripAddressChange

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.cloneUser.client.BR
import com.cloneUser.client.MyApplication
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.FragmentTripAddressChangeBinding
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.drawer.adapters.fav_adapter.FavoritesAdapter
import com.cloneUser.client.drawer.adapters.recent_loc_adapter.RecentLocationsAdapter
import com.cloneUser.client.drawer.adapters.search_adapter.SearchAdapter
import javax.inject.Inject

class TripAddressChangeFrag : BaseFragment<FragmentTripAddressChangeBinding, TripAddressChangeVM>(),
    TripAddressChangeNavigator, OnMapReadyCallback {

    lateinit var binding: FragmentTripAddressChangeBinding
    var isErrorDialogShown = false



    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(TripAddressChangeVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeFragment()
            }
        })

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        binding.back.setOnClickListener { closeFragment() }
        setValues()
        initializeMapAndLocation()
        setupAdapters()


    }


    override fun getLayoutId() = R.layout.fragment_trip_address_change

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    fun closeFragment() {
        findNavController().popBackStack()
    }

    override fun loadSearchResults(places: List<FavPlace.Favourite>) {
        searchAdapter?.addList(places)
    }

    override fun setSelectedSearchResult(place: FavPlace.Favourite) {
        vm.address.set(place.address)
        place.address?.let {
            place.placeId?.let { ad ->
                vm.enableConfirm.set(false)
                vm.getLatLngFromAddress(
                    it,
                    ad, vm.latLng
                )
            }
        }
        vm.showRecyclers.set(false)
    }

    override fun getGMap(): GoogleMap? {
        return googleMap
    }

    override fun getCtx(): Context {
        return if (isAdded) requireContext()
        else MyApplication.appContext!!
    }

    override fun loadFavorites(favPlaces: List<FavPlace.Favourite>) {
        favAdapter?.addList(favPlaces)
    }

    override fun setSelectedFavPlace(favPlace: FavPlace.Favourite) {
        vm.address.set(favPlace.address)
        favPlace.latitude?.let { lat ->
            favPlace.longitude?.let { lng ->
                vm.latLng.set(LatLng(lat.toDouble(), lng.toDouble()))
            }
        }
        vm.showRecyclers.set(false)
    }

    override fun loadRecentLocations(lastTrips: List<FavPlace.LastTripHistory>) {
        recentLocAdapter?.addList(lastTrips)
    }

    override fun setSelectedRecentLocation(lastTrip: FavPlace.LastTripHistory) {
        vm.address.set(lastTrip.dropAddress)
        lastTrip.dropLat?.let { lat ->
            lastTrip.dropLng?.let { lng ->
                vm.latLng.set(LatLng(lat, lng))
            }
        }
        vm.showRecyclers.set(false)
    }

    var favAdapter: FavoritesAdapter? = null
    var recentLocAdapter: RecentLocationsAdapter? = null
    var searchAdapter: SearchAdapter? = null
    private fun setupAdapters() {

        /* setting up favorite adapter */
//        val mLayoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//        favAdapter = FavoritesAdapter(ArrayList(), this)
//        binding.favoritesRecycler.layoutManager = mLayoutManager
//        binding.favoritesRecycler.itemAnimator = DefaultItemAnimator()
//        binding.favoritesRecycler.adapter = favAdapter


        /*setting up recent locations adapter */
//        val rLayoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//        recentLocAdapter =
//            RecentLocationsAdapter(ArrayList(), this)
//        binding.recentLocationsRecycler.layoutManager = rLayoutManager
//        binding.recentLocationsRecycler.itemAnimator = DefaultItemAnimator()
//        binding.recentLocationsRecycler.adapter = recentLocAdapter


        /*setting up search adapter */
        val sLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        searchAdapter = SearchAdapter(ArrayList(), this)
        binding.addressSearchResultsRecycler.layoutManager = sLayoutManager
        binding.addressSearchResultsRecycler.itemAnimator = DefaultItemAnimator()
        binding.addressSearchResultsRecycler.adapter = searchAdapter

    }

    private fun initializeMapAndLocation() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
        vm.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    lateinit var googleMap: GoogleMap
    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        vm.changeMapStyle(googleMap, requireContext())
        vm.moveCamera(googleMap, vm.latLng.get(), 16f)
        setCameraListener()
    }

    val args: TripAddressChangeFragArgs by navArgs()
    fun setValues() {
        vm.latLng.set(LatLng(args.lat.toDouble(), args.lng.toDouble()))
        vm.address.set(args.address)
        vm.addressChangeValue.set(
            when {
                args.type.equals("pickup", true) -> 1
                args.type.equals(
                    "drop",
                    true
                ) -> 2
                args.type.equals("pickup_rental", true) -> 3
                else -> -1
            }
        )
        vm.requestId = args.reqId
        if(args.type.equals("pickup",true))
            vm.destinationLatLng = LatLng(args.destLat.toDouble(),args.destLng.toDouble())
    }

    private fun setCameraListener() {
        googleMap.setOnCameraIdleListener {
            vm.latLng.set(googleMap.cameraPosition.target)
            vm.enableConfirm.set(false)
            vm.getAddressFromLatLng(vm.address, vm.latLng)
        }
    }

    override fun callRequestInProgressFromDrawer() {
        if (isAdded)
            (requireActivity() as DrawerActivity).callRequestInProgress()
    }

    override fun closeFromViewModel() {
        closeFragment()
    }


}