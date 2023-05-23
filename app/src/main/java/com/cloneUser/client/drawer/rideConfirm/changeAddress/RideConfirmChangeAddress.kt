package com.cloneUser.client.drawer.rideConfirm.changeAddress

import com.cloneUser.client.drawer.rideConfirm.adapter.ChangeAddressAdapter
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.RideConfirmChangeAddressBinding
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationData
import com.cloneUser.client.ut.Config.Companion.address
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject

class RideConfirmChangeAddress: BaseFragment<RideConfirmChangeAddressBinding, RideConfirmChangeAddressVm>(),
    RideConfirmChangeNavigator,OnMapReadyCallback {

    lateinit var binding: RideConfirmChangeAddressBinding
    private lateinit var adapter: ChangeAddressAdapter
    lateinit var googleMap: GoogleMap
    private val callCameraListner = ObservableBoolean(false)
    companion object{
        var address =""
        fun instance(address:String ){
            this.address = address
        }
    }


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(RideConfirmChangeAddressVm::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { closeFragment()} })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        binding.backImg.setOnClickListener { closeFragment() }
        vm.setNavigator(this)
        vm.address.set(address)
        initializeMapAndLocation()
        setUpAdapter()
        binding.pickupAddressRc.requestFocus()
        binding.pickupAddressRc.selectAll()
        activity?.let { Utilz.openKeyBoard(it) }
    }

    private fun setUpAdapter(){
        val sLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = ChangeAddressAdapter(ArrayList(), this)
        binding.searchRecycler.layoutManager = sLayoutManager
        binding.searchRecycler.itemAnimator = DefaultItemAnimator()
        binding.searchRecycler.adapter = adapter
    }

    private fun initializeMapAndLocation() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView_rideConfirm_search) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
    }


    override fun getLayoutId() = R.layout.ride_confirm_change_address

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    fun closeFragment(){
        activity?.let { hideKeyboard(it) }
        findNavController().popBackStack()
    }

    override fun loadAdapter(favPlace: List<FavPlace.Favourite>?) {
        if (favPlace != null) {
            adapter.addList(favPlace)
        }
    }

    override fun getCtx() = context!!

    override fun getGMap() = googleMap
    override fun onItemSelected(place: FavPlace.Favourite) {
        activity?.let { hideKeyboard(it) }
        vm.showAdapter.set(false)
        vm.showCnfrmButton.set(true)
        vm.showMarker.set(true)
        vm.address.set(place.address)
        place.address?.let {
            vm.getLatLngFromAddress(it,vm.latlng)
        }
    }

    override fun gotoRideConfirm() {
        ConfirmDestinationData.drop1Address = vm.address.get().toString()
        ConfirmDestinationData.drop1Lat = vm.latlng.get()?.latitude.toString()
        ConfirmDestinationData.drop1Lng = vm.latlng.get()?.longitude.toString()
        findNavController().navigate(R.id.ride_confirm)

    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        vm.getLatLngFromAddress(address,vm.latlng)
        setCameraListener()
    }

    private fun setCameraListener() {
        googleMap.setOnCameraIdleListener {
            if (callCameraListner.get()){
                vm.latlng.set(googleMap.cameraPosition.target)
                vm.getAddressFromLatLng(vm.address, vm.latlng)
            }
        }
        googleMap.setOnCameraMoveStartedListener { reason ->
            when(reason){
                GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> callCameraListner.set(true)
            }
        }
    }

}