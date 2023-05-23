package com.cloneUser.client.drawer.rental

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.PackageModel
import com.cloneUser.client.connection.responseModels.RentalPackageTypes
import com.cloneUser.client.databinding.FragmentRentalBinding
import com.cloneUser.client.dialogs.addressEdit.AddressEditDialog
import com.cloneUser.client.drawer.adapters.rentalPackageAdapter.RentalPackageAdapter
import com.cloneUser.client.drawer.adapters.rentalTypesAdapter.RentalTypesAdapter
import com.cloneUser.client.drawer.applyPromo.ApplyPromoDialog
import com.cloneUser.client.drawer.rental.rentalFareDetails.RentalFairBottom
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.Utilz
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RentalFrag : BaseFragment<FragmentRentalBinding, RentalVM>(),
    RentalNavigator, OnMapReadyCallback {
    private var isdialogShown = false

    companion object {
        const val TAG = "RentalFrag"
    }

    private var slugSelected: String? = null
    private lateinit var binding: FragmentRentalBinding

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(RentalVM::class.java)
    }

    private val promoCodeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.isPromoApplied.set(intent.getBooleanExtra(Config.isPromoApplied, false))
            vm.promoCode.set(intent.getStringExtra(Config.RECEIVE_PROMO_CODE))
            vm.getSelectedPackageDetails()
        }
    }

    private val noDriverReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.isTripBooked.set(false)
            stopProgressBar()
        }
    }
    private val editedAddressReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getIntExtra("addressChangeValue", 0) == 1) {
                vm.pickupAddress.set(intent.getStringExtra("address"))
                vm.pickupLatLng.set(
                    LatLng(
                        intent.getStringExtra("lat")!!.toDouble(),
                        intent.getStringExtra("lng")!!.toDouble()
                    )
                )
                googleMap?.let { vm.moveCamera(it, vm.pickupLatLng.get(), 16f) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeFrag()
            }
        })
        /* Promo code receiver for receiving promo from Apply promo dialog */
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            promoCodeReceiver,
            IntentFilter(Config.RECEIVE_PROMO_CODE)
        )

        /* Trip cancel due to no driver receiver */
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            noDriverReceiver,
            IntentFilter(Config.NO_DRIVER_FOUND)
        )

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            editedAddressReceiver,
            IntentFilter(Config.ADDRESS_EDIT_DIALOG)
        )
    }


    private val args: RentalFragArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)

        if (RentalData.flag) {
            vm.pickupLatLng.set(LatLng(RentalData.pickLat, RentalData.pickLng))
            vm.pickupAddress.set(RentalData.pickAddress)
            googleMap?.let {
                vm.moveCamera(it, vm.pickupLatLng.get(), 16f)
            }
            RentalData.reset()
        } else {
            vm.pickupAddress.set(args.pickAddress)
            vm.pickupLatLng.set(LatLng(args.pickLat.toDouble(), args.pickLng.toDouble()))

            val mapFragment =
                childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
            mapFragment!!.getMapAsync(this)


            binding.back.setOnClickListener { closeFrag() }
            vm.rideLaterDate.set(vm.translationModel.txt_now)
        }

        setupCancel()

    }


    override fun getLayoutId() = R.layout.fragment_rental

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    private fun closeFrag() {
        findNavController().popBackStack()
        parentFragmentManager.beginTransaction().remove(this@RentalFrag).commit()
    }

    var adapter: RentalPackageAdapter? = null
    var adapter2: RentalTypesAdapter? = null
    private fun setAdapter() {
        adapter = RentalPackageAdapter(ArrayList(), this)
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rentalPackages.layoutManager = mLayoutManager
        binding.rentalPackages.itemAnimator = DefaultItemAnimator()
        binding.rentalPackages.adapter = adapter

        adapter2 = RentalTypesAdapter(ArrayList(), this)
        val sLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.selectedPackageRentalTypes.layoutManager = sLayoutManager
        binding.selectedPackageRentalTypes.itemAnimator = DefaultItemAnimator()
        binding.selectedPackageRentalTypes.adapter = adapter2

        vm.getRentalPackageList()
    }

    override fun addList(packages: List<PackageModel.Package>) {
        adapter?.addList(packages)
    }

    override fun addSelectedPackageDetails(type: List<RentalPackageTypes.Type>) {
        var rentalTypeModel: RentalPackageTypes.Type? = null
        if (slugSelected != null) {
            type.forEachIndexed { index, element ->
                if (type[index].vehicle?.slug.equals(slugSelected))
                    rentalTypeModel = type[index]
            }
        } else {
            if (type.isNotEmpty())
                rentalTypeModel = type[0]
        }
        vm.rentalTypeModel = rentalTypeModel
        rentalTypeModel?.currencySymbol?.let { vm.currencySymbol = it }
        rentalTypeModel?.totalAmount.let { vm.estimatedFare.set("${vm.currencySymbol} ${Utilz.removeZero("$it")}") }
        rentalTypeModel?.totalAmountPromo?.let { vm.promoEstimatedFare.set("${vm.currencySymbol} ${Utilz.removeZero(it)}") }
        vm.showPromoNotApplicable.set(rentalTypeModel?.promoCode == 1 && rentalTypeModel?.totalAmountPromo == null)
        vm.showPromoAppliedTotalAmount.set(vm.isPromoApplied.get() && !vm.showPromoNotApplicable.get())

        rentalTypeModel?.vehicle?.slug?.let { vm.typeSlug = it }
        rentalTypeModel?.vehicle?.vehicleName?.let {
            vm.bookNowText.set("${vm.translationModel.txt_book_now} $it")
            vm.bookLaterText.set("${vm.translationModel.txt_schedule} $it")
        }
        vm.isTypesLoaded.set(true)
        if (vm.firebaseHelper == null)
            vm.initializeFireBaseObserver()
        vm.setDriverAvailability()
        type.forEachIndexed { index, element ->
            type[index].isSelected = type[index].vehicle?.slug.equals(slugSelected)
        }
        adapter2?.addList(type, slugSelected)
    }

    override fun setSelectedPackage(pkg: PackageModel.Package) {
        vm.packageModel = pkg
        pkg.slug?.let { vm.getSelectedPackageDetails() }
    }

    override fun setSelectedType(type: RentalPackageTypes.Type, slug: String?) {
        slugSelected = slug
        vm.rentalTypeModel = type
        vm.estimatedFare.set("${type.currencySymbol} ${Utilz.removeZero("${type.totalAmount}")}")
        type.totalAmountPromo?.let { vm.promoEstimatedFare.set("${vm.currencySymbol} ${Utilz.removeZero(it)}") }
        vm.showPromoNotApplicable.set(type.promoCode == 1 && type.totalAmountPromo == null)
        vm.showPromoAppliedTotalAmount.set(vm.isPromoApplied.get() && !vm.showPromoNotApplicable.get())
        type.vehicle?.slug?.let { vm.typeSlug = it }
        type.vehicle?.vehicleName?.let {
            vm.bookNowText.set("${vm.translationModel.txt_book_now} $it")
            vm.bookLaterText.set("${vm.translationModel.txt_schedule} $it")
        }
        vm.clearMaps()
        vm.setDriverAvailability()
    }

    var googleMap: GoogleMap? = null
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        vm.changeMapStyle(googleMap, requireContext())
        vm.moveCamera(googleMap!!, vm.pickupLatLng.get(), 16f)
        setAdapter()
    }

    override fun pickupAddressChange() {
        val addressEdit =
            AddressEditDialog.newInstance(
                vm.pickupLatLng.get()!!,
                "pickup",
                vm.pickupAddress.get()!!,
                TAG
            )
        addressEdit.show(requireActivity().supportFragmentManager, AddressEditDialog.TAG)
    }

    override fun getAct(): Activity? {
        return if (isAdded)
            requireActivity()
        else
            null
    }

    override fun getGMap(): GoogleMap? {
        return googleMap
    }

    override fun setETA() {
        adapter2?.setETA(vm.eta.get()!!)
    }

    override fun openPromoDialog() {
        val promo = ApplyPromoDialog(vm.promoCode.get()!!, vm.isPromoApplied.get(),"RENTAL")
        promo.show(requireActivity().supportFragmentManager, ApplyPromoDialog.TAG)

    }

    private var defaultDate = Date() /* This date is used if ride later applied already and opened
    ride later dialog again then we can set this date as default date . This is initialized when a date is selected*/
    private val mFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    private val mTimeFormatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
    private val mDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateShortFormat = SimpleDateFormat("dd,MMM", Locale.ENGLISH)
    private val timeShortFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

    override fun openScheduleBottomSheet() {
        val scheduleDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        scheduleDialog.setContentView(R.layout.layout_schedule_bottom)
        scheduleDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val tLaterTitle = scheduleDialog.findViewById<TextView>(R.id.txt_schedule_title)
        val tLaterText1 = scheduleDialog.findViewById<TextView>(R.id.txt_schedule_text_1)
        val tLaterText2 = scheduleDialog.findViewById<TextView>(R.id.txt_schedule_text_2)
        val reset = scheduleDialog.findViewById<TextView>(R.id.cancel_booking)
        val picker = scheduleDialog.findViewById<SingleDateAndTimePicker>(R.id.schedule_picker)
        val bBook = scheduleDialog.findViewById<TextView>(R.id.btn_schedule_book)

        tLaterTitle?.text = vm.translationModel.txt_pickupTime
        tLaterText1?.text = vm.translationModel.txt_schedule_ride
        tLaterText2?.text = vm.translationModel.Txt_Schedule_Alert
        reset?.text = vm.translationModel.txt_reset_now
        bBook?.text = vm.translationModel.txt_ConfirmBooking
        picker!!.setDefaultDate(defaultDate)


        reset!!.setOnClickListener {
            resetRideLaterDetails(picker)
        }

        bBook!!.setOnClickListener { v: View? ->
            val currentDate: String = mDateFormatter.format(Date())
            val selectedDate: String = mDateFormatter.format(picker.date)
            try {
                if (mDateFormatter.parse(currentDate)
                        .compareTo(mDateFormatter.parse(selectedDate)) == 0
                ) {
                    val now = Calendar.getInstance()
                    now.add(Calendar.MINUTE, 29)
                    val After30Time: String = mTimeFormatter.format(now.time)
                    val Selectedtime: String =
                        mTimeFormatter.format(picker.date)
                    val different: Long =
                        mTimeFormatter.parse(After30Time).time - mTimeFormatter.parse(
                            Selectedtime
                        ).time
                    if (different < 0) {
                        saveRideLaterDetails(picker)
                        scheduleDialog.dismiss()
                    } else {
                        vm.translationModel.Txt_Schedule_Alert?.let { showMessage(it) }
                    }
                } else if (mDateFormatter.parse(currentDate)
                        .compareTo(mDateFormatter.parse(selectedDate)) == -1
                ) {
                    saveRideLaterDetails(picker)
                    scheduleDialog.dismiss()
                } else {
                    vm.translationModel.Txt_Schedule_Alert?.let { showMessage(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        if (!isdialogShown) {
            scheduleDialog.show()
            isdialogShown = true
        }
        scheduleDialog.setOnDismissListener { isdialogShown = false }
    }

    private fun resetRideLaterDetails(picker: SingleDateAndTimePicker?) {
        vm.rideType.set(1)

        vm.dateTimeRideLater.set("")
        vm.rideLaterDate.set(vm.translationModel.txt_now)
        vm.rideLaterTime.set("")

        defaultDate = Date()

        picker?.setDefaultDate(Date())
    }

    private fun saveRideLaterDetails(picker: SingleDateAndTimePicker?) {
        vm.rideType.set(2)

        vm.dateTimeRideLater.set(mFormatter.format(picker?.date!!))
        vm.rideLaterDate.set(dateShortFormat.format(picker.date!!))
        vm.rideLaterTime.set(timeShortFormat.format(picker.date!!))

        defaultDate = picker.date!!
    }

    val mainHandler = Handler(Looper.getMainLooper())
    private val progressRunner = object : Runnable {
        override fun run() {
            vm.progressRate.set(vm.progressRate.get()?.plus(1))
            if (vm.progressRate.get() == 90)
                vm.progressRate.set(85)
            mainHandler.postDelayed(this, 2000)
        }
    }

    override fun runProgressBar() {
        mainHandler.post(progressRunner)
    }

    override fun stopProgressBar() {
        mainHandler.removeCallbacks(progressRunner)
        vm.progressRate.set(0)
    }

    @SuppressLint("SetTextI18n")
    override fun openSuccess() {
        val success = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        success.setContentView(R.layout.dialog_schedule_success)
        success.findViewById<TextView>(R.id.text1)?.text =
            vm.translationModel.text_ride_later_sucess
        success.findViewById<TextView>(R.id.text2)?.text = vm.translationModel.txt_schedule_at
        success.findViewById<TextView>(R.id.scheduled_time)?.text =
            "${vm.rideLaterDate.get()} ${vm.rideLaterTime.get()}"
        val button = success.findViewById<MaterialButton>(R.id.button)
        button?.text = vm.translationModel.text_ok
        button?.setOnClickListener {
            success.dismiss()
            closeFrag()
        }
        success.show()
    }

    override fun getBind(): FragmentRentalBinding {
        return binding
    }


    override fun getCtx(): Context {
        return context ?: requireContext()
    }

    override fun showConfirmAlert() {
        val confirmDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        confirmDialog.setContentView(R.layout.outstation_start_alert)
        val title = confirmDialog.findViewById<TextView>(R.id.start_title)
        val desc = confirmDialog.findViewById<TextView>(R.id.start_descrtiption)
        val cancel = confirmDialog.findViewById<MaterialButton>(R.id.cancel_start)
        val start_trip = confirmDialog.findViewById<MaterialButton>(R.id.start_trip)

        start_trip?.text = vm.translationModel.txt_confirm
        cancel?.text = vm.translationModel.text_cancel
        title?.text = vm.translationModel.txt_confirm_start
        if (vm.rideType.get() == 1)
            desc?.text = vm.translationModel.txt_sure_to_start
        else
            desc?.text =
                "${vm.translationModel.txt_are_you_sure_to_schedule} ${vm.rideLaterDate.get()} ${vm.rideLaterTime.get()}"
        cancel?.setOnClickListener {
            confirmDialog.dismiss()
        }
        start_trip?.setOnClickListener {

            if (vm.rideType.get() == 1)
                vm.createRequest()
            else
                vm.createRideLaterRequest()
            confirmDialog.dismiss()
        }
        confirmDialog.show()
    }


    private fun setupCancel() {
        binding.customSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (seekBar.progress > 55) {
                    seekBar.progress = 100
                    vm.cancelRequest()
                } else {
                    seekBar.progress = 0
//                    seekBar.thumb = resources.getDrawable(R.drawable.)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                if (progress > 95) {
//                    seekBar.thumb = resources.getDrawable(R.drawable.load_img1)
                }
            }
        })
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(promoCodeReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(noDriverReceiver)
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(editedAddressReceiver)
        mainHandler.removeCallbacks(progressRunner)
        super.onDestroy()
    }

    override fun showRentalEta() {
        vm.rentalTypeModel?.let { type ->
            val newInstance = RentalFairBottom.newInstance(vm.translationModel, type , "${vm.packageModel?.let { 
                it.hours+it.timeCostType+" "+it.km+vm.translationModel.txt_km
            }}")
            newInstance.show(requireActivity().supportFragmentManager, RentalFairBottom.TAG)
        }
    }

}