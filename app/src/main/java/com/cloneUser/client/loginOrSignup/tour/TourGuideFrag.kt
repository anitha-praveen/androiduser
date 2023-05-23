package com.cloneUser.client.loginOrSignup.tour

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.FragmentTourGuideBinding
import com.cloneUser.client.loginOrSignup.tour.sliderScreens.ScreenSlidePageFragment
import com.cloneUser.client.loginOrSignup.tour.sliderScreens.ZoomOutPageTransformer
import com.cloneUser.client.ut.SessionMaintainence
import javax.inject.Inject


/**
 * The number of pages (wizard steps) to show in this demo.
 */
private const val NUM_PAGES = 2

class TourGuideFrag : BaseFragment<FragmentTourGuideBinding, TourGuideVM>(), TourGuideNavigator {

    companion object {
        const val TAG = "TourGuideFrag"
    }


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: FragmentTourGuideBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(TourGuideVM::class.java)
    }


    var backpressed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backpressed)
                    requireActivity().finishAffinity()
                else
                    showMessage("Press back again to exit")
                backpressed = true
                Handler(Looper.getMainLooper()).postDelayed(
                    {backpressed = false},2000
                )
            }
        })

    }

    val mPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            vm.isFirst.set(position == 0)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        vm.forwardtxt.set(vm.translationModel.txt_next)
        viewPager = binding.pager
        tabLayout = binding.tabLay
        viewPager.setPageTransformer(ZoomOutPageTransformer())
        viewPager.registerOnPageChangeCallback(mPageChangeCallback)
        val pagerAdapter = ScreenSlidePagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
        }.attach()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position<3){
                    vm.forwardtxt.set(vm.translationModel.txt_next ?: "")
                    vm.skipDisable.set(false)

                }else{
                    vm.forwardtxt.set(vm.translationModel.txt_finish ?: "")
                    vm.skipDisable.set(true)
                }
            }

        })
    }


    override fun getLayoutId() = R.layout.fragment_tour_guide

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    /**
     * A simple pager adapter that represents 4 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            ScreenSlidePageFragment.modelPos(position,vm.translationModel)
            return ScreenSlidePageFragment()
        }

    }

    override fun forwardClick() {
        if (viewPager.currentItem == 1)
            moveToSignUp()
        else
            viewPager.currentItem = viewPager.currentItem + 1
    }

    override fun skipClick() {
        moveToSignUp()
    }

    private fun moveToSignUp() {
        vm.session.saveBoolean(SessionMaintainence.TOUR_SHOWN,true)
        findNavController().navigate(R.id.toLogin)
    }

}