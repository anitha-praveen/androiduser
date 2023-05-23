package com.cloneUser.client.drawer.complaint.history.common

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.FragmentComplaintsListHistoryBinding
import com.cloneUser.client.drawer.complaint.history.complaintsHistory.ComplaintsHistory
import com.cloneUser.client.drawer.complaint.history.suggestionHistory.SuggestionHistory
import javax.inject.Inject

class ComplaintsListHistory :
    BaseFragment<FragmentComplaintsListHistoryBinding, ComplaintsListHistoryVM>(),
    ComplaintsListHistoryNav {
    companion object {
        const val TAG = "ComplaintsListHistory"
    }

    var tabTitles: MutableList<String> = mutableListOf()
    private var pagerAdapter: ViewPagerAdapter? = null
    private lateinit var binding: FragmentComplaintsListHistoryBinding

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory)[ComplaintsListHistoryVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeFrag()
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setClickListeners()
        setup()
    }

    private fun setup() {
        binding.backImg.setOnClickListener { findNavController().popBackStack() }
        tabTitles.add(vm.translationModel.complaints ?: "Complaints")
        tabTitles.add(vm.translationModel.txt_suggestion ?: "Suggestion")
        pagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = pagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ComplaintsHistory()
                else -> SuggestionHistory()
            }
        }
    }

    override fun getLayoutId() = R.layout.fragment_complaints_list_history

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    private fun closeFrag() {
        findNavController().popBackStack()
    }

    private fun setClickListeners() {
        binding.backImg.setOnClickListener { closeFrag() }
        binding.addComplaint.setOnClickListener {
            if (findNavController().currentDestination?.label == "Complaints List History")
                findNavController().navigate(R.id.toAddComplaints)
        }
    }

}