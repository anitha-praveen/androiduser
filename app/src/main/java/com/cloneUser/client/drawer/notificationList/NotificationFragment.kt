package com.cloneUser.client.drawer.notificationList

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.NotificationData
import com.cloneUser.client.databinding.FragmentNotificationBinding
import com.cloneUser.client.drawer.notificationList.adapter.NotificationAdapter
import com.cloneUser.client.ut.PaginationScrollListener
import javax.inject.Inject

class NotificationFragment : BaseFragment<FragmentNotificationBinding, NotificationVM>(),
    NotificationNavigator {

    lateinit var _binding: FragmentNotificationBinding
    private val list = ArrayList<NotificationData>()
    private var customAdapter: NotificationAdapter? = null
    private var isPageLoading = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(NotificationVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeFragment()
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = getmBinding()
        vm.setNavigator(this)
       _binding.backImg.setOnClickListener{closeFragment()}
        _binding.pullToRefresh.setOnRefreshListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.notifcation)
            _binding.pullToRefresh.isRefreshing = false
        }
        startSimmer()
        vm.getApi()
        customAdapter = NotificationAdapter(list)
        val layoutManager = LinearLayoutManager(context)
        _binding.notificationRecycler.layoutManager = layoutManager
        _binding.notificationRecycler.adapter = customAdapter
        _binding.notificationRecycler.addOnScrollListener(object :
            PaginationScrollListener(layoutManager) {
            override val isLastPage: Boolean
                get() {
                    return vm.isLastPage
                }
            override val isLoading: Boolean
                get() {
                    return isPageLoading
                }
            override val totalPageCount: Int
                get(){
                    return vm.totalPages
                }



            override fun loadMoreItems() {
                isPageLoading = true
                vm.isLoading.value = true
                getMoreitems()
            }
        })
    }

    fun getMoreitems() {
        vm.previousPage = vm.currentPage
        vm.currentPage += 1
        vm.getApi()
    }


    override fun getLayoutId() = R.layout.fragment_notification

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    fun closeFragment() {
        findNavController().popBackStack()
    }

    override fun addList(list: ArrayList<NotificationData>) {
        customAdapter!!.addData(list)
    }

    override fun stopLoader() {
        isPageLoading = false
    }

    override fun mentionLastPage() {
        if (vm.currentPage != 1) {
            isPageLoading = false
            vm.isLastPage = true
        }
    }

    override fun startSimmer() {
        vm.showShimmer.set(true)
        _binding.shimmerViewNotification.startShimmerAnimation()

    }

    override fun stopSimmer() {
        vm.showShimmer.set(false)
        _binding.shimmerViewNotification.stopShimmerAnimation()
    }

}