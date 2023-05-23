package com.cloneUser.client.drawer.trip

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.cloneUser.client.BR
import com.cloneUser.client.MyApplication
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.CancelReason
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.databinding.FragmentTripBinding
import com.cloneUser.client.dialogs.cancelReasons.CancelReasonsDialog
import com.cloneUser.client.dialogs.tripCancelled.TripCancelledDialog
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.drawer.trip.nightPhotoBottom.NightPhotoBottom
import com.cloneUser.client.ut.Config
import javax.inject.Inject


class TripFragment : BaseFragment<FragmentTripBinding, TripVM>(),
    TripNavigator, OnMapReadyCallback {
    companion object {
        const val TAG = "TripFragment"
    }

    private lateinit var binding: FragmentTripBinding
    private val cancelDialog = CancelReasonsDialog()
    private var callingReqProPending = false

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(TripVM::class.java)
    }

    private val cancelReasonReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.reason = intent.getSerializableExtra("REASON") as? CancelReason.Reason
            vm.getUserLocationToCancel()
            vm.isLoading.value = true
        }
    }
    private val closeTripPage: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            close()
        }
    }

    private val addressChange: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isAdded) {
                if (dialog?.isShowing == true) {
                    dialog?.dismiss()
                    (requireActivity() as DrawerActivity).callRequestInProgress()
                }
            }

        }
    }

    var isArriveHandled = false
    var isStartHandled = false
    var isCompleteHandled = false
    var isCancelHandled = false

    /* For MyFirebaseMessagingService */
    private val tripArrived: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isArriveHandled) {
                isArriveHandled = true
                if (intent.hasExtra("TRIP_DATA")) {
                    vm.requestData =
                        Gson().fromJson(
                            intent.getStringExtra("TRIP_DATA"),
                            RequestData.Data::class.java
                        )
                    vm.setInitialData()
                } else {
                    vm.callReqProgress()
                }
            }
        }
    }

    /* For MyFirebaseMessagingService */
    private val tripStarted: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isStartHandled) {
                isStartHandled = true
                isArriveHandled = true
                if (intent.hasExtra("TRIP_DATA")) {
                    vm.requestData =
                        Gson().fromJson(
                            intent.getStringExtra("TRIP_DATA"),
                            RequestData.Data::class.java
                        )
                    vm.setInitialData()
                } else {
                    vm.callReqProgress()
                }
            }

        }
    }

    /* For MyFirebaseMessagingService */
    private val tripCompleted: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isCompleteHandled) {
                isCompleteHandled = true
                if (intent.hasExtra("TRIP_DATA")) {
                    vm.requestData =
                        Gson().fromJson(
                            intent.getStringExtra("TRIP_DATA"),
                            RequestData.Data::class.java
                        )
                    vm.setInitialData()
                } else {
                    vm.callReqProgress()
                }
            }
        }
    }

    /* For MyFirebaseMessagingService */
    private val tripCancel: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isCancelHandled) {
                isCancelHandled = true
                if (intent.hasExtra("TRIP_DATA")) {
                    vm.requestData =
                        Gson().fromJson(
                            intent.getStringExtra("TRIP_DATA"),
                            RequestData.Data::class.java
                        )
                    vm.setInitialData()
                } else {
                    vm.callReqProgress()
                }
            }
        }
    }

    private val uploadNightImg: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if(intent.hasExtra(Config.upload_status))
                vm.isDriverUploadImage = intent.getBooleanExtra(Config.upload_status,false)
            if(intent.hasExtra(Config.images))
                vm.nightImageDriver = intent.getStringExtra(Config.images)?:""
            if(intent.hasExtra(Config.retake_image))
                vm.isRetake = intent.getBooleanExtra(Config.retake_image,false)
            vm.checkForImageUpload()
        }
    }


    private val pushUploadNightImg: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.checkForImageUpload()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                /* on back press it stays on trip*/
            }
        })

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            cancelReasonReceiver, IntentFilter(Config.RECEIVE_CANCEL_REASON)
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            addressChange, IntentFilter(Config.ADDRESS_CHANGE)
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            closeTripPage, IntentFilter(Config.RECEIVE_CLOSE_TRIP)
        )
    }

    override fun onDestroy() {
        Config.isTripRefreshed = false
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(cancelReasonReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(closeTripPage)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        isArriveHandled = false
        isStartHandled = false
        isCompleteHandled = false
        isCancelHandled = false

        if (!Config.isTripRefreshed) {
            arguments?.let {
//            vm.requestData = it.getSerializable("REQUEST_DATA") as? RequestData.Data
                vm.requestData =
                    Gson().fromJson(it.getString("REQUEST_DATA"), RequestData.Data::class.java)

            }
        }
        initializeMap()
        //liftBottomSheet()
    }


    override fun getLayoutId() = R.layout.fragment_trip

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    override fun onMapReady(p0: GoogleMap) {
        vm.googleMap = p0
        vm.changeMapStyle(p0, requireContext())
        vm.setInitialData()
        vm.pickupLatLng.get()?.let {
            p0.moveCamera(
                CameraUpdateFactory.newLatLngZoom(it, 16f)
            )
        }
        vm.initializeFireBaseObserver()
        vm.setPickupDropMarkers()
    }

    private fun initializeMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.trip_map) as? SupportMapFragment
        mapFragment!!.getMapAsync(this)
        vm.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }


    override fun getCtx(): Context {
        return if (isAdded)
            requireContext()
        else
            MyApplication.appContext!!
    }

    override fun isAddedInAct(): Boolean {
        return isAdded
    }


    var invoicePending = false
    override fun invoice(data: RequestData.Data) {
        if (!DrawerActivity.isDrawer && isAdded) {
            requireActivity().runOnUiThread {
                val bundle = bundleOf("REQUEST_DATA" to data, "MODE" to 0)
                val requestData = bundle.getSerializable("REQUEST_DATA") as? RequestData.Data
                when (requestData?.serviceCategory) {
                    Config.rental -> {
                        (requireActivity() as DrawerActivity).navController.navigate(R.id.rental_invoice_frag, bundle)
                    }
                    Config.outstation -> {
                        (requireActivity() as DrawerActivity).navController.navigate(R.id.outstation_invoice_frag, bundle)
                    }
                    else -> (requireActivity() as DrawerActivity).navController.navigate(R.id.invoice, bundle)
                }
            }
        } else {
            invoicePending = true
        }
    }

    override fun cancelReasonDialog(reasonList: MutableList<CancelReason.Reason>) {
        cancelDialog.reasons.clear()
        cancelDialog.reasons.addAll(reasonList)
        if (!cancelDialog.isAdded)
            cancelDialog.show(requireActivity().supportFragmentManager, CancelReasonsDialog.TAG)
    }

    var tripCancelled: TripCancelledDialog? = null
    override fun showTripCancelled() {
        if (isAdded) {
            val intent = Intent(Config.CLOSE_NIGHT_IMG)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            if (tripCancelled?.isVisible == true)
                tripCancelled?.dismiss()
            tripCancelled = TripCancelledDialog()
            tripCancelled?.show(requireActivity().supportFragmentManager, TripCancelledDialog.TAG)
        }
    }

    override fun getAct(): Activity {
        return requireActivity()
    }

    override fun openSideMenu() {
        (mActivity as DrawerActivity).openSideMenu()
    }

    override fun close() {
        (mActivity as DrawerActivity).navigateFirstTabWithClearStack()
    }

    override fun goToSearchPlace(mode: String) {
        if (vm.pickupLatLng.get() != null && vm.dropLatLng.get() != null) {
            val action = TripFragmentDirections.tripToAddressChange(
                lat = if (mode == "pickup") "${vm.pickupLatLng.get()!!.latitude}" else "${vm.dropLatLng.get()!!.latitude}",
                lng = if (mode == "pickup") "${vm.pickupLatLng.get()!!.longitude}" else "${vm.dropLatLng.get()!!.longitude}",
                address = if (mode == "pickup") vm.pickupAddress.get()!! else vm.dropAddress.get()!!,
                type = mode,
                reqId = vm.requestId ?: "",
                destLat = if (mode == "pickup") "${vm.dropLatLng.get()!!.latitude}" else "",
                destLng = if (mode == "pickup") "${vm.dropLatLng.get()!!.longitude}" else ""
            )
            findNavController().navigate(action)
        }
    }

    var dialog: Dialog? = null
    override fun openApproveAlert() {
        dialog = BottomSheetDialog(getCtx(), R.style.AppBottomSheetDialogTheme)
        dialog!!.setContentView(R.layout.loc_approval_lay)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val title: TextView = dialog!!.findViewById(R.id.title)
        title.text = vm.translationModel.loc_driver_approval
        dialog!!.setCancelable(false)
        dialog!!.show()
    }


    override fun openReqProgrss() {
        callingReqProPending = if (!DrawerActivity.isDrawer && isAdded) {
            (requireActivity() as DrawerActivity).callRequestInProgress()
            true
        } else {
            true
        }
    }

    override fun openSos() {
        //mode one to close fragment
        val action = TripFragmentDirections.tripToSos(1)
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        Config.isTripRefreshed = false
        if (invoicePending)
            vm.requestData?.let { invoice(it) }
        if (callingReqProPending)
            openReqProgrss()

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            tripArrived, IntentFilter(Config.TRIP_ARRIVED)
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            tripStarted, IntentFilter(Config.TRIP_STARTED)
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            tripCompleted, IntentFilter(Config.TRIP_COMPLETED)
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            tripCancel, IntentFilter(Config.TRIP_CANCEL)
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            uploadNightImg, IntentFilter(Config.DRIVER_NIGHT_IMAGE)
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            pushUploadNightImg, IntentFilter(Config.PUSH_UPLOAD_NIGHT)
        )
    }

    override fun onPause() {
        super.onPause()
        if (Config.isReqCalled)
            Config.isReqCalled = false
        else
            Config.isTripRefreshed = true

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(tripArrived)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(tripStarted)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(tripCompleted)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(tripCancel)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(uploadNightImg)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(pushUploadNightImg)
    }

    override fun showTakePhotoDialog() {
        val newInstance = NightPhotoBottom.newInstance(vm.translationModel,vm.nightImageDriver,vm.isRetake)
        if(newInstance.dialog?.isShowing == true)
            newInstance.dialog?.dismiss()
        newInstance.show(childFragmentManager , NightPhotoBottom.TAG)
    }

     fun openCamera() {
        if ( ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("com.google.assistant.extra.USE_FRONT_CAMERA", true)
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)

            // Samsung
            intent.putExtra("camerafacing", "front")
            intent.putExtra("previous_mode", "front")

            // Huawei
            intent.putExtra("default_camera", "1")
            intent.putExtra("default_mode", "com.huawei.camera2.mode.photo.PhotoMode")
            resultLauncherCamera.launch(intent)
        }
    }

    var resultLauncherCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                vm.onCaptureImageResult(data!!)
            }
        }


    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra("com.google.assistant.extra.USE_FRONT_CAMERA", true)
                    putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                    putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                    putExtra("android.intent.extras.CAMERA_FACING", 1)

                    // Samsung
                    putExtra("camerafacing", "front")
                    putExtra("previous_mode", "front")

                    // Huawei
                    putExtra("default_camera", "1")
                    putExtra("default_mode", "com.huawei.camera2.mode.photo.PhotoMode")
                }
                resultLauncherCamera.launch(intent)

                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }


}