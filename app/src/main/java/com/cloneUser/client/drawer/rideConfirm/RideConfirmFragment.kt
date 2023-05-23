package com.cloneUser.client.drawer.rideConfirm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.view.KeyEvent
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.cloneUser.client.BR
import com.cloneUser.client.MyApplication
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.connection.responseModels.StopModel
import com.cloneUser.client.connection.responseModels.TypesModel
import com.cloneUser.client.databinding.FragmentRideConfirmationBinding
import com.cloneUser.client.dialogs.paymentType.PaymentTypeDialog
import com.cloneUser.client.dialogs.waitingProgress.WaitingProgressDialog
import com.cloneUser.client.drawer.applyPromo.ApplyPromoDialog
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationData
import com.cloneUser.client.drawer.rideConfirm.adapter.TypesAdapter
import com.cloneUser.client.drawer.rideConfirm.changeAddress.RideConfirmChangeAddress
import com.cloneUser.client.drawer.rideConfirm.localPriceDetails.LocalFairDetailsBottom
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.ContactList
import com.cloneUser.client.ut.Utilz
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.SupervisorJob
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class RideConfirmFragment : BaseFragment<FragmentRideConfirmationBinding, RideConfirmVM>(),
    RideConfirmNavigator, OnMapReadyCallback {
    companion object {
        const val TAG = "RideConfirmFragment"
    }

    private lateinit var binding: FragmentRideConfirmationBinding
    var adapter: TypesAdapter? = null
    var rideNow = "RIDE_NOW"
    var rideLater = "RIDE_LATER"
    lateinit var googleMap: GoogleMap
    private var isScheduleDialogShown = false
    private var callAdminDailogShown = false

    /* private val paymentTypeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.isPaymentSelected.set(true)
            vm.paymentType.set(intent.getStringExtra(Config.RECEIVE_PAYMENT_TYPE))
        }
    } */

    private val promoCodeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.isPromoApplied.set(intent.getBooleanExtra(Config.isPromoApplied, false))
            vm.promoCode.set(intent.getStringExtra(Config.RECEIVE_PROMO_CODE))
            if (vm.rideType.get() == 1)
                getTypesListForCurrentTime()
            else
                getTypesForRideLater()
        }
    }

    private val noDriverReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.isTripBooked.set(false)
            stopProgressBar()
        }
    }


    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(RideConfirmVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onClickBack()
            }
        })

        /* The below receiver is commented out due to payment method default cash*/
        /* LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
             paymentTypeReceiver,
             IntentFilter(Config.RECEIVE_PAYMENT_TYPE)
         ) */

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


    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(promoCodeReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(noDriverReceiver)
        job.cancel()
        mainHandler.removeCallbacks(progressRunner)
        super.onDestroy()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        vm.bookNowText.set(vm.translationModel.txt_book_now)
        vm.rideLaterDate.set(vm.translationModel.txt_now)
        vm.driverNotes = ""
        setupAddress()
        initializeMap()
        setupCancel()
        binding.backImg.setOnClickListener { onClickBack() }
    }

    override fun getLayoutId() = R.layout.fragment_ride_confirmation

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    private fun setupAddress() {
        vm.pickAddress.set(ConfirmDestinationData.pickAddress)
        if (ConfirmDestinationData.pickupLat.isNotEmpty() && ConfirmDestinationData.pickupLng.isNotEmpty()) {
            vm.pickupLatLng.set(
                LatLng(
                    ConfirmDestinationData.pickupLat.toDouble(),
                    ConfirmDestinationData.pickupLng.toDouble()
                )
            )
        } else {
            vm.pickupLatLng.set(LatLng(0.0, 0.0))
        }

        vm.isStopAdded.set(ConfirmDestinationData.multipleDrop)
        if (vm.isStopAdded.get()) {
            vm.stopAddress.set(ConfirmDestinationData.drop1Address)
            if (ConfirmDestinationData.drop1Lat.isNotEmpty() && ConfirmDestinationData.drop1Lng.isNotEmpty()) {
                vm.stopLatLng.set(
                    LatLng(
                        ConfirmDestinationData.drop1Lat.toDouble(),
                        ConfirmDestinationData.drop1Lng.toDouble()
                    )
                )
            } else {
                vm.stopLatLng.set(LatLng(0.0, 0.0))
            }

            val data = StopModel(
                vm.stopAddress.get()!!,
                vm.stopLatLng.get()!!.latitude,
                vm.stopLatLng.get()!!.longitude,
                1
            )
            vm.stopModel.add(data)


            vm.dropAddress.set(ConfirmDestinationData.drop2Address)
            if (ConfirmDestinationData.drop2Lat.isNotEmpty() && ConfirmDestinationData.drop1Lng.isNotEmpty()) {
                vm.dropLatLng.set(
                    LatLng(
                        ConfirmDestinationData.drop2Lat.toDouble(),
                        ConfirmDestinationData.drop2Lng.toDouble()
                    )
                )
            } else {
                vm.dropLatLng.set(LatLng(0.0, 0.0))
            }

        } else {
            vm.dropAddress.set(ConfirmDestinationData.drop1Address)
            if (ConfirmDestinationData.drop1Lat.isNotEmpty() && ConfirmDestinationData.drop1Lng.isNotEmpty()) {
                vm.dropLatLng.set(
                    LatLng(
                        ConfirmDestinationData.drop1Lat.toDouble(),
                        ConfirmDestinationData.drop1Lng.toDouble()
                    )
                )
            } else {
                vm.dropLatLng.set(LatLng(0.0, 0.0))
            }
        }
    }

    private fun setupAdapter() {
        adapter = TypesAdapter(ArrayList<TypesModel.ZoneTypePrice>(), vm.session, vm.mConnect, this)
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.typesRecycler.layoutManager = mLayoutManager
        binding.typesRecycler.itemAnimator = DefaultItemAnimator()
        binding.typesRecycler.adapter = adapter
        getTypesListForCurrentTime()
    }

    override fun closeFragment() {
        findNavController().popBackStack()
        parentFragmentManager.beginTransaction().remove(this@RideConfirmFragment).commit()
    }

    override fun openCallBottomSheet() {
        showErrorDialog()
    }

    private fun getTypesListForCurrentTime() {
        val currentDate: String = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        val currentTime: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        vm.getTypes(rideNow, currentDate, currentTime)
    }

    private fun getTypesForRideLater() {
        vm.getTypes(rideLater, vm.rideLaterApiDate, vm.rideLaterApiTime)
    }

    override fun addList(types: List<TypesModel.ZoneTypePrice>?) {
        adapter!!.addList(types!!)
    }

    override fun showSelectedPriceDetails() {
        vm.type?.let { type ->
            vm.typesModel?.let { typesModel ->
                val newInstance =
                    LocalFairDetailsBottom.newInstance(vm.translationModel, type, typesModel)
                if (newInstance.dialog?.isShowing == true)
                    newInstance.dialog?.dismiss()
                newInstance.show(childFragmentManager, LocalFairDetailsBottom.TAG)
            }
        }
    }

    override fun getCtx(): Context {
        return if (isAdded) requireContext() else MyApplication.appContext!!
    }

    override fun getAct(): Activity? {
        return if (isAdded) requireActivity() else null
    }

    override fun isAddedInAct(): Boolean {
        return isAdded
    }

    override fun openPromoDialog() {
        val promo = ApplyPromoDialog(vm.promoCode.get()!!, vm.isPromoApplied.get(),"LOCAL")
        promo.show(requireActivity().supportFragmentManager, ApplyPromoDialog.TAG)
    }

    override fun bind(): FragmentRideConfirmationBinding {
        return binding
    }

    override fun selectedSlug(zoneTypePrice: TypesModel.ZoneTypePrice) {
        vm.type = zoneTypePrice
        vm.typeSlug = zoneTypePrice.typeSlug ?: ""
        vm.setDriverAvailability()
        vm.clearMaps()
        vm.estimatedFare.set("${vm.typesModel?.currencySymble} ${Utilz.removeZero(zoneTypePrice.totalAmount?:"")}")
        vm.promoEstimatedFare.set("${vm.typesModel?.currencySymble} ${Utilz.removeZero(zoneTypePrice.promoTotalAmount ?: "")}")
        vm.bookNowText.set("${vm.translationModel.txt_book_now} ${vm.type?.typeName ?: ""}")
        vm.bookLaterText.set("${vm.translationModel.txt_schedule} ${vm.type?.typeName ?: ""}")
        vm.showPromoNotApplicable.set(vm.type?.promoCode == 1 && vm.type?.promoTotalAmount == null)
        vm.showPromoAppliedTotalAmount.set(vm.isPromoApplied.get() && !vm.showPromoNotApplicable.get())

    }

    override fun openPaymentMethodDialog(paymentTypes: MutableList<String>) {
        if (isAdded) {
            val prevFragment = childFragmentManager.findFragmentByTag(PaymentTypeDialog.TAG)
            if (prevFragment != null) {
                return
            }
            val newInstance =
                PaymentTypeDialog.newInstance(
                    vm.translationModel,
                    paymentTypes,
                    vm.paymentType.get()!!
                )
            newInstance.show(childFragmentManager, PaymentTypeDialog.TAG)
        }
    }

    override fun getGMap(): GoogleMap {
        return googleMap
    }

    override fun openWaitingProgressDialog(reqId: String) {
        val waitingProgressDialog = WaitingProgressDialog(reqId)
        waitingProgressDialog.show(
            requireActivity().supportFragmentManager,
            WaitingProgressDialog.TAG
        )

    }

    override fun getNavControl(): NavController {
        return findNavController()
    }

    override fun openTripPage(data: RequestData.Data) {
        vm.notifyWaitingProgress()
        if (isAdded)
            requireActivity().runOnUiThread {
                val json = Gson().toJson(data)
                val bundle = bundleOf("REQUEST_DATA" to json.toString())
                findNavController().navigate(R.id.ride_confirm_to_trip, bundle)
            }

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
        if (!isScheduleDialogShown) {
            scheduleDialog.show()
            isScheduleDialogShown = true
        }
        scheduleDialog.setOnDismissListener { isScheduleDialogShown = false }

        val tLaterTitle = scheduleDialog.findViewById<TextView>(R.id.txt_schedule_title)
        val tLaterText1 = scheduleDialog.findViewById<TextView>(R.id.txt_schedule_text_1)
        val tLaterText2 = scheduleDialog.findViewById<TextView>(R.id.txt_schedule_text_2)
        val reset = scheduleDialog.findViewById<TextView>(R.id.cancel_booking)
        val picker = scheduleDialog.findViewById<SingleDateAndTimePicker>(R.id.schedule_picker)
        val bBook = scheduleDialog.findViewById<TextView>(R.id.btn_schedule_book)

        tLaterTitle?.text = vm.translationModel.txt_pickup_time
        tLaterText1?.text = vm.translationModel.txt_schedule_ride
        tLaterText2?.text = vm.translationModel.txt_schedule_ride_desc
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
                        getTypesForRideLater()
                        scheduleDialog.dismiss()
                    } else {
                        vm.translationModel.Txt_Schedule_Alert?.let { showMessage(it) }
                    }
                } else if (mDateFormatter.parse(currentDate)
                        .compareTo(mDateFormatter.parse(selectedDate)) == -1
                ) {
                    saveRideLaterDetails(picker)
                    getTypesForRideLater()
                    scheduleDialog.dismiss()
                } else {
                    vm.translationModel.Txt_Schedule_Alert?.let { showMessage(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    private fun saveRideLaterDetails(picker: SingleDateAndTimePicker?) {
        vm.rideType.set(2)

        vm.dateTimeRideLater.set(mFormatter.format(picker?.date!!))
        vm.rideLaterDate.set(
            "${dateShortFormat.format(picker.date!!)} \n ${
                timeShortFormat.format(
                    picker.date!!
                )
            }"
        )
        vm.rideLaterTime.set(timeShortFormat.format(picker.date!!))

        defaultDate = picker.date!!
        vm.rideLaterApiDate = mDateFormatter.format(picker.date)
        vm.rideLaterApiTime = mTimeFormatter.format(picker.date)
    }

    private fun resetRideLaterDetails(picker: SingleDateAndTimePicker?) {
        vm.rideType.set(1)

        vm.dateTimeRideLater.set("")
        vm.rideLaterDate.set(vm.translationModel.txt_now)
        vm.rideLaterTime.set("")

        defaultDate = Date()
        vm.rideLaterApiDate = ""
        vm.rideLaterApiTime = ""

        picker?.setDefaultDate(Date())
        getTypesListForCurrentTime()
    }

    override fun navigateToMap() {
        if (findNavController().currentDestination?.label == "Ride Confirm") {
            findNavController().navigate(R.id.ride_confirm_to_map)
            parentFragmentManager.beginTransaction().remove(this@RideConfirmFragment).commit()
        }
    }

    override fun onClickBack() {
        if (findNavController().currentDestination?.label == "Ride Confirm") {
            findNavController().navigate(R.id.ride_confirm_to_map)
            parentFragmentManager.beginTransaction().remove(this@RideConfirmFragment).commit()
        }
    }

    override fun setETA() {
        adapter?.setETA(vm.eta.get()!!)
    }

    override fun getJob(): CompletableJob {
        return job
    }


    private fun initializeMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.ride_map) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        vm.changeMapStyle(googleMap, requireContext())
        vm.buildBound(googleMap)
        vm.setPickupDropMarkers()
        setupAdapter()

    }

    private val job = SupervisorJob()

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

    override fun showErrorDialog() {
        val errorDialog = context?.let { BottomSheetDialog(it, R.style.AppBottomSheetDialogTheme) }
        errorDialog?.setContentView(R.layout.call_admin_dialog)
        if (!callAdminDailogShown) {
            errorDialog?.show()
            callAdminDailogShown = true
        }
        errorDialog?.setOnDismissListener {
            callAdminDailogShown = false
        }


        val title = errorDialog?.findViewById<TextView>(R.id.log_title)
        val desc = errorDialog?.findViewById<TextView>(R.id.desc_log)
        val cancelButton = errorDialog?.findViewById<MaterialButton>(R.id.cancel_log)
        val callLayout = errorDialog?.findViewById<ConstraintLayout>(R.id.constraint_two)
        if (title != null) {
            title.text = vm.translationModel.txt_no_driver_found
        }
        if (desc != null) {
            desc.text = vm.translationModel.txt_plz_reach_us
        }
        if (cancelButton != null) {
            cancelButton.text = vm.translationModel.txt_plain_back
        }
        cancelButton?.setOnClickListener {
            if (vm.isTripBooked.get())
                vm.isTripBooked.set(false)
            errorDialog.dismiss()
        }
        callLayout?.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:${vm.translationModel.txt_admin_number}")
            view?.context?.startActivity(callIntent)

        }
        errorDialog?.setCancelable(false)
        errorDialog?.show()
        errorDialog!!.setOnKeyListener { arg0, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (vm.isTripBooked.get())
                    vm.isTripBooked.set(false)
                errorDialog.dismiss()
            }
            true
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

    private fun setupCancel() {
        binding.customSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
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

    var searchingDriver: BottomSheetDialog? = null
    override fun showApiWaiting() {
        if (searchingDriver?.isShowing == true)
            searchingDriver?.dismiss()
        searchingDriver = BottomSheetDialog(getCtx(), R.style.AppBottomSheetDialogTheme)
        searchingDriver?.setContentView(R.layout.searching_for_driver)
        val imageview : ImageView? = searchingDriver?.findViewById(R.id.loader)
        if (imageview != null) {
            Glide.with(requireActivity()).asGif().load(R.raw.loader_gif)
                .into(imageview)
        }
        searchingDriver?.findViewById<TextView>(R.id.preWaitingTxt)?.text =
            vm.translationModel.txt_request_being_submitted
        searchingDriver?.setCancelable(false)
        searchingDriver?.show()
    }

    override fun closeApiWaiting() {
        searchingDriver?.dismiss()
    }

    @SuppressLint("SetTextI18n")
    override fun openSuccess() {
        val success = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        success.setContentView(R.layout.dialog_schedule_success)
//        success.findViewById<TextView>(R.id.text1)?.text = vm.translationModel.txt_trip_successfully
        success.findViewById<TextView>(R.id.text1)?.text =
            vm.translationModel.text_ride_later_sucess
        success.findViewById<TextView>(R.id.text2)?.text = vm.translationModel.txt_schedule_at
        success.findViewById<TextView>(R.id.scheduled_time)?.text =
            "${vm.rideLaterDate.get()?.replace("\n","")}"
        val button = success.findViewById<MaterialButton>(R.id.button)
        button?.text = vm.translationModel.txt_ok
        button?.setOnClickListener {
            success.dismiss()
            navigateToMap()
        }
        success.show()
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
                    phone?.text?.length!! < 10 -> showMessage("Invalid phone contact number")
                    else -> {
                        ContactList.name = name?.text.toString()
                        ContactList.phone = phone?.text.toString()
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

    override fun gotoSearchplace() {

        RideConfirmChangeAddress.instance(vm.dropAddress.get().toString())
        if (findNavController().currentDestination?.label?.equals("Ride Confirm") == true)
            findNavController().navigate(R.id.ride_confirm_search_place)
    }

    override fun goToNodriverFound() {
        Toast.makeText(requireContext(),"No Driver Found",Toast.LENGTH_LONG).show()
    }

    fun setPaymentType(paymentType: String) {
        vm.paymentType.set(paymentType)
    }


}