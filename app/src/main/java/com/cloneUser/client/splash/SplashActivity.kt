package com.cloneUser.client.splash

import android.Manifest
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.card.MaterialCardView
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseAppActivity
import com.cloneUser.client.connection.responseModels.AvailableCountryAndKLang
import com.cloneUser.client.databinding.ActivitySplashBinding
import com.cloneUser.client.dialogs.disclosure.DisclosureDialog
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.loginOrSignup.LoginOrSignupActivity
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject

class SplashActivity : BaseAppActivity<ActivitySplashBinding, SplashVM>(), SplashNavigator {

    companion object {
        const val TAG = "SplashActivity"
    }

    private lateinit var binding: ActivitySplashBinding
    lateinit var mainHandler: Handler
    private val permissions = Runnable { requestPermission() }
    lateinit var fusedLocationClient: FusedLocationProviderClient


    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(SplashVM::class.java)
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
        // Glide.with(this).asGif().load(R.raw.roda_animation).into(binding.imgSpl)
        startFirebaseInitiations()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationPermissionReceiver,
            IntentFilter(Config.RECEIVE_LOCATION_PERMISSION_RESULT)
        )
        vm.getLanguageCodeApi()
    }

    private fun startFirebaseInitiations() {
        getFCMDeviceToken()
    }

    override fun getLayoutId() = R.layout.activity_splash

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    override fun startRequestingPermission() {
        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(permissions)
    }

    private fun requestPermission() {
        if (!checkGranted(Config.Array_permissions)) {
            val newInstance = DisclosureDialog(Config.Array_permissions, Config.REQUEST_PERMISSION)
            newInstance.show(supportFragmentManager, DisclosureDialog.TAG)
        } else {
            fetchCurrentLocation()
            if (!Utilz.isEmpty(vm.session.getString(SessionMaintainence.AccessToken)!!)) {
                callDrawerActivity()
            } else {
                startActivity(Intent(this, LoginOrSignupActivity::class.java))
                finish()
            }
        }
    }


    override fun translationForCurrentLanguage(model: AvailableCountryAndKLang) {
        if (Utilz.isEmpty(vm.session.getString(SessionMaintainence.CURRENT_LANGUAGE)!!))
            startRequestingPermission()
        else {
            model.languages!!.apply {
                forEach { languages ->
                    if (languages.code!! == vm.session.getString(SessionMaintainence.CURRENT_LANGUAGE)) {
                        vm.session.saveLong(
                            SessionMaintainence.TRANSLATION_TIME_OLD,
                            vm.session.getLong(SessionMaintainence.TRANSLATION_TIME_NOW)!!
                        )
                        vm.session.saveLong(
                            SessionMaintainence.TRANSLATION_TIME_NOW,
                            languages.updatedDate!!.toLong()
                        )
                        return@apply
                    }
                }
            }
            if ((vm.session.getLong(SessionMaintainence.TRANSLATION_TIME_OLD)) == 0L && (vm.session.getLong(
                    SessionMaintainence.TRANSLATION_TIME_NOW
                ) != 0L)
            ) {
                vm.session.saveLong(
                    SessionMaintainence.TRANSLATION_TIME_OLD,
                    vm.session.getLong(SessionMaintainence.TRANSLATION_TIME_NOW)!!
                )
            }
            if (vm.session.getLong(SessionMaintainence.TRANSLATION_TIME_NOW) != vm.session.getLong(
                    SessionMaintainence.TRANSLATION_TIME_OLD
                )
            ) {
                vm.taskId = 1
                vm.getSelectedLangBase(vm.session.getString(SessionMaintainence.CURRENT_LANGUAGE)!!)
            } else
                startRequestingPermission()
        }
    }

    var updateDialog: Dialog? = null
    override fun openAppUpdateDialog() {
        updateDialog = Dialog(this)
        if (updateDialog?.isShowing!!)
            updateDialog?.dismiss()
        updateDialog?.setContentView(R.layout.dialog_update_app)
        updateDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        updateDialog?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        updateDialog?.findViewById<MaterialCardView>(R.id.continueBtn)?.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
            updateDialog?.dismiss()
        }
        updateDialog?.setCancelable(false)
        updateDialog?.show()
    }

    override fun showCustomDialog(message: String) {
        showCustomDialog(message)
    }

    fun permissionsResult() {
        if (!Utilz.isEmpty(vm.session.getString(SessionMaintainence.AccessToken)!!)) {
            callDrawerActivity()
        } else {
            startActivity(Intent(this, LoginOrSignupActivity::class.java))
            finish()
        }
    }


    private val locationPermissionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            fetchCurrentLocation()
            permissionsResult()
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationPermissionReceiver)
        super.onDestroy()
    }

    /*
    Fetch current location and go to drawer activity
     */
    private fun fetchCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        vm.session.saveString(SessionMaintainence.CURRENT_LATITUDE, "")
        vm.session.saveString(SessionMaintainence.CURRENT_LONGITUDE, "")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(OnSuccessListener<Location?> { p0: Location? ->
            if (p0 != null) {
                vm.session.saveString(
                    SessionMaintainence.CURRENT_LATITUDE,
                    "" + p0.latitude
                )
                vm.session.saveString(
                    SessionMaintainence.CURRENT_LONGITUDE,
                    "" + p0.longitude
                )
            }

        })

    }

    private fun callDrawerActivity() {
        startActivity(Intent(this, DrawerActivity::class.java))
        finish()
    }

}