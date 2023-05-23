package com.cloneUser.client.dialogs.addressEdit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.DialogAddressEditBinding
import com.cloneUser.client.drawer.adapters.search_adapter.SearchAdapter
import com.cloneUser.client.drawer.mapfragment.MapFragment
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject

class AddressEditDialog : BaseDialog<DialogAddressEditBinding, AddressEditVM>(),
    AddressEditNavigator, OnMapReadyCallback {
    companion object {
        const val TAG = "AddressEditDialog"
        lateinit var latLng : LatLng
        lateinit var addressChangeValue: String
        lateinit var address : String
        lateinit var target: String
        fun newInstance(iLatLng: LatLng , iAddressChangeValue: String , iAddress: String , iTarget: String) = AddressEditDialog().apply{
            latLng = iLatLng
            addressChangeValue = iAddressChangeValue
            address = iAddress
            target = iTarget
        }
    }

    private var callLatLngfun = ObservableBoolean(false)
    private lateinit var binding: DialogAddressEditBinding


    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(AddressEditVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setDialogFullSCreen()
        binding.back.setOnClickListener { close() }
        setValues()
        initializeMapAndLocation()
        setupAdapters()
    }

    override fun getLayout(): Int = R.layout.dialog_address_edit

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getViewModel(): AddressEditVM = vm

    override fun loadSearchResults(places: List<FavPlace.Favourite>) {
        searchAdapter?.addList(places)
    }

    override fun getCtx(): Context? {
        return if (isAdded) requireContext()
        else null
    }

    override fun showSnackBar(message: String) =
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun isNetworkConnected(): Boolean = Utilz.checkForInternet(requireActivity())

    override fun showCustomDialog(message: String) {}

    fun setValues() {
        vm.latLng.set(latLng)
        vm.address.set(address)
        vm.addressChangeValue.set(
            when {
                addressChangeValue.equals("pickup", true) -> 1
                addressChangeValue.equals(
                    "drop",
                    true
                ) -> 2
                else -> -1
            }
        )
    }

    private fun initializeMapAndLocation() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
        vm.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    var googleMap: GoogleMap? = null
    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        vm.changeMapStyle(googleMap, requireContext())
        vm.moveCamera(googleMap!!, vm.latLng.get(), 16f)
        setCameraListener()
    }

    private fun setCameraListener() {
        googleMap?.setOnCameraIdleListener {
            if (callLatLngfun.get()) {
                vm.latLng.set(googleMap?.cameraPosition?.target)
                vm.enableConfirm.set(false)
                vm.getAddressFromLatLng(vm.address, vm.latLng)
            }
        }
        googleMap?.setOnCameraMoveStartedListener { reason ->
            when(reason){
            REASON_GESTURE -> callLatLngfun.set(true)
            }
        }
    }



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

    override fun setSelectedSearchResult(place: FavPlace.Favourite) {
        callLatLngfun.set(false)
        vm.address.set(place.address)
        place.address?.let {
            place.placeId?.let { ad ->
                vm.enableConfirm.set(false)
                vm.getLatLngFromAddress(
                    it,
                    ad,
                    vm.latLng
                )
            }
        }
        vm.showRecyclers.set(false)
        hideKeyBoard(binding.pickupAddress)
    }

    override fun getGMap(): GoogleMap? {
        return googleMap
    }

    override fun close() {
        hideKeyBoard(binding.pickupAddress)
        dismiss()
    }

    override fun sendToTarget() {
        var intent: Intent? = null
        if (target == MapFragment.TAG)
            intent = Intent(Config.ADDRESS_EDIT_TO_MAP)
        intent?.let {
            it.putExtra("address", vm.address.get())
            it.putExtra("lat", "" + vm.latLng.get()!!.latitude)
            it.putExtra("lng", "" + vm.latLng.get()!!.longitude)
            it.putExtra("addressChangeValue", vm.addressChangeValue.get())
            getCtx()?.let { ctx -> LocalBroadcastManager.getInstance(ctx).sendBroadcast(it) }
        }
        close()
    }


    override fun getBindss(): DialogAddressEditBinding {
        return binding
    }

}