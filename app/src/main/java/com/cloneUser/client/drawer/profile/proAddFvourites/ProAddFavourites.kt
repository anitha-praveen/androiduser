package com.cloneUser.client.drawer.profile.proAddFvourites

import android.content.Context
import android.os.Bundle
import android.view.View
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
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.ProaddFavoBinding
import com.cloneUser.client.drawer.adapters.search_adapter.SearchAdapter
import com.cloneUser.client.ut.SessionMaintainence
import javax.inject.Inject

class ProAddFavourites : BaseFragment<ProaddFavoBinding, ProAddFavouritesVm>(),
    ProAddFavoNavigator,OnMapReadyCallback {

    lateinit var binding: ProaddFavoBinding
    lateinit var googleMap:GoogleMap
    var searchAdapter: SearchAdapter? = null
    private val args: ProAddFavouritesArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(ProAddFavouritesVm::class.java)
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
        binding.backImg.setOnClickListener { closeFragment() }
        initializeMapAndLocation()
        val sLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        searchAdapter = SearchAdapter(ArrayList(), this)
        binding.favoritesRecycler.layoutManager = sLayoutManager
        binding.favoritesRecycler.itemAnimator = DefaultItemAnimator()
        binding.favoritesRecycler.adapter = searchAdapter
        vm.mPlaceName.set(args.title)
    }


    override fun getLayoutId() = R.layout.proadd_favo

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    fun closeFragment() {
        findNavController().popBackStack()
    }
    private fun initializeMapAndLocation() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView_outstation_search) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
        vm.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        vm.latlng.set(vm.session.getString(SessionMaintainence.CURRENT_LATITUDE)
            ?.let { LatLng(it.toDouble(),
                vm.session.getString(SessionMaintainence.CURRENT_LONGITUDE)!!.toDouble()) })
        vm.changeMapStyle(googleMap, requireContext())
        vm.moveCamera(googleMap, vm.latlng.get(), 16f)
        setCameraListener()
    }

    private fun setCameraListener() {
        googleMap.setOnCameraIdleListener {
            vm.latlng.set(googleMap.cameraPosition.target)
            vm.getAddressFromLatLng(vm.address, vm.latlng)
        }
    }

    override fun loadAdapter(places: List<FavPlace.Favourite>) {
        searchAdapter?.addList(places)
    }

    override fun setPlace(place: FavPlace.Favourite) {
        vm.showAdapter.set(false)
        vm.showCnfrmButton.set(true)
        vm.showMarker.set(true)
        vm.address.set(place.address)
        place.address?.let {
            place.placeId?.let { ad ->

                vm.getLatLngFromAddress(
                    it,
                    ad, vm.latlng
                )
            }
        }
    }

    override fun getCtx(): Context {
        return requireContext()
    }

    override fun getGMap(): GoogleMap {
        return googleMap
    }

    override fun closeFrag() {
        if(isAdded)
        findNavController().popBackStack()
    }

}