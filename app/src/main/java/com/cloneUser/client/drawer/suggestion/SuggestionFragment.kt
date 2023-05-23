package com.cloneUser.client.drawer.suggestion

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.SuggestionModel
import com.cloneUser.client.databinding.FragmentSuggestionBinding
import com.cloneUser.client.drawer.adapters.suggestion_adapter.SuggestionAdapter
import javax.inject.Inject

class SuggestionFragment : BaseFragment<FragmentSuggestionBinding, SuggestionVm>(),
    SuggestionNavigator {
    companion object {
        const val TAG = "ComplaintFragment"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: FragmentSuggestionBinding

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(SuggestionVm::class.java)
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
        binding.backImg.setOnClickListener { closeFrag() }
        setAdapter()
    }


    var adapter: SuggestionAdapter? = null
    private fun setAdapter(){
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = SuggestionAdapter(ArrayList(), this)
        binding.listRecycler.layoutManager = mLayoutManager
        binding.listRecycler.itemAnimator = DefaultItemAnimator()
        binding.listRecycler.adapter = adapter

        vm.getSuggestionApi()

    }

    override fun getLayoutId() = R.layout.fragment_suggestion

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    private fun closeFrag(){
        findNavController().popBackStack()
    }

    override fun addItems(data: ArrayList<SuggestionModel>?) {
        data?.let { adapter?.addList(it) }
    }

    override fun setSelected(data: SuggestionModel) {
       data.slug?.let { vm.selectedSlug = it  }
    }

    override fun getCtx() = context?:requireContext()


}