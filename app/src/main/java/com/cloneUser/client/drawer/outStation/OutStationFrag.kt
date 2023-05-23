package com.cloneUser.client.drawer.outStation

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.OutStationTypes
import com.cloneUser.client.connection.responseModels.OutstationModel
import com.cloneUser.client.databinding.FragmentOutstationBinding
import com.cloneUser.client.drawer.applyPromo.ApplyPromoDialog
import com.cloneUser.client.drawer.outStation.listOutStation.OutStationListFragment
import com.cloneUser.client.drawer.outStation.types.ChildTypesOutStationAdapter
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.Utilz
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.gms.location.LocationServices
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
import kotlin.math.roundToInt


class OutStationFrag : BaseFragment<FragmentOutstationBinding, OutStationVM>(),
    OutStationNavigator, OnMapReadyCallback {
    companion object {
        const val TAG = "OutStationFrag"
    }

    private var isScheduleDialogShown = false
    private var isOutStationEtaShown = false

    private lateinit var binding: FragmentOutstationBinding
    private lateinit var googleMap: GoogleMap
    var setWithInitalLoaction = ObservableBoolean(true)
    private lateinit var adapter: ChildTypesOutStationAdapter
    private val newInstance = OutStationListFragment()

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(OutStationVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeFrag()
            }
        })
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            getSelectedPlace, IntentFilter(Config.GETOUTSTATIONSELECTEDPLACE)
        )

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            pickUpAddress, IntentFilter(Config.RECEIVE_PICKUP_DETAILS)
        )

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            promoCodeReceiver,
            IntentFilter(Config.RECEIVE_PROMO_CODE)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        binding.back.setOnClickListener { closeFrag() }
        setupGoogleMap()
        setTypesAdapter()

        if (vm.showTypes.get()) {
            if (adapter.itemCount == 0)
                vm.getOutsationTypes()
        }
    }

    fun setTypesAdapter() {
        adapter = ChildTypesOutStationAdapter(ArrayList(), this, vm.translationModel)
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.typesRecycler.layoutManager = mLayoutManager
        binding.typesRecycler.itemAnimator = DefaultItemAnimator()
        binding.typesRecycler.adapter = adapter
    }

    /*to get pickup lat lng and address form outstation search place */
    private val getSelectedPlace: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setCurrentTime()
            vm.outStationListItems =
                intent.getSerializableExtra(Config.outStationSelcetedPlace) as OutstationModel
            vm.dropAddress.set(vm.outStationListItems!!.drop)
            vm.dropLatLng.set(vm.outStationListItems!!.dropLat?.let {
                vm.outStationListItems!!.dropLng?.let { it1 ->
                    LatLng(
                        it,
                        it1
                    )
                }
            })
            vm.toTxt.set(vm.dropAddress.get())
            val map = HashMap<String, String>()
            map[Config.outstation_id] = vm.outStationListItems!!.id.toString()
            vm.getOutsationTypes()
