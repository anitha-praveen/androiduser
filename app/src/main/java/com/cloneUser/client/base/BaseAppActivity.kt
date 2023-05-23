package com.cloneUser.client.base

import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.cloneUser.client.R
import com.cloneUser.client.dialogs.defaultDialog.DefaultDialog
import com.cloneUser.client.ut.Utilz
import dagger.android.support.DaggerAppCompatActivity

/**
 * This activity to be extended to All Activity
 * */
abstract class BaseAppActivity<T : ViewDataBinding, V : ViewModel> : DaggerAppCompatActivity() {
    private lateinit var mBinding: T
    private var backpressed = false

    //    @Inject
//    lateinit var session: SessionMaintainence
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, getLayoutId())
        mBinding.apply {
            lifecycleOwner = this@BaseAppActivity
            setVariable(getBR(), getVMClass())
        }
    }

    fun Activity.hideKeyboard(view: View) {
//        hideKeyboard(currentFocus ?: View(this))
    }

    //This receiever is for check the internet is enabled or not
    var internetReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkInternetState()
        }
    }

    fun showNetworkUnAvailable() {
        showNetworkDialog()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(internetReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        checkInternetState()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(internetReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        checkInternetState()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(internetReceiver)
    }

    lateinit var pBarNoInternet: ProgressBar
    lateinit var btnNoInternet: View
    var dialog_internet: Dialog? = null
    fun showNetworkDialog() {
        if (dialog_internet != null) if (!dialog_internet!!.isShowing) {
            dialog_internet!!.show()
            return
        } else return

        dialog_internet = Dialog(this)
        dialog_internet!!.setContentView(R.layout.no_internet)
        if (dialog_internet!!.window != null) {
            dialog_internet!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog_internet!!.window!!.getAttributes())
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            dialog_internet!!.window!!.setAttributes(layoutParams)
        }
        dialog_internet!!.findViewById<View>(R.id.btnNoInternet)
            .setOnClickListener { checkInternetState() }
        dialog_internet!!.show()
        dialog_internet!!.setCanceledOnTouchOutside(false)
        dialog_internet!!.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN  ) {
                if (backpressed)
                    finish()
                if (!backpressed)
                    showMessage(getString(R.string.text_double_tap_exit))
                backpressed = true
                Handler(Looper.getMainLooper()).postDelayed(
                    { backpressed = false }, 2000
                )
            }
            true
        }
    }

    private fun removeNetworkDialog() {
        if (dialog_internet != null) if (dialog_internet!!.isShowing()) {
            dialog_internet!!.setCancelable(true)
            dialog_internet!!.dismiss()
        }
    }

    fun getFCMDeviceToken() {
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
//            session.saveString(SessionMaintainence.FCM_TOKEN, token.toString())
            // Log and toast
            Log.d("RefreshTokenOLD", token.toString())
        })
    }

    @TargetApi(Build.VERSION_CODES.M)
    open fun requestPermissionsSafely(permissions: Array<String>, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun checkGranted(permissions: Array<String>): Boolean {
        for (per in permissions) {
            if (checkSelfPermission(per) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.M)
    open fun checkGranted(permissions: IntArray): Boolean {
        for (per in permissions) {
            if (per != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    fun checkInternetState() {
        if (isNetworkConnected())
            removeNetworkDialog()
        else showNetworkDialog()
    }

    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showSnackBar(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun isNetworkConnected(): Boolean {
        return Utilz.checkForInternet(this)
    }

    /* fun showNetworkUnAvailable() {
         Toast.makeText(this, "Network Unavailable, Connect to internet!", Toast.LENGTH_SHORT)
             .show()
     }
 */
    fun showDialog(msg: String) {
        val defaultDialog = DefaultDialog(msg)
        defaultDialog.show(supportFragmentManager, DefaultDialog.TAG)
    }


    fun getmBinding() = mBinding
    abstract fun getLayoutId(): Int
    abstract fun getBR(): Int
    abstract fun getVMClass(): V

}