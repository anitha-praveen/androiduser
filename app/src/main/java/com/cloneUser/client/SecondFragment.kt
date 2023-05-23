package com.cloneUser.client

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.FragmentSecondBinding
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : BaseFragment<FragmentSecondBinding, SecondFragmeVM>() {
    @Inject
    lateinit var vmProvider: ViewModelProvider.Factory

    @Inject
    lateinit var test: String
    val vm by lazy {
        ViewModelProvider(this, vmProvider).get(SecondFragmeVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Snackbar.make(view, "TESt<<" + test + ">>", Snackbar.LENGTH_SHORT).show()
//        mbinding.buttonSecond.setOnClickListener { findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment) }

    }

    override fun getLayoutId() = R.layout.fragment_second

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

}