//            vm.getPolyString()

        }
    }

    private val pickUpAddress: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setWithInitalLoaction.set(false)
            val lat = intent.getDoubleExtra(Config.latitude, 0.0)
            val long = intent.getDoubleExtra(Config.longitude, 0.0)
            val address = intent.getStringExtra(Config.pick_address)
            vm.latLng.set(LatLng(lat, long))
            vm.address.set(address)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(getSelectedPlace)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(promoCodeReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(pickUpAddress)
    }

    private val promoCodeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.isPromoApplied.set(intent.getBooleanExtra(Config.isPromoApplied, false))
            vm.promoCode.set(intent.getStringExtra(Config.RECEIVE_PROMO_CODE))
            vm.getOutsationTypes()
        }
    }

    override fun getLayoutId() = R.layout.fragment_outstation

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    private fun closeFrag() {
//        findNavController().navigate(R.id.outStation_to_map)
        findNavController().popBackStack()
        parentFragmentManager.beginTransaction().remove(this@OutStationFrag).commit()
    }

    private fun setupGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
        vm.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        if (setWithInitalLoaction.get()) {
            vm.setInitialLocation()
        } else {
            vm.moveCamera(googleMap, vm.latLng.get(), vm.zoom)
        }
        context?.let { vm.changeMapStyle(googleMap, it) }

    }

    override fun getCtx(): Context? {
        return context
    }

    override fun getMap(): GoogleMap {
        return googleMap
    }

    override fun gotoSearchPlace() {
        findNavController().navigate(R.id.toOutstation_search)
    }

    override fun goSelectOutStation() {
        if (!newInstance.isAdded)
            newInstance.show(requireActivity().supportFragmentManager, OutStationListFragment.TAG)
    }

    override fun typesSelected(types: OutStationTypes) {
        vm.selectedType = types
        vm.selectedTypeId = types.id
        vm.priceTxt.set("${types.currencySymbol} ${Utilz.removeZero("${types.totalAmount}")}")
        vm.infoTotal.set("${types.currencySymbol} ${Utilz.removeZero("${types.totalAmount}")}")
        vm.typeSlug.set(types.getVehicle?.slug ?: "")
        vm.promoAmount.set("${types.currencySymbol} ${Utilz.removeZero("${types.totalAmountPromo}")}")
        if (vm.isTwoWay.get())
            vm.distanceTxt.set("${(types.distance!!.roundToInt())} ${vm.translationModel.txt_km}")
        else
            vm.distanceTxt.set("${vm.selectedType?.distance!!.roundToInt()} ${vm.translationModel.txt_km}")
        //vm.distanceTxt.set("${vm.selectedType?.distance.toString()} *2 (${(vm.selectedType?.distance?.toInt() ?: 0.0) * 2})")
        vm.basePrice.set("${vm.translationModel.txt_base_charge}: ${types.currencySymbol}${Utilz.removeZero("${types.distancePrice}")}")
        vm.driverBata.set("${vm.translationModel.txt_driver_beta}: ${types.currencySymbol} ${Utilz.removeZero("${types.driverPrice}")}")
        vm.scheduleButtontxt.set("${vm.translationModel.text_schedule} ${types.getVehicle?.vehicleName}")
        vm.confirmButtonTxt.set("${vm.translationModel.txt_confirm} ${types.getVehicle?.vehicleName}")
//        vm.distancePrice.set("${types.distancePrice}")
        vm.baseFare.set("${types.currencySymbol} ${types.baseFare?.let { Utilz.removeZero("${vm.round(it, 3)}") }}")
        if (vm.isTwoWay.get())
            vm.kmPrice.set("(${types.currencySymbol} ${Utilz.removeZero("${vm.selectedType?.distancePriceTwoWay}")}/${vm.translationModel.txt_km})")
        else
            vm.kmPrice.set("(${types.currencySymbol} ${Utilz.removeZero("${vm.selectedType?.distancePrice}")}/${vm.translationModel.txt_km})")
    }

    override fun loadTypesAdapter(types: MutableList<OutStationTypes>) {
        vm.showTypes.set(true)
        adapter.addList(types, vm.isTwoWay.get())

        if (vm.selectedTypeId != null) {
            if (types.any { it.id == vm.selectedType?.id }) {
                vm.selectedType = types.find { it.id == vm.selectedTypeId }

                vm.priceTxt.set("${vm.selectedType?.currencySymbol} ${Utilz.removeZero("${vm.selectedType?.totalAmount}")}")
                vm.infoTotal.set("${vm.selectedType?.currencySymbol} ${Utilz.removeZero("${vm.selectedType?.totalAmount}")}")
                vm.typeSlug.set(vm.selectedType?.getVehicle?.slug ?: "")
                vm.promoAmount.set("${vm.selectedType?.currencySymbol} ${Utilz.removeZero("${vm.selectedType?.totalAmountPromo}")}")
                try {
                    if (vm.isTwoWay.get()) {
                        vm.distanceTxt.set("${(vm.selectedType?.distance!!.roundToInt())}${vm.translationModel.txt_km}")
                        //  vm.distancePrice.set("${vm.translationModel.txt_distance} ${vm.selectedType?.distance.toString()} *2 (${(vm.selectedType?.distance?.toInt() ?: 0.0) * 2})")
                    } else {
                        vm.distanceTxt.set("${vm.selectedType?.distance!!.roundToInt()} ${vm.translationModel.txt_km}")

                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
                vm.basePrice.set("${vm.selectedType?.currencySymbol}${Utilz.removeZero("${vm.selectedType?.distancePrice}")}")
                vm.driverBata.set("${vm.translationModel.txt_driver_beta}: ${Utilz.removeZero("${vm.selectedType?.driverPrice}")}")
                vm.scheduleButtontxt.set("${vm.translationModel.text_schedule} ${vm.selectedType?.getVehicle?.vehicleName}")
                vm.confirmButtonTxt.set("${vm.translationModel.txt_confirm} ${vm.selectedType?.getVehicle?.vehicleName}")
                if (vm.isTwoWay.get())
                    vm.kmPrice.set("(${vm.selectedType!!.currencySymbol}${Utilz.removeZero("${vm.selectedType?.distancePriceTwoWay}")}/${vm.translationModel.txt_km})")
                else
                    vm.kmPrice.set("(${vm.selectedType!!.currencySymbol}${Utilz.removeZero("${vm.selectedType?.distancePrice}")}/${vm.translationModel.txt_km})")
                vm.baseFare.set(
                    "${vm.selectedType?.currencySymbol} ${vm.selectedType?.baseFare?.let { Utilz.removeZero("${vm.round(it, 3)}")}}")

            }
        } else if (types.size > 0) {
            vm.selectedType = types[0]

            vm.priceTxt.set("${types[0].currencySymbol} ${Utilz.removeZero("${types[0].totalAmount}")}")
            vm.infoTotal.set("${types[0].currencySymbol} ${Utilz.removeZero("${types[0].totalAmount}")}")
            vm.typeSlug.set(types[0].getVehicle?.slug ?: "")
            vm.promoAmount.set("${types[0].currencySymbol} ${Utilz.removeZero("${types[0].totalAmountPromo}")}")
            try {
                if (vm.isTwoWay.get()) {
                    vm.distanceTxt.set("${(types[0].distance!!.roundToInt())}${vm.translationModel.txt_km}")
                    // vm.distancePrice.set("${vm.translationModel.txt_distance} ${vm.selectedType?.distance.toString()} *2 (${(vm.selectedType?.distance?.toInt() ?: 0.0) * 2})")
                } else {

                    vm.distanceTxt.set("${types[0].distance!!.roundToInt()} ${vm.translationModel.txt_km}")
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            vm.basePrice.set("${types[0].currencySymbol} ${Utilz.removeZero("${types[0].distancePrice}")}")
            vm.driverBata.set("${vm.translationModel.txt_driver_beta}: ${types[0].currencySymbol} ${Utilz.removeZero("${types[0].driverPrice}")}")
            vm.scheduleButtontxt.set("${vm.translationModel.text_schedule} ${types[0].getVehicle?.vehicleName}")
            vm.confirmButtonTxt.set("${vm.translationModel.txt_confirm} ${types[0].getVehicle?.vehicleName}")
            if (vm.isTwoWay.get())
                vm.kmPrice.set("(${vm.selectedType!!.currencySymbol} ${Utilz.removeZero("${vm.selectedType?.distancePriceTwoWay}")}/${vm.translationModel.txt_km})")
            else
                vm.kmPrice.set("(${vm.selectedType!!.currencySymbol} ${Utilz.removeZero("${vm.selectedType?.distancePrice}")}/${vm.translationModel.txt_km})")
            vm.baseFare.set(
                "${types[0].currencySymbol} ${types[0].baseFare?.let { Utilz.removeZero("${vm.round(it, 3)}")}}"
            )

        }
    }

    private var defaultDate = Date()

    override fun openScheduleBottomSheet(type: Boolean) {
        val scheduleDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        scheduleDialog.setContentView(R.layout.outstation_schedule_bottom)
        scheduleDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED


        val tLaterTitle = scheduleDialog.findViewById<TextView>(R.id.txt_schedule_title)
        val tLaterText1 = scheduleDialog.findViewById<TextView>(R.id.txt_schedule_text_1)
        val tLaterText2 = scheduleDialog.findViewById<TextView>(R.id.txt_schedule_text_2)
        val reset = scheduleDialog.findViewById<TextView>(R.id.cancel_booking)
        val picker = scheduleDialog.findViewById<SingleDateAndTimePicker>(R.id.schedule_picker)
        val bBook = scheduleDialog.findViewById<TextView>(R.id.btn_schedule_book)
        val relativeLayout = scheduleDialog.findViewById<RelativeLayout>(R.id.relativeLayout)
        bBook?.text = vm.translationModel.txt_confirm
        picker?.setCustomLocale(Locale.ENGLISH)
        if (vm.isTwoWay.get()) {
            if (type) {
                tLaterTitle?.text = vm.translationModel.txt_schedule_two_return_on
                relativeLayout?.visibility = View.GONE
            } else {
                tLaterTitle?.text = vm.translationModel.txt_scedule_your_two_pick_time
                relativeLayout?.visibility = View.VISIBLE
            }
        } else {
            tLaterTitle?.text = vm.translationModel.txt_scedule_one_way_pick
            relativeLayout?.visibility = View.VISIBLE
        }
        tLaterText1?.text = vm.translationModel.txt_schedule_ride
        tLaterText2?.text = vm.translationModel.txt_schedule_ride_desc
        reset?.text = vm.translationModel.txt_reset_now
        bBook?.text = vm.translationModel.txt_ConfirmBooking
        picker!!.setDefaultDate(defaultDate)


        val mFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val showFormatter = SimpleDateFormat("MMM,d hh:mm a",Locale.ENGLISH)
        val mTimeFormatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        val mDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateShortFormat = SimpleDateFormat("dd,MMM", Locale.ENGLISH)
        val timeShortFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        reset!!.setOnClickListener {
            vm.dateTimeRideLater.set("")
            vm.rideLaterDate.set(vm.translationModel.txt_now)
            vm.rideLaterTime.set("")
            defaultDate = Date()
            picker.setDefaultDate(Date())
            vm.isDateSelected.set(false)
        }

        bBook!!.setOnClickListener { v: View? ->
            val currentDate: String = mDateFormatter.format(Date())
            val selectedDate: String = mDateFormatter.format(picker.date)
            try {
                if (mDateFormatter.parse(currentDate)
                        .compareTo(mDateFormatter.parse(selectedDate)) == 0
                ) {
                    val now = Calendar.getInstance()
                    if (type)
                        now.add(Calendar.HOUR, 3)
                    else
                        now.add(Calendar.MINUTE, 29)
                    val After30Time: String = mTimeFormatter.format(now.time)
                    val Selectedtime: String =
                        mTimeFormatter.format(picker.date)
                    val different: Long =
                        mTimeFormatter.parse(After30Time).time - mTimeFormatter.parse(
                            Selectedtime
                        ).time
                    if (different < 0) {
                        /* we can call ride later */

                        if (vm.isTwoWay.get()) {
                            if (!type) {
                                vm.editedStartTime.set(mFormatter.format(picker.date))
                                vm.showCurrentTime.set(showFormatter.format(picker.date))
                                scheduleDialog.dismiss()
                                vm.getOutsationTypes()
                            } else {
                                vm.editedEndTime.set(mFormatter.format(picker.date))
                                vm.showEndTime.set(showFormatter.format(picker.date))
                                scheduleDialog.dismiss()
                                vm.isReturnDateSet.set(true)
                                vm.getOutsationTypes()
                            }
                        } else {
                            vm.rideLaterDate.set(dateShortFormat.format(picker.date))
                            vm.rideLaterTime.set(timeShortFormat.format(picker.date))
                            vm.dateTimeRideLater.set(mFormatter.format(picker.date))
                            defaultDate = picker.date
                            vm.showCalender.set(true)
                            vm.isDateSelected.set(true)
                            scheduleDialog.dismiss()
                        }
                    } else {
                        if (!type)
                            vm.translationModel.Txt_Schedule_Alert?.let { showMessage(it) }
                        else
                            vm.translationModel.txt_three_hrs_alert?.let { showMessage(it) }

                    }
                } else if (mDateFormatter.parse(currentDate)
                        .compareTo(mDateFormatter.parse(selectedDate)) == -1
                ) {
                    /* we can call ride later */

                    if (vm.isTwoWay.get()) {
                        if (!type) {
                            vm.editedStartTime.set(mFormatter.format(picker.date))
                            vm.showCurrentTime.set(showFormatter.format(picker.date))
                            scheduleDialog.dismiss()
                            vm.getOutsationTypes()
                        } else {
                            vm.editedEndTime.set(mFormatter.format(picker.date))
                            vm.showEndTime.set(showFormatter.format(picker.date))
                            scheduleDialog.dismiss()
                            vm.isReturnDateSet.set(true)
                            vm.getOutsationTypes()
                        }
                    } else {
                        vm.dateTimeRideLater.set(mFormatter.format(picker.date))
                        vm.rideLaterDate.set(dateShortFormat.format(picker.date))
                        vm.rideLaterTime.set(timeShortFormat.format(picker.date))
                        vm.showCalender.set(true)
                        vm.isDateSelected.set(true)
                        scheduleDialog.dismiss()
                    }
                } else {
                    vm.translationModel.Txt_Schedule_Alert?.let { showMessage(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!isScheduleDialogShown) {
            scheduleDialog.show()
            isScheduleDialogShown = true
        }
        scheduleDialog.setOnDismissListener { isScheduleDialogShown = false }


    }

    override fun openPromoDialog() {
        val promo = ApplyPromoDialog(vm.promoCode.get()!!, vm.isPromoApplied.get(),"OUTSTATION")
        promo.show(requireActivity().supportFragmentManager, ApplyPromoDialog.TAG)
    }

    @SuppressLint("SetTextI18n")
    override fun openSuccess() {
        val success = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        success.setContentView(R.layout.dialog_schedule_success)
//        success.findViewById<TextView>(R.id.text1)?.text = vm.translationModel.txt_trip_successfully
        success.findViewById<TextView>(R.id.text1)?.text =
            vm.translationModel.text_ride_later_sucess
        success.findViewById<TextView>(R.id.text2)?.text = vm.translationModel.txt_schedule_at
        if (!vm.isTwoWay.get())
            success.findViewById<TextView>(R.id.scheduled_time)?.text =
                "${vm.rideLaterDate.get()} ${vm.rideLaterTime.get()}"
        else
            success.findViewById<TextView>(R.id.scheduled_time)?.text =
                "${vm.showCurrentTime.get()}"
        val button = success.findViewById<MaterialButton>(R.id.button)
        button?.text = vm.translationModel.txt_ok
        button?.setOnClickListener {
            success.dismiss()
            closeFrag()
        }
        success.show()
    }

    override fun toCallAdmin() {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:${vm.translationModel.txt_admin_number}")
        startActivity(callIntent)
    }

    @SuppressLint("SetTextI18n")
    override fun showOutstationEta() {
        val outStationEta = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        outStationEta.setContentView(R.layout.outstation_eta_bottom_sheet)
        val totalPrice = outStationEta.findViewById<TextView>(R.id.total_out_eta)
        val title = outStationEta.findViewById<TextView>(R.id.title_out_eta)
        if (vm.isPromoApplied.get())
            totalPrice?.text = vm.promoAmount.get()
        else
            totalPrice?.text = vm.infoTotal.get()
        val descTitle = outStationEta.findViewById<TextView>(R.id.desc_title)
//        val descOne = outStationEta.findViewById<TextView>(R.id.desc_one)
        val descTwo = outStationEta.findViewById<TextView>(R.id.desc_two)
        //       val descThree = outStationEta.findViewById<TextView>(R.id.desc_three)
        val descFour = outStationEta.findViewById<TextView>(R.id.desc_four)
        val descFive = outStationEta.findViewById<TextView>(R.id.desc_five)
        val descSix = outStationEta.findViewById<TextView>(R.id.desc_six)
        val twoWayDescription = outStationEta.findViewById<TextView>(R.id.two_way_desc)
        val estimatedFareDesc = outStationEta.findViewById<TextView>(R.id.estimated_fare_desc)
        // val descBaseFare = outStationEta.findViewById<TextView>(R.id.desc_baseFare)

        twoWayDescription?.visibility = if (vm.isTwoWay.get()) View.GONE else View.VISIBLE
        title?.text = vm.translationModel.txt_total_fare_estimation
//        basePricetitle?.text =
//            "(${vm.distanceTxt.get()} * ${vm.translationModel.num_two}(${vm.translationModel.txt_up_downs}) " +
//                    "* ${vm.distancePrice.get()}/${vm.translationModel.txt_km}) + ${vm.translationModel.text_setvice_tax}"

//        twoWayDescription?.text = "${vm.translationModel.two_way_desc} (${vm.distanceTxt.get()} * ${vm.translationModel.num_two})"
        twoWayDescription?.text = vm.translationModel.txt_both_up_down_desc

//        descOne?.text = "-${vm.translationModel.txt_plain_exclude} ${vm.driverBata.get()}"
        descTwo?.text = "-${vm.translationModel.txt_exclude_toll}"
//        descThree?.text = "-${vm.translationModel.txt_hill_additional_charges}"
        descFour?.text = "-${vm.translationModel.txt_drop_extra_charges}"
        descFive?.text = "-${vm.translationModel.txt_return_empty_charge}"
        descSix?.text = "-${vm.translationModel.txt_ex_interstate_permits}"
        estimatedFareDesc?.text = "(${vm.translationModel.txt_plain_estimated_fare})"
        //descBaseFare?.text = "-${vm.translationModel.txt_below_km_basefare_apply} (${vm.baseFare.get()})"


        descTitle?.text = vm.translationModel.txt_plain_description
        if (!isOutStationEtaShown) {
            outStationEta.show()
            isOutStationEtaShown = true
        }
        outStationEta.setOnDismissListener { isOutStationEtaShown = false }

        val distTxt = outStationEta.findViewById<TextView>(R.id.distanceCostTxt)
        val distValue = outStationEta.findViewById<TextView>(R.id.distanceCostValue)
        val hillLayout = outStationEta.findViewById<LinearLayout>(R.id.hillCostLayout)
        val hillTxt = outStationEta.findViewById<TextView>(R.id.hillCostTxt)
        val hillVal = outStationEta.findViewById<TextView>(R.id.hillCostValue)
        val driverCostTxt = outStationEta.findViewById<TextView>(R.id.driverCommTxt)
        val driverCostVal = outStationEta.findViewById<TextView>(R.id.driverCommValue)


        vm.selectedType?.distance?.let { distance ->
            if (vm.isTwoWay.get()) {
                vm.selectedType?.distancePriceTwoWay?.let { price ->
                    distTxt?.text =
                        "${vm.translationModel.text_distance_cost} ((${Utilz.removeZero("$distance")} ${vm.translationModel.txt_km} x 2) x ${vm.selectedType?.currencySymbol} ${Utilz.removeZero("$price")})"
                    distValue?.text = "${vm.selectedType?.currencySymbol} ${Utilz.removeZero("${distance * 2 * price}")}"
                }
            } else {
                vm.selectedType?.distancePrice?.let { price ->
                    distTxt?.text =
                        "${vm.translationModel.text_distance_cost} ((${Utilz.removeZero("$distance")} ${vm.translationModel.txt_km} x 2) x ${vm.selectedType?.currencySymbol} ${Utilz.removeZero("$price")})"
                    distValue?.text = "${vm.selectedType?.currencySymbol} ${Utilz.removeZero("${distance * 2 * price}")}"
                }
            }
        }

        if (vm.outStationListItems?.hillStation.equals("YES", true))
            hillLayout?.visibility = View.VISIBLE
        else
            hillLayout?.visibility = View.GONE
        if (vm.selectedType?.hillStationPrice != null && vm.selectedType?.hillStationPrice!! > 0) {
            hillTxt?.text = "${vm.translationModel.txt_hill_price}"
            hillVal?.text =
                "${vm.selectedType?.currencySymbol} ${Utilz.removeZero("${vm.selectedType?.hillStationPrice}")}"
        }
        driverCostTxt?.text =
            if (vm.isTwoWay.get()) vm.translationModel.txt_day_rent else "${vm.translationModel.txt_driver_beta}"
        driverCostVal?.text =
            "${vm.selectedType?.currencySymbol} ${Utilz.removeZero("${vm.selectedType?.driverPrice}")}"
        outStationEta.findViewById<TextView>(R.id.desc_seven)?.text ="-"+vm.translationModel.txt_rental_grace_time_desc

    }

    override fun showConfirmation() {
        val confirmDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        confirmDialog.setContentView(R.layout.outstation_start_alert)
        val title = confirmDialog.findViewById<TextView>(R.id.start_title)
        val desc = confirmDialog.findViewById<TextView>(R.id.start_descrtiption)
        val cancel = confirmDialog.findViewById<MaterialButton>(R.id.cancel_start)
        val start_trip = confirmDialog.findViewById<MaterialButton>(R.id.start_trip)
        if (desc != null) {
            setHighLightedText(desc,vm.showCurrentTime.get()!!)
        }
        start_trip?.text = vm.translationModel.txt_confirm
        title?.text = vm.translationModel.txt_confirm_start
        if (vm.isTwoWay.get()){
            desc?.text =
                "${vm.translationModel.txt_trip_assigned} ${vm.showCurrentTime.get()} ${vm.translationModel.txt_to} ${vm.showEndTime.get()}"
            if (desc != null) {
                setHighLightedText(desc,vm.showCurrentTime.get()!!)
                setHighLightedText(desc,vm.showEndTime.get()!!)
            }
        }
        else{
            desc?.text =
                "${vm.translationModel.txt_outstation_start_alert} ${vm.rideLaterDate.get()} ${vm.rideLaterTime.get()}"
            if (desc != null) {
                setHighLightedText(desc,vm.rideLaterDate.get()!!)
                setHighLightedText(desc,vm.rideLaterTime.get()!!)
            }
        }
        cancel?.setOnClickListener {
            confirmDialog.dismiss()
        }
        start_trip?.setOnClickListener {
            confirmDialog.dismiss()
            vm.createRequest()
        }
        confirmDialog.show()
    }

    fun setHighLightedText(
        tv: TextView,
        textToHighlight: String) {
        val tvt = tv.text.toString()
        var ofe = tvt.indexOf(textToHighlight, 0)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(),R.color.black)
                ds.typeface = ResourcesCompat.getFont(requireContext(),R.font.roboto_bold)
                ds.isUnderlineText = false
            }

            override fun onClick(p0: View) {

            }
        }
        val wordToSpan = SpannableString(tv.text)
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(textToHighlight, ofs)
            if (ofe == -1) break else {
                wordToSpan.setSpan(
                    clickableSpan,
                    ofe,
                    ofe + textToHighlight.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE)
                tv.movementMethod = LinkMovementMethod.getInstance()
            }
            ofs = ofe + 1
        }
    }


    override fun setCurrentTime() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, 30)
        val extCal = Calendar.getInstance()
        extCal.add(Calendar.HOUR,3)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH)
        val showSdf = SimpleDateFormat("MMM,d hh:mm a",Locale.ENGLISH)
        val endSsdf = SimpleDateFormat("MMM,d",Locale.ENGLISH)
        vm.showCurrentTime.set(showSdf.format(cal.time))
        vm.editedStartTime.set(sdf.format(cal.time))
        vm.extraTime.set(sdf.format(extCal.time))
        val endCal = Calendar.getInstance()
        vm.editedEndTime.set(sdf.format(endCal.time))
        vm.showEndTime.set(endSsdf.format(endCal.time) + " " + "(${vm.translationModel.txt_return})")
    }


}