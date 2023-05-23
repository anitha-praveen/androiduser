package com.cloneUser.client.drawer.faq

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
import com.cloneUser.client.connection.responseModels.FaqModel
import com.cloneUser.client.databinding.FragmentFaqBinding
import com.cloneUser.client.drawer.faq.adapter.NewFaqAdapter
import javax.inject.Inject

class FaqFragment: BaseFragment<FragmentFaqBinding, FaqVm>(),FaqNavigator {
    lateinit var _binding: FragmentFaqBinding
    //lateinit var adapter : FaqAdapter
    companion object{
        const val TAG = "FaqFragment"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(FaqVm::class.java)
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
        _binding = getmBinding()
        vm.setNavigator(this)
        _binding.backNaviagation.setOnClickListener{closeFragment()}
        Log.e(TAG,vm.translationModel.text_faq)
        vm.getFaqApiVm()
    }


    override fun getLayoutId() = R.layout.fragment_faq

    override fun getBR() =BR.viewModel

    override fun getVMClass() =vm

    fun closeFragment(){
        findNavController().popBackStack()
        parentFragmentManager.beginTransaction().remove(this@FaqFragment).commit()
    }

    override fun setFaqAdapter(list: ArrayList<FaqModel>) {
        val layoutManager = LinearLayoutManager(context)
        val adapter = NewFaqAdapter(list)
        _binding.faqRecyclerView.layoutManager = layoutManager
        _binding.faqRecyclerView.adapter = adapter

    }


}