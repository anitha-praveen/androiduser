package com.cloneUser.client.drawer.language

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.Languages
import com.cloneUser.client.databinding.FragmentLanguagesBinding
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.loginOrSignup.getStarteedScrn.LanguageAdapter
import com.cloneUser.client.ut.SessionMaintainence
import javax.inject.Inject

class LanguageFrag : BaseFragment<FragmentLanguagesBinding, LanguageVM>(),
    LanguageNavigator {
    companion object {
        const val TAG = "LanguageFrag"
    }

    private lateinit var binding: FragmentLanguagesBinding
    //  lateinit var googleMap: GoogleMap

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(LanguageVM::class.java)
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
        binding.backImg.setOnClickListener { findNavController().popBackStack() }
        vm.data = vm.session.getAvailableCountryAndLanguages()!!
        binding.langRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val languageAdapter = LanguageAdapter(vm.data.languages!!, this,vm.session.getString(
            SessionMaintainence.CURRENT_LANGUAGE) ?: "")
        binding.langRecycler.adapter = languageAdapter
    }

    override fun getLayoutId() = R.layout.fragment_languages

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm
    override fun setSelectedLanguage(languages: Languages) {
        vm.selectedLangCode.set(languages.code!!)
        vm.session.saveString(SessionMaintainence.CURRENT_LANGUAGE,languages.code)
        vm.session.saveLong(SessionMaintainence.TRANSLATION_TIME_NOW,languages.updatedDate!!.toLong())
    }

    override fun refresh() {
        startActivity(Intent(requireContext(), DrawerActivity::class.java))
        requireActivity().finish()
    }

    override fun close(){
        findNavController().popBackStack()
    }

}