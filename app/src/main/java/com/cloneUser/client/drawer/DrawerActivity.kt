package com.cloneUser.client.drawer

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.cloneUser.client.BR
import com.cloneUser.client.BuildConfig
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseAppActivity
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.databinding.ActivityDrawerBinding
import com.cloneUser.client.databinding.NavHeaderDrawerBinding
import com.cloneUser.client.loginOrSignup.LoginOrSignupActivity
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DrawerActivity : BaseAppActivity<ActivityDrawerBinding, DrawerVM>(), DrawerNavigator,
    NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val TAG = "DrawerActivity"
        var isDrawer = false
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDrawerBinding
    lateinit var navController: NavController
    var mDrawer: DrawerLayout? = null
    private lateinit var navHeaderDrawerBinding: NavHeaderDrawerBinding
    private var isErrorDialogShown = false

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(DrawerVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        if (!vm.session.getString(SessionMaintainence.CURRENT_LANGUAGE).isNullOrEmpty()) {
            vm.setLanguage(this, vm.session.getString(SessionMaintainence.CURRENT_LANGUAGE)!!)
        }else{
            vm.setLanguage(this,"en")
        }
        setup()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            profilePicUpdated,
            IntentFilter(Config.NOTIFY_DRAWER_IMAGE_CHANGED)
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(
            profileEditReceiver,
            IntentFilter(Config.PROFILE_NAME)
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(
            tripAccepted,
            IntentFilter(Config.TRIP_ACCEPTED)
        )
        vm.versionCode.set("${vm.translationModel.txt_app_version} " + "v" + BuildConfig.VERSION_NAME)
    }

    private val profilePicUpdated: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.requestInProgress()
        }
    }

    private val profileEditReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.firstName.set(intent.getStringExtra("NAME"))
        }
    }

    private val tripAccepted: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (navController.currentDestination?.label != "Trip")
                callRequestInProgress()
        }
    }

    private val locToRentPush: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e("LOC_TO_RENT", "Inside receiver")
            callRequestInProgress()
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(profilePicUpdated)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(profileEditReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tripAccepted)
        super.onDestroy()
    }

    private lateinit var navHostFragment: NavHostFragment
    private fun setup() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        findViewById<NavigationView>(R.id.nav_view)
            .setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)
        setUpNavigationHeader()
        setUpNavMenuTranslation()
        vm.countryCode.set(vm.session.getString(SessionMaintainence.SelectedCountryCode))
        mDrawer = binding.drawerLayout
        mDrawer!!.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                vm.showProfilePic.set(false)
            }

            override fun onDrawerOpened(drawerView: View) {
                vm.showProfilePic.set(false)
            }

            override fun onDrawerClosed(drawerView: View) {
                checkDrawer()
            }

            override fun onDrawerStateChanged(newState: Int) {
                if (!mDrawer!!.isOpen)
                    checkDrawer()
            }
        })
        vm.requestInProgress()

    }

    private fun checkDrawer() {
        if (navController.currentDestination?.label == "Map")
            vm.showProfilePic.set(true)
    }

    private fun setUpNavigationHeader() {
        navHeaderDrawerBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.nav_header_drawer, binding.navView, false
        )
        binding.navView.addHeaderView(navHeaderDrawerBinding.root)
        navHeaderDrawerBinding.viewModel = vm
        binding.navView.setNavigationItemSelectedListener(this)
        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.FirstFragment) {
                vm.showProfilePic.set(true)
            } else {
                vm.showProfilePic.set(false)
            }
        }
        navHeaderDrawerBinding.name.isSelected = true
    }

    override fun getLayoutId() = R.layout.activity_drawer

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        // Handle navigation view item clicks here.
        val id = item.itemId
        when (id) {
            R.id.nav_home -> {
                vm.requestInProgress()
            }
            R.id.nav_profile -> {
                if (navController.currentDestination?.label == "Map")
                    navController.navigate(R.id.map_to_profile)
                else if (navController.currentDestination?.label == "Trip")
                    navController.navigate(R.id.trip_to_profile)
            }
            R.id.nav_history -> {
                if (navController.currentDestination?.label == "Map")
                    navController.navigate(R.id.map_to_history_list)
                else if (navController.currentDestination?.label == "Trip")
                    navController.navigate(R.id.trip_to_history)
            }
//            R.id.nav_fav -> {
//                if (navController.currentDestination?.label == "Map")
//                    navController.navigate(R.id.map_to_favorites)
//                else if (navController.currentDestination?.label == "Trip")
//                    navController.navigate(R.id.trip_to_favorites)
//            }
            R.id.nav_logout -> {
                if (navController.currentDestination?.label == "Map")
                    showLogOutDialog()
            }
            R.id.nav_support -> {
                if (navController.currentDestination?.label == "Map")
                    navController.navigate(R.id.map_to_support)
                else if (navController.currentDestination?.label == "Trip")
                    navController.navigate(R.id.trip_to_support)
            }
            R.id.nav_suggestion -> {
                if (navController.currentDestination?.label == "Map")
                    navController.navigate(R.id.map_to_suggestion)
                else if (navController.currentDestination?.label == "Trip")
                    navController.navigate(R.id.trip_to_suggestion)
            }
            R.id.nav_wallet -> {
                if (navController.currentDestination?.label == "Map")
                    navController.navigate(R.id.map_to_wallet)
                else if (navController.currentDestination?.label == "Trip")
                    navController.navigate(R.id.trip_to_wallet)

            }
            R.id.nav_notification -> {
                if (navController.currentDestination?.label == "Map")
                    navController.navigate(R.id.map_to_notification)
                else if (navController.currentDestination?.label == "Trip")
                    navController.navigate(R.id.trip_to_notification)

            }
            R.id.nav_referral -> {
                if (navController.currentDestination?.label == "Map")
                    navController.navigate(R.id.map_to_refferal)
//                else if (navController.currentDestination?.label == "Trip")
//                    navController.navigate(R.id.trip_to_notification)

            }
            R.id.nav_about_us -> {
                if (navController.currentDestination?.label == "Map")
                    navController.navigate(R.id.map_to_aboutus)
            }
            R.id.nav_language -> {
                    navController.navigate(R.id.language)
            }
            R.id.nav_sos -> {
                if (navController.currentDestination?.label == "Map")
                    navController.navigate(R.id.map_to_sos)
                if (navController.currentDestination?.label == "Confirm Destination")
                    navController.navigate(R.id.confirm_destination_to_sos)
                if (navController.currentDestination?.label == "Trip")
                    navController.navigate(R.id.trip_to_sos)
                if (navController.currentDestination?.label == "wallet")
                    navController.navigate(R.id.walltet_to_sos)
                if (navController.currentDestination?.label == "notification")
                    navController.navigate(R.id.notifcation_to_sos)
            }
        }
        vm.showProfilePic.set(false)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showLogOutDialog() {
        val logDialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
        logDialog.setContentView(R.layout.logout_dialog)
        logDialog.show()


        val title = logDialog.findViewById<TextView>(R.id.log_title)
        val desc = logDialog.findViewById<TextView>(R.id.desc_log)
        val cancelButton = logDialog.findViewById<MaterialButton>(R.id.cancel_log)
        val logOUtButton = logDialog.findViewById<MaterialButton>(R.id.logOut_button)
        if (title != null) {
            title.text = vm.translationModel.txt_logout
        }
        if (desc != null) {
            desc.text = vm.translationModel.text_desc_logout
        }
        if (cancelButton != null) {
            cancelButton.text = vm.translationModel.text_cancel
        }
        if (logOUtButton != null) {
            logOUtButton.text = vm.translationModel.txt_logout
        }
        logOUtButton?.setOnClickListener {
            vm.getLogutApiVm()
            logDialog.dismiss()

        }
        cancelButton?.setOnClickListener {
            logDialog.dismiss()
        }
        logDialog.setCancelable(true)
        logDialog.show()
        logDialog.setOnKeyListener { arg0, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                logDialog.dismiss()
            }
            true
        }

    }

    override fun openSideMenu() {
        if (!mDrawer!!.isDrawerOpen(GravityCompat.START)) mDrawer!!.openDrawer(GravityCompat.START)
    }

    override fun logoutNav() {
        vm.session.saveString(SessionMaintainence.AccessToken, "")
        startActivity(Intent(this, LoginOrSignupActivity::class.java))
        finish()
    }

    override fun openTrip(data: RequestData.Data) {
        while (navController.currentBackStackEntry != null) {
            navController.popBackStack()
        }

        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph_drawer)
        graph.setStartDestination(R.id.trip)
        val navController = navHostFragment.navController
        val reqData = NavArgument.Builder().setDefaultValue(Gson().toJson(data)).build()
        graph.addArgument("REQUEST_DATA", reqData)
        navController.graph = graph

    }

    /**
     * Clear all the back fragments and opens the fresh one.
     */
    fun navigateFirstTabWithClearStack() {
        while (navController.currentBackStackEntry != null) {
            navController.popBackStack()
        }
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph_drawer)
        graph.setStartDestination(R.id.FirstFragment)
        val navController = navHostFragment.navController
        navController.graph = graph
