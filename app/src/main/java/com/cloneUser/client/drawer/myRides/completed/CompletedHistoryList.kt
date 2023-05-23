package com.cloneUser.client.drawer.myRides.completed

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.HistoryModel
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.databinding.FragmentCompletedHistoryListBinding
import com.cloneUser.client.drawer.adapters.completed_history_adapter.CompletedHistoryAdapter
import com.cloneUser.client.drawer.myRides.HistoryListFragment
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.PaginationScrollListener
import java.util.*
import javax.inject.Inject

class CompletedHistoryList: BaseFragment<FragmentCompletedHistoryListBinding, CompletedHistoryListVM>(),
    CompletedHistoryNavigator {
    companion object {
        const val TAG = "CompletedHistoryList"
    }

    private lateinit var binding: FragmentCompletedHistoryListBinding
    var adapter: CompletedHistoryAdapter? = null
    var isPageLoading  = false

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(CompletedHistoryListVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
//        binding.pullToRefresh.setOnRefreshListener {
//           setupAdapter()
//            binding.pullToRefresh.isRefreshing = false
//        }
        setupAdapter()
    }


    override fun getLayoutId() = R.layout.fragment_completed_history_list

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    private fun setupAdapter() {
        adapter = CompletedHistoryAdapter(ArrayList<HistoryModel.History>(), this,vm.session)
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerHistoryList.layoutManager = mLayoutManager
        binding.recyclerHistoryList.itemAnimator = DefaultItemAnimator()
        binding.recyclerHistoryList.adapter = adapter
        vm.currentPage = 1
        binding.recyclerHistoryList.addOnScrollListener(object :
            PaginationScrollListener(mLayoutManager) {
            override fun loadMoreItems() {
                isPageLoading = true
                vm.previousPage = vm.currentPage
                vm.currentPage += 1
                vm.getHistoryList()
            }

            override val totalPageCount: Int get() = vm.totalPages

            override val isLastPage: Boolean
                get() {
                    return vm.isLastPage
                }

            override val isLoading: Boolean
                get() {
                    return isPageLoading
                }
        })

        vm.getHistoryList()
    }

    override fun loadData(data: MutableList<HistoryModel.History>) {
        adapter!!.addList(data)
        if(vm.currentPage <= vm.totalPages) adapter?.addLoadingFooter()
        else vm.isLastPage = true
    }

    override fun invoice(data: RequestData.Data) {
        if (isAdded) {
            val intent = Intent(Config.RECEIVE_DIRECTION_INVOICE)
            intent.putExtra("REQUEST_DATA",data)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

    }

    override fun startSimmer() {
        binding.shimmerViewContainer.startShimmerAnimation()
    }

    override fun stopSimmer() {
        binding.shimmerViewContainer.stopShimmerAnimation()
        binding.shimmerViewContainer.visibility = View.GONE

    }

    override fun getTranslation(): TranslationModel {
        return vm.translationModel
    }

    override fun stopLoader() {
        adapter?.removeLoadingFooter()
        isPageLoading = false
    }

    override fun mentionLastPage() {
        if (vm.currentPage != 1) {
            adapter!!.removeLoadingFooter()
            isPageLoading = false
            vm.isLastPage = true
        }
    }

    override fun openDispute(history: RequestData.Data) {
        (requireParentFragment() as HistoryListFragment).openDispute(history)
    }
}