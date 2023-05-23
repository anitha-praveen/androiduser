package com.cloneUser.client.drawer.outStation.outstationSearchPlace

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.OutstationSerachFragmentBinding
import com.cloneUser.client.drawer.adapters.search_adapter.SearchAdapter
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import javax.inject.Inject

class OutStationSearchFragment : BaseFragment<OutstationSerachFragmentBinding, OutStationSearchVm>(),
    OutStationSearchNavigator, OnMapReadyCallback {

    lateinit var binding: OutstationSerachFragmentBinding
    lateinit var googleMap: GoogleMap
    var searchAdapter: SearchAdapter? = null
    private var outStationError = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(OutStationSearchVm::class.java)
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
        binding.searchRecycler.layoutManager = sLayoutManager
        binding.searchRecycler.itemAnimator = DefaultItemAnimator()
        binding.searchRecycler.adapter = searchAdapter
    }




    override fun getLayoutId() = R.layout.outstation_serach_fragment

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
    // to show no result available in search outstation


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
        if(isAdded) {
            val intent = Intent(Config.RECEIVE_PICKUP_DETAILS)
            vm.latlng.get()?.let { intent.putExtra(Config.latitude, it.latitude) }
            intent.putExtra(Config.longitude, vm.latlng.get()?.longitude)
            intent.putExtra(Config.pick_address,vm.address.get())
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            findNavController().navigateUp()
        }
    }

    override fun outOfStationDialog() {
        val errorDialog = context?.let { BottomSheetDialog(it, R.style.AppBottomSheetDialogTheme) }
        errorDialog?.setContentView(R.layout.outstation_error_dialog)
        if (!outStationError) {
            errorDialog?.show()
            outStationError = true
        }
        errorDialog?.setOnDismissListener {
            outStationError = false
        }


        val title = errorDialog?.findViewById<TextView>(R.id.outerror_title)
        val desc = errorDialog?.findViewById<TextView>(R.id.desc_outerror)
        val okButton = errorDialog?.findViewById<MaterialButton>(R.id.ok_outstation_error)
        if (title != null) {
            title.text = vm.translationModel.txt_outZone_error_title
        }
        if (desc != null) {
            desc.text = vm.translationModel.txt_exceeding_km
        }
        if (okButton != null) {
            okButton.text = vm.translationModel.txt_ok
        }
        okButton?.setOnClickListener{
            if (errorDialog.isShowing)
                errorDialog.dismiss()
        }
        errorDialog?.setCanceledOnTouchOutside(false)
    }


}