//        reqProgress()
    }

    override fun openInvoice(data: RequestData.Data) {
        val bundle = bundleOf("REQUEST_DATA" to data, "MODE" to 0)
        if (data.serviceCategory == Config.rental) {
            if (navController.currentDestination?.label == "Map") {
                navController.navigate(R.id.map_to_rental_invoice, bundle)
            }

        } else if (data.serviceCategory == Config.outstation) {
            if (navController.currentDestination?.label == "Map") {
                navController.navigate(R.id.map_to_outStation_invoice, bundle)
            }
        } else {
            if (navController.currentDestination?.label == "Map")
                navController.navigate(R.id.map_to_invoice, bundle)
        }
    }

    override fun closeDrawer() {
        if (mDrawer != null) {
            if (mDrawer!!.isOpen)
                mDrawer!!.close()
        }
    }

    override fun openSos() {
        if (navController.currentDestination?.label == "Map")
            navController.navigate(R.id.map_to_sos)
        else if (navController.currentDestination?.label == "Trip")
            navController.navigate(R.id.trip_to_sos)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun getAct(): Activity {
        return this
    }

    override fun openProfile() {
        if (navController.currentDestination?.label == "Map")
            navController.navigate(R.id.map_to_profile)
        else if (navController.currentDestination?.label == "Trip")
            navController.navigate(R.id.trip_to_profile)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun showErrorDialog() {
        // if (navController.currentDestination?.label == "Ride Confirm" || navController.currentDestination?.label == "Rental")
        //showCallDialog()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun goToNodriverFound(id: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext,"No Driver Found",Toast.LENGTH_LONG).show()
        }
    }

    var meterDialog: Dialog? = null
    override fun showMeterPic(url: String, string: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (meterDialog?.isShowing == true)
                meterDialog?.dismiss()
            meterDialog = BottomSheetDialog(this@DrawerActivity, R.style.AppBottomSheetDialogTheme)
            meterDialog?.setContentView(R.layout.dialog_trip_km)
            meterDialog?.findViewById<ImageView>(R.id.close)
                ?.setOnClickListener { meterDialog?.dismiss() }
            val image = meterDialog?.findViewById<ImageView>(R.id.meterImg)
            meterDialog?.findViewById<TextView>(R.id.title)?.text =
                vm.translationModel.txt_verfiy_trip_start_km
            meterDialog?.findViewById<TextView>(R.id.description)?.text =
                vm.translationModel.txt_start_km_desc
            meterDialog?.findViewById<TextView>(R.id.meterValue)?.text =
                vm.translationModel.txt_vehicle_start_km + " : " + string
            val okay = meterDialog?.findViewById<MaterialButton>(R.id.okay)
            okay?.text = vm.translationModel.txt_okay
            okay?.setOnClickListener { meterDialog?.dismiss() }
            if (image != null) {
                Glide.with(this@DrawerActivity).load(url).apply(
                    RequestOptions.errorOf(R.drawable.ic_user)
                        .placeholder(R.drawable.ic_user)
                ).into(image)
            }
            image?.setOnClickListener {
                showImage(vm.translationModel.txt_verfiy_trip_start_km ?: "", url, false)
            }
            meterDialog?.show()
        }
    }

    override fun hideMeterDialog() {
        if (meterDialog?.isShowing == true)
            meterDialog?.dismiss()
    }

    var imageDialog: Dialog? = null
    fun showImage(title: String, url: String, enableOption: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (imageDialog?.isShowing == true)
                imageDialog?.dismiss()
            imageDialog = Dialog(this@DrawerActivity)
            imageDialog?.setContentView(R.layout.dialog_show_image)
            val profileTitle = imageDialog?.findViewById<TextView>(R.id.profile_txt)
            val camera = imageDialog?.findViewById<TextView>(R.id.camera)
            val profile = imageDialog?.findViewById<ImageView>(R.id.profile_image)
            camera?.text = vm.translationModel.text_camera
            camera?.visibility = if (enableOption) View.VISIBLE else View.GONE
            profileTitle?.text = title
            if (profile != null) {
                Glide.with(applicationContext).load(url).apply(
                    RequestOptions.errorOf(R.drawable.ic_user)
                        .placeholder(R.drawable.ic_user)
                ).into(profile)
            }
            imageDialog?.show()
        }
    }


    private fun showCallDialog() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val errorDialog =
                    BottomSheetDialog(this@DrawerActivity, R.style.AppBottomSheetDialogTheme)
                errorDialog.setContentView(R.layout.call_admin_dialog)
                if (!isErrorDialogShown) {
                    errorDialog.show()
                    isErrorDialogShown = true
                }
                val title = errorDialog.findViewById<TextView>(R.id.log_title)
                val desc = errorDialog.findViewById<TextView>(R.id.desc_log)
                val cancelButton = errorDialog.findViewById<MaterialButton>(R.id.cancel_log)
                val callLayout = errorDialog.findViewById<ConstraintLayout>(R.id.constraint_two)
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
                    errorDialog.dismiss()
                }
                callLayout?.setOnClickListener {
                    val callIntent = Intent(Intent.ACTION_DIAL)
                    callIntent.data = Uri.parse("tel:${vm.translationModel.txt_admin_number}")
                    startActivity(callIntent)

                }
                errorDialog.setOnDismissListener {
                    isErrorDialogShown = false
                }
                errorDialog.setCancelable(false)
                errorDialog.show()
                errorDialog.setOnKeyListener { arg0, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        errorDialog.dismiss()
                    }
                    true
                }
            } catch (t: Throwable) {
                vm.printE("CouroutinesFailure", "Failed")
            }
        }
    }


    override fun showCustomDialog(message: String) {
        showDialog(message)
    }

    override fun getCtx(): Context {
        return this
    }

    override fun setImage(imageURL: String) {
        Glide.with(this).load(imageURL).apply(
            RequestOptions.circleCropTransform().error(R.drawable.simple_profile_bg)
                .placeholder(R.drawable.simple_profile_bg)
        ).into(navHeaderDrawerBinding.drawerProPic)

//        Glide.with(this).load(imageURL).apply(
//            RequestOptions.circleCropTransform().error(R.drawable.ic_user)
//                .placeholder(R.drawable.ic_user)
//        ).into(binding.menuHam)

    }

    /*
    This method is also  called from other fragments
     */
    fun callRequestInProgress() {
        vm.requestInProgress()
    }

    private fun setUpNavMenuTranslation() {
        binding.navView.menu.findItem(R.id.nav_home).title = vm.translationModel.txt_Home
//        binding.navView.menu.findItem(R.id.nav_profile).title = (vm.translationModel.txt_my_profile)
        binding.navView.menu.findItem(R.id.nav_history).title = vm.translationModel.txt_my_rides
        //binding.navView.menu.findItem(R.id.nav_fav).title = vm.translationModel.txt_Fav_side_menu
        binding.navView.menu.findItem(R.id.nav_notification).title =
            vm.translationModel.txt_noitification
//        binding.navView.menu.findItem(R.id.nav_wallet).title = vm.translationModel.txt_wallet
        binding.navView.menu.findItem(R.id.nav_support).title = vm.translationModel.txt_support
        binding.navView.menu.findItem(R.id.nav_language).title = vm.translationModel.txt_Lang
        binding.navView.menu.findItem(R.id.nav_about_us).title = vm.translationModel.txt_plain_about
        binding.navView.menu.findItem(R.id.nav_referral).title = vm.translationModel.text_title_Referralcode
//        binding.navView.menu.findItem(R.id.nav_suggestion).title =
//            vm.translationModel.txt_suggestion
//        binding.navView.menu.findItem(R.id.nav_logout).title = vm.translationModel.txt_logout
    }

    private fun checkLocationTurnedOn() {
        val mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val mGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!mGPS) {
            val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_turn_on_location)
            val text = dialog.findViewById<TextView>(R.id.log_title)
            val desc = dialog.findViewById<TextView>(R.id.desc_log)
            desc?.text = vm.translationModel.txt_turn_location_on
            text?.text = vm.translationModel.txt_location_mandatory
            val ok = dialog.findViewById<MaterialButton>(R.id.ok)
            ok?.text = vm.translationModel.Txt_Continue
            ok?.setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            dialog.show()

        }
    }

    override fun onResume() {
        super.onResume()
        isDrawer = true
        checkLocationTurnedOn()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locToRentPush,
            IntentFilter(Config.LOC_TO_RENT_PUSH)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            locToRentPush
        )

    }

    override fun onBackPressed() {
        if (mDrawer?.isOpen == true)
            closeDrawer()
        else
            super.onBackPressed()
    }
}