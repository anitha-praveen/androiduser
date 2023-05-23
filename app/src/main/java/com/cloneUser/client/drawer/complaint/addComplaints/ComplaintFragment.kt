package com.cloneUser.client.drawer.complaint.addComplaints

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.ComplaintsModel
import com.cloneUser.client.databinding.FragmentComplaintBinding
import com.cloneUser.client.drawer.adapters.complaints_list_adapter.ComplaintListAdapter
import javax.inject.Inject

class ComplaintFragment: BaseFragment<FragmentComplaintBinding, ComplaintVM>(),
    ComplaintNavigator {
    companion object {
        const val TAG = "ComplaintFragment"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: FragmentComplaintBinding
    private val args : ComplaintFragmentArgs by navArgs()
    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(ComplaintVM::class.java)
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
        binding.back.setOnClickListener { closeFrag() }
        vm.mode = args.mode
        vm.reqId = args.requestId
        setAdapter()
    }


    override fun getLayoutId() = R.layout.fragment_complaint

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    private fun closeFrag(){
        findNavController().popBackStack()
    }



    var adapter: ComplaintListAdapter? = null
    private fun setAdapter(){
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = ComplaintListAdapter(ArrayList(), this)
        binding.listRecycler.layoutManager = mLayoutManager
        binding.listRecycler.itemAnimator = DefaultItemAnimator()
        binding.listRecycler.adapter = adapter

        if(vm.mode==1)
            vm.getComplaintsListOfTrip()
        else
            vm.getComplaintsList()

    }

    override fun addItems(data: ArrayList<ComplaintsModel>?) {
        data?.let { adapter?.addList(it) }
    }

    override fun setSelected(data: ComplaintsModel) {
         data.slug?.let { vm.selectedSlug = it }
    }

    override fun getCtx(): Context {
        return (context?:activity?.applicationContext) as Context
    }


}