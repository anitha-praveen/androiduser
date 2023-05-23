package com.cloneUser.client.loginOrSignup.login

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.gms.location.LocationServices
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.AvailableCountryAndKLang
import com.cloneUser.client.connection.responseModels.Country
import com.cloneUser.client.databinding.FragmentLoginBinding
import com.cloneUser.client.dialogs.countrylist.CountryListDialog
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import javax.inject.Inject

import android.text.style.ClickableSpan

import android.widget.TextView.BufferType

import android.text.method.LinkMovementMethod


import android.widget.TextView
import java.util.*
import android.text.TextPaint
import android.util.Log

import android.text.Spanned

import android.text.SpannableString
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.cloneUser.client.dialogs.notification_disclosure.NotificationPermissionDisclosure
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.cloneUser.client.ut.StringGenerator
import com.cloneUser.client.ut.Utilz


class LoginFragment : BaseFragment<FragmentLoginBinding, LoginVM>(),
    LoginNavigator {
    companion object {
        const val TAG = "LoginFragment"
    }

    private var backPressed = false

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: FragmentLoginBinding
    lateinit var data: AvailableCountryAndKLang

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(LoginVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressed)
                    requireActivity().finishAffinity()
                else
                    showMessage("Press back again to exit")
                backPressed = true
                Handler(Looper.getMainLooper()).postDelayed(
                    { backPressed = false },
                    2000
                )
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        getFCMToken()
        vm.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        vm.requestLocationUpdates(requireActivity())
        setupCountryList()
        binding.privacyTxt.text =  vm.translationModel.txt_terms_complete

        setClickableHighLightedText(binding.privacyTxt, vm.translationModel.txt_privacy_policy?:"") {
            // TODO: do your stuff here
        }
        setClickableHighLightedText(binding.privacyTxt, vm.translationModel.txt_terms_of_use?:"") {
            // TODO: do your stuff here
        }
         //to generate hash string for sms retriver api
//        val stringGenerator = StringGenerator(context)
//        vm.printE("GeneratedHashString", stringGenerator.appSignatures[0])
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.e(TAG, "Notification permission granted")
            } else {
                if (isAdded) {
                    val prevFragment: Fragment? = childFragmentManager.findFragmentByTag(
                        NotificationPermissionDisclosure.TAG
                    )
                    if (prevFragment == null) {
                        val dialog =
                            NotificationPermissionDisclosure.newInstance(vm.translationModel)
                        dialog.show(childFragmentManager, NotificationPermissionDisclosure.TAG)
                    }
                }
            }
        }
    override fun getLayoutId() = R.layout.fragment_login

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm
    override fun hideKeyBoard() {
        hideKeyBoard()
    }

    override fun getContextAttached(): Context {
        return requireContext()
    }

    override fun openCountryListDialogue() {
        val newInstance = CountryListDialog(0)
        newInstance.show(requireActivity().supportFragmentManager, CountryListDialog.TAG)
    }

    override fun openOtp(phone: String, cc: String, ccId: String) {
        var ccID = ""
        if(phone=="9090909090") ccID = "aca7fe98-b65e-42d2-a318-d389fcda2de0" else ccID = ccId
        var bundle = bundleOf(Config.phone_number to phone,Config.country_code to cc,Config.country_code_id to ccId)
        findNavController().navigate(R.id.toOtp,bundle)
    }



    /*
    The below method setupCountryList will get the current location and check if the current location's
    country code matches with the country list obtained from splash . If matches then it will set it as
    default country else it will set default country as the first one from the country list
     */
    override fun setupCountryList() {
        data = vm.session.getAvailableCountryAndLanguages()!!
        data.country!!.forEach { country ->
            if (country.code!!.equals("IN", true) /*== vm.getCountryFromLatLng(
                    vm.currentLatlng.get()!!.latitude,
                    vm.currentLatlng.get()!!.longitude
                )*/
            ) {
                vm.countryModel = country
                return@forEach
            }
        }
        if (!vm.checkCountryModelIsInitialized())
            vm.countryModel = data.country!![0]

        vm.countryCode.set(vm.countryModel.dialCode)

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            countryReceiver,
            IntentFilter(Config.RECEIVE_COUNTRY)
        )
    }

    private val countryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            vm.countryModel = intent.getSerializableExtra(Config.country) as Country
            vm.countryCode.set(vm.countryModel.dialCode)
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(countryReceiver);
        super.onPause()
    }

    fun setClickableHighLightedText(
        tv: TextView,
        textToHighlight: String,
        onClickListener: View.OnClickListener?
    ) {
        val tvt = tv.text.toString()
        var ofe = tvt.indexOf(textToHighlight, 0)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                onClickListener?.onClick(textView)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(),R.color.black)
                    ds.typeface = ResourcesCompat.getFont(requireContext(),R.font.roboto_bold)
                ds.isUnderlineText = false
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
                tv.setText(wordToSpan, BufferType.SPANNABLE)
                tv.movementMethod = LinkMovementMethod.getInstance()
            }
            ofs = ofe + 1
        }
    }

    private fun getFCMToken(){
        FirebaseApp.initializeApp(requireContext())
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            vm.session.saveString(SessionMaintainence.FCM_TOKEN, token.toString())

        })
    }
}