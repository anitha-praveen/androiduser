package com.cloneUser.client.drawer.confirmDestination

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.cloneUser.client.databinding.FragmentConfirmDestinationBinding
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.drawer.adapters.fav_adapter.FavoritesAdapter
import com.cloneUser.client.drawer.adapters.recent_loc_adapter.RecentLocationsAdapter
import com.cloneUser.client.drawer.adapters.search_adapter.SearchAdapter
import com.cloneUser.client.ut.ContactList
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class
ConfirmDestinationFragment :
    BaseFragment<FragmentConfirmDestinationBinding, ConfirmDestinationVM>(),
    ConfirmDestinationNavigator, OnMapReadyCallback {
    companion object {
        const val TAG = "ConfirmDestinationFragment"
    }


    private lateinit var binding: FragmentConfirmDestinationBinding
    private val args: ConfirmDestinationFragmentArgs by navArgs()
    lateinit var googleMap: GoogleMap
    var isTripAddressChange = false
    var outStationError = false

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(ConfirmDestinationVM::class.java)
    }

    var onViewCreatedCount = 0
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
        onViewCreatedCount += 1

        binding = getmBinding()
        vm.setNavigator(this)
        vm.printE("Count", "$onViewCreatedCount")
        binding.back.setOnClickListener { closeFragment() }
        setClear1()
        setClear2()
        setPickupAddress()
        initializeMapAndLocation()
        setupAdapters()
        vm.getFavoritePlaceApiCall()

        binding.dropAddress1.requestFocus()
        Utilz.openKeyBoard(requireActivity())
    }


    private fun initializeMapAndLocation() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
        vm.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    override fun getLayoutId() = R.layout.fragment_confirm_destination

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    override fun getCtx(): Context {
        return if (isAdded)
            requireContext()
        else
            MyApplication.appContext!!
    }

    override fun gotoRideConfirm() {

        ConfirmDestinationData.pickupLat = vm.pickLatLng.get()!!.latitude.toString()
        ConfirmDestinationData.pickupLng = vm.pickLatLng.get()!!.longitude.toString()

        ConfirmDestinationData.drop1Lat = "${vm.drop1LatLng.get()?.latitude ?: ""}"
        ConfirmDestinationData.drop1Lng = "${vm.drop1LatLng.get()?.longitude ?: ""}"

        if (vm.showMultipleDropElements.get()) {
            vm.drop2LatLng.get()?.let {
                ConfirmDestinationData.drop2Lat = it.latitude.toString()
                ConfirmDestinationData.drop2Lng = it.longitude.toString()
            }
            vm.drop2Address.get()?.let {
                ConfirmDestinationData.drop2Address = it
            }
        }

        ConfirmDestinationData.pickAddress = vm.pickAddress.get()!!
        ConfirmDestinationData.drop1Address = vm.drop1Address.get()!!

        ConfirmDestinationData.multipleDrop = vm.showMultipleDropElements.get()

        if (findNavController().currentDestination?.label == "Confirm Destination")
            findNavController().navigate(R.id.confirm_destination_to_ride_confirm)

    }

    override fun callRequestInProgressFromDrawer() {
        if (isAdded)
            (requireActivity() as DrawerActivity).callRequestInProgress()
    }


    @SuppressLint("Range")
    var resultContacts =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val contactData: Uri? = data?.data
                val c: Cursor? = contactData?.let {
                    requireActivity().contentResolver.query(
                        it,
                        null,
                        null,
                        null,
                        null
                    )
                }
                var name = ""
                var phone = ""

                if (c?.moveToFirst() == true) {
                    val id: String =
                        c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val hasPhone: String =
                        c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    if (hasPhone.equals("1", ignoreCase = true)) {
                        val phones: Cursor? = requireActivity().contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null, null
                        )
                        phones?.moveToFirst()
                        phone = phones?.getString(phones.getColumnIndex("data1")) ?: ""
                        name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        ContactList.name = name
                        ContactList.phone = phone
                        vm.rider.set(ContactList.name)
                        riderSelection?.dismiss()
                    }
                }
            }
        }

    val requestPermissionReadContacts =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                resultContacts.launch(intent)
            } else {
                showMessage("Without giving permission you cannot choose contacts")
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    var riderSelection: BottomSheetDialog? = null
    override fun openRiderSelection() {
        if (riderSelection?.isShowing == true)
            riderSelection?.dismiss()
        riderSelection = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        riderSelection?.setContentView(R.layout.rider_selection_bottom_sheet)
        val myself = riderSelection?.findViewById<RadioButton>(R.id.mySelf)
        val others = riderSelection?.findViewById<RadioButton>(R.id.others)
        val cancel = riderSelection?.findViewById<MaterialButton>(R.id.cancel)
        val save = riderSelection?.findViewById<MaterialButton>(R.id.save)
        val name = riderSelection?.findViewById<EditText>(R.id.name)
        val phone = riderSelection?.findViewById<EditText>(R.id.phone)
        val contact = riderSelection?.findViewById<ImageView>(R.id.contact)

        myself?.text = vm.translationModel.txt_myself
        others?.text = vm.translationModel.txt_Other
        cancel?.text = vm.translationModel.text_cancel
        save?.text = vm.translationModel.txt_save
        if (!ContactList.isMySelf)
            name?.setText(ContactList.name)
        phone?.setText(ContactList.phone)
        name?.hint = vm.translationModel.name
        phone?.hint = vm.translationModel.hint_phone_number
        if (!ContactList.isMySelf) {
            myself?.isChecked = false
            others?.isChecked = true
            name?.visibility = View.VISIBLE
            phone?.visibility = View.VISIBLE
            contact?.visibility = View.VISIBLE
        }
        myself?.setOnClickListener {
            ContactList.isMySelf = true
            name?.visibility = View.GONE
            phone?.visibility = View.GONE
            contact?.visibility = View.GONE
        }

        others?.setOnClickListener {
            ContactList.isMySelf = false
            name?.visibility = View.VISIBLE
            phone?.visibility = View.VISIBLE
            contact?.visibility = View.VISIBLE
        }

        contact?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    getCtx(),
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionReadContacts.launch(Manifest.permission.READ_CONTACTS)
            } else {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                resultContacts.launch(intent)
            }
        }
        cancel?.setOnClickListener {
            ContactList.isMySelf =
                !(ContactList.name?.isNotEmpty() == true && ContactList.phone?.isNotEmpty() == true)
            riderSelection?.dismiss()
        }

        save?.setOnClickListener {
            if (!ContactList.isMySelf) {
                when {
                    name?.text?.isEmpty() == true -> showMessage("Enter name")
                    phone?.text?.isEmpty() == true -> showMessage("Enter phone")
                    phone?.text?.length != 10 -> showMessage("Invalid phone contact number")
                    else -> {
                        ContactList.name = name?.text.toString()
                        ContactList.phone = phone.text.toString()
                        vm.rider.set(ContactList.name)
                        riderSelection?.dismiss()
                    }
                }
            } else {
                vm.rider.set("Myself")
                ContactList.name = ""
                ContactList.phone = ""
                riderSelection?.dismiss()
            }
        }

        riderSelection?.setCancelable(false)
        riderSelection?.show()

    }


    private fun closeFragment() {
        if (isTripAddressChange)
            (mActivity as DrawerActivity).callRequestInProgress()
        else {
            findNavController().navigate(R.id.confirm_destination_to_map)
            parentFragmentManager.beginTransaction().remove(this@ConfirmDestinationFragment)
                .commit()
        }
    }

    private fun setPickupAddress() {
//        binding.dropEditTextOne.isFocusable = false
        isTripAddressChange = args.isTripAddressChange
        if (isTripAddressChange)
            vm.requestId = args.requestId
        vm.pickAddress.set(args.pickupAddress)
        vm.pickLatLng.set(LatLng(args.pickupLatitude.toDouble(), args.pickupLongitude.toDouble()))
    }

    var favAdapter: FavoritesAdapter? = null
    var recentLocAdapter: RecentLocationsAdapter? = null
    var searchAdapter: SearchAdapter? = null
    var favouriteList: MutableList<FavPlace.Favourite> = ArrayList()
    var lastTripsList: MutableList<FavPlace.LastTripHistory> = ArrayList()
    private fun setupAdapters() {

        /* setting up favorite adapter */
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        favAdapter = FavoritesAdapter(favouriteList, this, 0)
        binding.favoritesRecycler.layoutManager = mLayoutManager
        binding.favoritesRecycler.itemAnimator = DefaultItemAnimator()
        binding.favoritesRecycler.adapter = favAdapter


        /*setting up recent locations adapter */
        val rLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recentLocAdapter =
            RecentLocationsAdapter(lastTripsList, this)
        binding.recentLocationsRecycler.layoutManager = rLayoutManager
        binding.recentLocationsRecycler.itemAnimator = DefaultItemAnimator()
        binding.recentLocationsRecycler.adapter = recentLocAdapter


        /*setting up search adapter */
        val sLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        searchAdapter = SearchAdapter(ArrayList<FavPlace.Favourite>(), this)
        binding.addressSearchResultsRecycler.layoutManager = sLayoutManager
        binding.addressSearchResultsRecycler.itemAnimator = DefaultItemAnimator()
        binding.addressSearchResultsRecycler.adapter = searchAdapter

    }

    override fun loadFavorites(favPlaces: List<FavPlace.Favourite>) {
        favAdapter?.addList(favPlaces)
    }

    override fun setSelectedFavPlace(favPlace: FavPlace.Favourite) {
        if (vm.editValue == 1) {
            vm.drop1Address.set(favPlace.address)
            favPlace.latitude?.let { lat ->
                favPlace.longitude?.let { lng ->
                    vm.drop1LatLng.set(LatLng(lat.toDouble(), lng.toDouble()))
                }
            }
        } else {
            vm.drop2Address.set(favPlace.address)
            favPlace.latitude?.let { lat ->
                favPlace.longitude?.let { lng ->
                    vm.drop2LatLng.set(LatLng(lat.toDouble(), lng.toDouble()))
                }
            }
        }
        hideRecyclerViews()
        hideKeyboard(requireActivity())

    }


    override fun loadRecentLocations(lastTrips: List<FavPlace.LastTripHistory>) {
        recentLocAdapter?.addList(lastTrips)
    }

    override fun setSelectedRecentLocation(lastTrip: FavPlace.LastTripHistory) {
        if (vm.editValue == 1) {
            vm.drop1Address.set(lastTrip.dropAddress)
            lastTrip.dropLat?.let { lat ->
                lastTrip.dropLng?.let { lng ->
                    vm.drop1LatLng.set(LatLng(lat, lng))
                }
            }
        } else {
            vm.drop2Address.set(lastTrip.dropAddress)
            lastTrip.dropLat?.let { lat ->
                lastTrip.dropLng?.let { lng ->
                    vm.drop2LatLng.set(LatLng(lat, lng))
                }
            }
        }

        hideKeyboard(requireActivity())
        hideRecyclerViews()
    }

    override fun loadSearchResults(places: List<FavPlace.Favourite>) {
        searchAdapter?.addList(places)
    }

    override fun setSelectedSearchResult(place: FavPlace.Favourite) {
        if (vm.editValue == 1) {
            vm.drop1Address.set(place.address)
            place.address?.let {
                place.placeId?.let { ad ->
                    vm.getLatLngFromAddress(
                        it,
                        ad, vm.drop1LatLng
                    )
                }
            }
        } else {
            vm.drop2Address.set(place.address)
            place.address?.let {
                place.placeId?.let { ad ->
                    vm.getLatLngFromAddress(
                        it,
                        ad, vm.drop2LatLng
                    )
                }
            }
        }
        hideKeyboard(requireActivity())
        hideRecyclerViews()

    }


    override fun getBinds(): FragmentConfirmDestinationBinding {
        return binding
    }

    override fun setFavoriteList(favouriteList: List<FavPlace.Favourite>) {
        this.favouriteList = favouriteList as MutableList<FavPlace.Favourite>
    }

    override fun setLastTripHistory(lastTripsList: List<FavPlace.LastTripHistory>) {
        this.lastTripsList = lastTripsList as MutableList<FavPlace.LastTripHistory>
    }


    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        vm.changeMapStyle(googleMap, requireContext())
        vm.moveCamera(googleMap, vm.pickLatLng.get(), 16f)
