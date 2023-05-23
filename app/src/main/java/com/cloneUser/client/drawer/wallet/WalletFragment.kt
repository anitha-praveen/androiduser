package com.cloneUser.client.drawer.wallet

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.responseModels.WalletResponsModel
import com.cloneUser.client.databinding.FragmentWalletBinding
import com.cloneUser.client.drawer.wallet.adapter.WalletHistoryAdapter
import javax.inject.Inject

class WalletFragment : BaseFragment<FragmentWalletBinding, WalletVm>(),
WalletNavigator {

    lateinit var binding: FragmentWalletBinding


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(WalletVm::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { closeFragment()} })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        binding.backImg.setOnClickListener{closeFragment()}
        vm.getListApi()
    }




    override fun getLayoutId() = R.layout.fragment_wallet

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    fun closeFragment(){
        findNavController().popBackStack()
    }

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun setUpAdapter(list: ArrayList<WalletResponsModel>, type: BaseResponse.DataObjectsAllApi) {
        val layoutManager = LinearLayoutManager(context)
        val adapter = WalletHistoryAdapter(list,type)
        binding.walletRe.layoutManager = layoutManager
        binding.walletRe.adapter = adapter
    }
}