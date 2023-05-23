package com.cloneUser.client.drawer.myRides

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.databinding.FragmentHistoryListBinding
import com.cloneUser.client.drawer.myRides.cancelled.CancelledHistoryFragment
import com.cloneUser.client.drawer.myRides.completed.CompletedHistoryList
import com.cloneUser.client.drawer.myRides.scheduled.ScheduledHistoryFragment
import com.cloneUser.client.ut.Config
import javax.inject.Inject

class HistoryListFragment : BaseFragment<FragmentHistoryListBinding, HistoryListVM>(),
    HistoryListNavigator {
    companion object {
        const val TAG = "HistoryListFragment"
    }

    private lateinit var binding: FragmentHistoryListBinding
    private var pagerAdapter: ViewPagerAdapter? = null
    var tabTitles: MutableList<String> = mutableListOf()
    private val goToInvoice: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            requireActivity().runOnUiThread {
                val bundle = bundleOf(
                    "REQUEST_DATA" to intent.getSerializableExtra("REQUEST_DATA") as? RequestData.Data,
                    "MODE" to 1
                )
                val requestData = bundle.getSerializable("REQUEST_DATA") as? RequestData.Data
                if (requestData?.serviceCategory == Config.rental) {
                    if (findNavController().currentDestination?.label == "My Rides")
                        findNavController().navigate(R.id.history_to_rental_invoice, bundle)
                } else if (requestData?.serviceCategory == Config.outstation) {
                    if (findNavController().currentDestination?.label == "My Rides")
                        findNavController().navigate(R.id.history_to_outstation_invoice, bundle)
                } else {
                    if (findNavController().currentDestination?.label == "My Rides")
                        findNavController().navigate(R.id.history_to_invoice, bundle)
                }
            }
        }
    }

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(HistoryListVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            goToInvoice,
            IntentFilter(Config.RECEIVE_DIRECTION_INVOICE)
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(goToInvoice)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setup()
    }


    override fun getLayoutId() = R.layout.fragment_history_list

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    private fun setup() {
        binding.backImg.setOnClickListener { findNavController().popBackStack() }
        tabTitles.add(vm.translationModel.txt_schedule ?: "Scheduled")
        tabTitles.add(vm.translationModel.txt_completed ?: "Completed")
        tabTitles.add(vm.translationModel.txt_cancelled ?: "Cancelled")
        pagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = pagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ScheduledHistoryFragment()
                1 -> CompletedHistoryList()
                else -> CancelledHistoryFragment()
            }
        }
    }

    fun openDispute(history: RequestData.Data) {
        val action = HistoryListFragmentDirections.completedHistoryToComplaints(1, history.id!!)
        findNavController().navigate(action)
    }

    fun refreshFragment() {
        val id = findNavController().currentDestination?.id
        findNavController().popBackStack(id!!, true)
        findNavController().navigate(id)
    }

}