//        setCameraListener()
    }

    private fun setCameraListener() {
        googleMap.setOnCameraIdleListener {
            if (vm.editValue == 1) {
                vm.drop1LatLng.set(googleMap.cameraPosition.target)
                vm.getAddressFromLatLng(vm.drop1Address, vm.drop1LatLng)
            } else if (vm.editValue == 2) {
                vm.drop2LatLng.set(googleMap.cameraPosition.target)
                vm.getAddressFromLatLng(vm.drop2Address, vm.drop2LatLng)
            } else {
                vm.editValue = 1
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    fun setClear1() {

        if (vm.drop1Address.get()?.isEmpty()!!) {
            binding.dropAddress1.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.clear_icon_invisible_small,
                0
            )
        } else {
            binding.dropAddress1.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.clear_icon_visible_small,
                0
            )
        }


        binding.dropAddress1.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding.dropAddress1.right - binding.dropAddress1.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    vm.drop1Address.set("")
                    vm.drop1LatLng.set(LatLng(0.0, 0.0))
                    binding.dropAddress1.requestFocus()
                    return@OnTouchListener true
                }
            }
            false
        })

    }

    @SuppressLint("ClickableViewAccessibility")
    fun setClear2() {

        if (vm.drop2Address.get()?.isEmpty()!!) {
            binding.dropAddress2.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.clear_icon_invisible_small,
                0
            );
        } else {
            binding.dropAddress2.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.clear_icon_visible_small,
                0
            );
        }

        binding.dropAddress2.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding.dropAddress2.right - binding.dropAddress2.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    vm.drop2Address.set("")
                    vm.drop2LatLng.set(LatLng(0.0, 0.0))
                    binding.dropAddress2.requestFocus()
                    return@OnTouchListener true
                }
            }
            false
        })

    }

    override fun openFavBottomSheet() {
        vm.fPlaceName = ""
        val saveFav = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        saveFav.setContentView(R.layout.save_favorite_bottom_sheet)
        saveFav.show()

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
    }

    override fun setFavDetails(lastTripHistory: FavPlace.LastTripHistory) {
        vm.fAddress = lastTripHistory.dropAddress ?: ""
        vm.fLat = "${lastTripHistory.dropLat}"
        vm.fLng = "${lastTripHistory.dropLng}"
        openFavBottomSheet()
    }
    override fun getCurrentLoc(): Location? {
        var location: Location? = null
        vm.session.getString(SessionMaintainence.CURRENT_LATITUDE)?.let { lat ->
            vm.session.getString(SessionMaintainence.CURRENT_LONGITUDE)?.let { lng ->
                if (lat.isNotEmpty() && lng.isNotEmpty()) {
                    location = Location("current")
                    location?.latitude = lat.toDouble()
                    location?.longitude = lng.toDouble()
                }
            }
        }
        return location
    }

    override fun hideKeyBoardNav() {
        hideKeyboard(requireActivity())
    }


    private fun hideRecyclerViews() {
        Timer("hideRecyclers", false).schedule(500) {
            vm.showRecyclers.set(false)
            requireActivity().runOnUiThread {
                vm.proceedNext()
            }
        }
    }

}