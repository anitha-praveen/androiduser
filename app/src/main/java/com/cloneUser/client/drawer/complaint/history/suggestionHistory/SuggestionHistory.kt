package com.cloneUser.client.drawer.complaint.history.suggestionHistory

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.TranslationModel
import com.cloneUser.client.connection.responseModels.SuggestionHistoryModel
import com.cloneUser.client.databinding.FragmentSuggestionHistoryBinding
import javax.inject.Inject

class SuggestionHistory : BaseFragment<FragmentSuggestionHistoryBinding, SuggestionHistoryVM>(),
    SuggestionHistoryNav {
    companion object {
        const val TAG = "SuggestionHistory"
    }

    private lateinit var binding: FragmentSuggestionHistoryBinding

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    lateinit var adapter: SuggestionHistoryAdapter
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory)[SuggestionHistoryVM::class.java]
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
        setAdapter()
    }


    override fun getLayoutId() = R.layout.fragment_suggestion_history

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    private fun closeFrag() {
        findNavController().popBackStack()
    }

    private fun setAdapter() {
        adapter = SuggestionHistoryAdapter(ArrayList(), this)
        binding.sugHistRec.layoutManager = LinearLayoutManager(requireContext())
        binding.sugHistRec.adapter = adapter
        vm.getRegisteredSug()
    }

    override fun setData(list: List<SuggestionHistoryModel.Suggestion>) {
        adapter.addData(list)
    }

    override fun getTranslation(): TranslationModel {
        return vm.translationModel
    }
}