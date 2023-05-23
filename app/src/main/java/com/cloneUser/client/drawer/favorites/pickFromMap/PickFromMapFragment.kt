package com.cloneUser.client.drawer.favorites.pickFromMap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.cloneUser.client.BR
import com.cloneUser.client.MyApplication
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.FragmentPickFromMapBinding
import com.cloneUser.client.ut.SessionMaintainence
import javax.inject.Inject

class PickFromMapFragment : BaseFragment<FragmentPickFromMapBinding, PickFromMapVM>(),
    PickFromMapNavigator , OnMapReadyCallback {
    companion object {
        const val TAG = "PickFromMapFragment"
    }

    private val args : PickFromMapFragmentArgs by navArgs()
    private lateinit var binding: FragmentPickFromMapBinding
    private lateinit var googleMap: GoogleMap
    val fields = listOf(Place.Field.ID, Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG)

    var placesSearch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    vm.mAddress.set(place.address)
                    vm.latLng?.value = place.latLng
                    vm.latLng?.value?.let {
                        vm.moveCamera(googleMap,it,19f)
                    }
                }
            }
        }

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(PickFromMapVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)

        // Initialize the SDK
        Places.initialize(requireContext(), vm.session.getString(SessionMaintainence.PLACES_DYNAMIC_KEY)?:"")
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        vm.placeName = args.placeName
        initializeMapAndLocation()
    }

    private fun initializeMapAndLocation() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.pick_from_map) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
        vm.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    override fun getLayoutId() = R.layout.fragment_pick_from_map

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        vm.changeMapStyle(googleMap,getCtx())
        vm.getLastLocation(getCtx(),googleMap)
        setCameraListener()
    }

    override fun getCtx(): Context {
        return if(isAdded)
            requireContext()
        else
            MyApplication.appContext!!
    }

    override fun getGmap(): GoogleMap {
        return googleMap
    }

    override fun goToFavoritesList() {
        findNavController().navigate(R.id.pick_from_map_to_fav_list)
    }

    override fun launchSearchPlace() {
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountry("IN")
            .build(requireContext())
        placesSearch.launch(intent)
    }

    private fun setCameraListener() {
        googleMap.setOnCameraIdleListener {
            vm.latLng?.value = (googleMap.cameraPosition.target)
            vm.getAddressFromLatLng()
        }
    }


}