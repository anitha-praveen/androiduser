package com.cloneUser.client.drawer.aboutUs

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cloneUser.client.BR
import com.cloneUser.client.BuildConfig
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.AboutUsFragmentBinding
import javax.inject.Inject


class AboutUsFrag: BaseFragment<AboutUsFragmentBinding, AboutUsVm>(),
    AboutUsNavigator {

    lateinit var binding: AboutUsFragmentBinding


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(AboutUsVm::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { closeFragment()} })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        binding.backImg.setOnClickListener{closeFragment()}

        vm.appVersion.set("${vm.translationModel.txt_app_version} " + "v"+ BuildConfig.VERSION_NAME)


    }




    override fun getLayoutId() = R.layout.about_us_fragment

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    fun closeFragment(){
        findNavController().popBackStack()
    }

    override fun openFb() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/rodataxitn"))
        startActivity(browserIntent)
    }

    override fun openPlayStore() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${activity?.packageName}")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${activity?.packageName}")
                )
            )
        }
    }

    override fun openRodaWeb() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://rodataxi.in/"))
        startActivity(browserIntent)
    }

}