package com.cloneUser.client.loginOrSignup.otp

import android.app.Activity
import android.content.*
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.cloneUser.client.BR
import com.cloneUser.client.MySMSBroadcastReceiver
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.FragmentOtpBinding
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.loginOrSignup.login.LoginFragment
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SessionMaintainence
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class OtpFragment : BaseFragment<FragmentOtpBinding, OtpVM>(),
    OtpNavigator {
    companion object {
        const val TAG = "OtpFrag"
    }

    //auto fill
    val task = context?.let { SmsRetriever.getClient(it).startSmsUserConsent(null) }
    var mAuth: FirebaseAuth? = null
    private lateinit var binding: FragmentOtpBinding

    var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mVerificationId = ""
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var otpView: OTPView? = null
    private var mDatabase: DatabaseReference? = null
    var phoneOtp = HashMap<String, String>()

    private var intentFilter: IntentFilter? = null
    private var smsReceiver: MySMSBroadcastReceiver? = null

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(OtpVM::class.java)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        getFCMToken()
        binding.backImg.setOnClickListener { closeFrag() }
        arguments?.let {
            vm.phoneNumber.set(it.getString(Config.phone_number))
            vm.countryCode.set(it.getString(Config.country_code))
            vm.countryCodeId.set(it.getString(Config.country_code_id))
        }


        vm.checkSmsText.set("${vm.translationModel.txt_chk_sms} \n${vm.countryCode.get()} ${vm.phoneNumber.get()}")
        otpView = binding.optCustomview
        //  setUpFirebaseOTP()
        vm.callotpApi()
        vm.startTimer(60000)
        mDatabase = FirebaseDatabase.getInstance().reference
        binding.otp.requestFocus()
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        //  recieveSms()
//
//
//        initSmsRetriver()
//        initBroadCast()
//
//        context!!.registerReceiver(smsReceiver, intentFilter)
//    }


    override fun getLayoutId() = R.layout.fragment_otp

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                vm.printE("HellowWorld", data.toString())

            }
        }

//    override fun onResume() {
//        super.onResume()
//        context!!.registerReceiver(smsReceiver, intentFilter)
//    }

    override fun onPause() {
        super.onPause()
        vm.isEnabled.set(true)
        //  context!!.unregisterReceiver(smsReceiver)
    }

    /**
     * not used
     */
    private fun setUpFirebaseOTP() {
        mAuth = FirebaseAuth.getInstance()
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                vm.isLoading.value = false

                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e("VerifiFailed===", "onVerificationFailed", e)
                Log.e("VerifiFailed===", "onVerificationFailedDetails" + e.localizedMessage)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
//                    Toast.makeText(OTPActivity.this, getTranslatedString(R.string.txt_sms_excedd), Toast.LENGTH_SHORT).show();
                }

                // Show a message and update the UI
                // ...

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                vm.isLoading.value = false
                Log.d(tag, "onCodeSent:$verificationId")
                //                Toast.makeText(getApplicationContext(), "OTP sent", Toast.LENGTH_SHORT).show();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
                vm.isLoading.value = false
            }
        }

        sendVerificationCode(vm.countryCode.get() + vm.phoneNumber.get())
        vm.isLoading.value = true
    }


    /**
     * not used coz, alternative sms gateway used .
     */
    private fun sendVerificationCode(phoneNumber: String?) {
        vm.isLoading.value = true
        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber(phoneNumber!!) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(mCallbacks!!) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * not used coz, alternative sms gateway used.
     */
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                requireActivity()
            ) { task ->
                if (task.isSuccessful) {
                    vm.isLoading.value = false
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")


                    // Update UI
                } else {
                    // Sign in failed, display a message and update the UI
                    showSnackBar(task.exception!!.message!!)
                    Log.w(tag, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {

                    }
                }
            }
    }


    /**
     * Commented by ramesh coz used alternative sms gateway.
     */
    override fun verifyCode(otp: String?) {
        /* try {
             val credential = PhoneAuthProvider.getCredential(mVerificationId, otp!!)
             signInWithPhoneAuthCredential(credential)
         } catch (e: Exception) {
             vm.isLoading.value = false
             if (otp!!.isEmpty())
                 Toast.makeText(requireContext(), "Enter OTP", Toast.LENGTH_SHORT).show()
             else Toast.makeText(requireContext(), "Wrong OTP", Toast.LENGTH_SHORT).show()
         }*/
    }

    override fun getOpt(): String? {
        return if (otpView!!.getOTP()!!.isNotEmpty())
            otpView!!.getOTP()
        else
            vm.savedOtp
    }


    override fun resendOtp() {
        //  sendVerificationCode(vm.phoneNumber.get())
        vm.callotpApi()
    }

    override fun goToSignUp(phone: String, cc: String, ccId: String) {
        var bundle = bundleOf(Config.phone_number to phone,Config.country_code to cc,Config.country_code_id to ccId)
        findNavController().navigate(R.id.toRegister,bundle)
    }

    override fun goToDrawer() {
        startActivity(Intent(requireActivity(), DrawerActivity::class.java))
        requireActivity().finish()
    }

    override fun getAct(): Activity {
        return requireActivity()
    }

    override fun callHideKeyBoard() {
        if (isAdded)
            hideKeyboard(requireActivity())
    }

    override fun getCtx(): Context {
        return context?:requireContext()
    }


    private fun updateOtpInFirebase(otp: String) {
        phoneOtp.clear()
        phoneOtp["otp"] = otp
        mDatabase!!.child("verification").child("user")
            .child(vm.phoneNumber.get()!!).setValue(phoneOtp).addOnCompleteListener {
                // Write was successful!
                // ...
                if (vm.mode.get() == 0)
                    vm.loginOrSignUpApi()
                else
                    vm.updatePhoneInProfile()
            }

    }

    private fun closeFrag() {
        findNavController().popBackStack()
    }

    private fun getFCMToken() {
        FirebaseApp.initializeApp(requireContext())
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(LoginFragment.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            vm.session.saveString(SessionMaintainence.FCM_TOKEN, token.toString())

        })
    }

    private fun recieveSms() {
        val br = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    for (sms in Telephony.Sms.Intents.getMessagesFromIntent(p1)) {
                        vm.printE("HellowWorld", sms.displayMessageBody)
                    }
                }

            }

        }
        requireActivity().registerReceiver(
            br,
            IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        )
    }


    private fun initSmsRetriver() {

        val client = context?.let { SmsRetriever.getClient(it /* context */) }


        val task: Task<Void> =
            client?.startSmsRetriever() ?: task!!.addOnSuccessListener {
                vm.printE("SmsRetriverApi", "Started successfully")

            }

        task.addOnFailureListener {
            vm.printE("SmsRetriverApi", "Started successfully: $it")
        }

    }

    fun initBroadCast() {
        intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        smsReceiver = MySMSBroadcastReceiver()
        smsReceiver!!.setOTPListener(object : MySMSBroadcastReceiver.OTPReceiveListener {
            override fun onOTPReceived(otp: String?) {
                vm.printE("recivedOtp", "ReceivedOtp: $otp")
                vm.otpReceived.set(otp)
            }

        })

    }

}