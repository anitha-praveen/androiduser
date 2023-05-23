package com.cloneUser.client.drawer.favorites.addFavorite

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.FragmentAddFavoriteBinding
import com.cloneUser.client.drawer.confirmDestination.ConfirmDestinationFragmentArgs
import javax.inject.Inject

class AddFavoriteFragment : BaseFragment<FragmentAddFavoriteBinding, AddFavoriteVM>(),
    AddFavoriteNavigator {
    companion object {
        const val TAG = "AddFavoriteFragment"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: FragmentAddFavoriteBinding

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(AddFavoriteVM::class.java)
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
    }


    override fun getLayoutId() = R.layout.fragment_add_favorite

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm
    override fun closeFragment() {
        findNavController().popBackStack()
    }

    override fun getCtx(): Context {
        return requireContext()
    }

    override fun goToPickFromMap() {
        val action = AddFavoriteFragmentDirections.addFavoriteToPickFromMap(vm.mPlaceName.get()!!)
        findNavController().navigate(action)
    }

    override fun getBinding(): FragmentAddFavoriteBinding {
        return binding
    }


}