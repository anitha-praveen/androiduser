package com.cloneUser.client.drawer.complaint.history.complaintsHistory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.ComplaintHistoryModel
import com.cloneUser.client.databinding.FragmentComplaintsHistoryBinding
import javax.inject.Inject

class ComplaintsHistory : BaseFragment<FragmentComplaintsHistoryBinding, ComplaintsHistoryVM>(),
    ComplaintsHistoryNav {
    companion object {
            const val TAG = "ComplaintsHistory"
    }

    private lateinit var binding: FragmentComplaintsHistoryBinding

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory)[ComplaintsHistoryVM::class.java]
    }

    lateinit var adapter: ComplaintsHistoryAdapter
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
        setAdapter()
    }


    override fun getLayoutId() = R.layout.fragment_complaints_history

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    private fun closeFrag() {
        findNavController().popBackStack()
    }

    private fun setAdapter() {
        adapter = ComplaintsHistoryAdapter(ArrayList(), this)
        binding.comHistRec.layoutManager = LinearLayoutManager(requireContext())
        binding.comHistRec.adapter = adapter
        vm.getComplaints()
    }

    override fun getTranslation(): TranslationModel {
        return vm.translationModel
    }

    override fun setData(list: List<ComplaintHistoryModel.Complaints>) {
        adapter.addData(list)
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: ")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG, "onAttach: ")
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart:")
    }
}