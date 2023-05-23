package com.cloneUser.client.drawer.sos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.SosModel
import com.cloneUser.client.databinding.FragmentSosBinding
import com.cloneUser.client.dialogs.addSos.AddSosBottomSheet
import com.cloneUser.client.drawer.sos.adapter.SosAdapter
import com.cloneUser.client.ut.Config
import javax.inject.Inject

class SosFragment : BaseFragment<FragmentSosBinding, SosVM>(),
    SosNavigator {
    companion object {
        const val TAG = "SosFragment"
    }

    private lateinit var binding: FragmentSosBinding
    var adapter: SosAdapter? = null
    val args: SosFragmentArgs by navArgs()

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(SosVM::class.java)
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
        binding.backImg.setOnClickListener { closeFragment() }
        setupAdapter()
        vm.mode.set(args.mode)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(sosRec, IntentFilter(Config.SEND_SOS_CONTACT))
    }

    private val sosRec = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            vm.saveSos(
                p1?.getStringExtra(Config.firstname) ?: "",
                p1?.getStringExtra(Config.phone_number) ?: ""
            )
        }

    }

    override fun getLayoutId() = R.layout.fragment_sos

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    override fun onPhoneClick(number: String) {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:" + number.trim { it <= ' ' })
        startActivity(callIntent)
    }

    override fun addList(sos: List<SosModel.Sos>?) {
        adapter!!.addList(sos!!)
    }

    override fun showSosAdd() {
        val addSos = AddSosBottomSheet.newInstance(vm.translationModel)
        addSos.show(childFragmentManager,AddSosBottomSheet.TAG)
    }


    override fun deleteSosNav(slug: String) {
        vm.deleteSos(slug)
    }

    fun closeFragment() = findNavController().popBackStack()

    private fun setupAdapter() {
        adapter = SosAdapter(ArrayList<SosModel.Sos>(), this)
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerSos.layoutManager = mLayoutManager
        binding.recyclerSos.itemAnimator = DefaultItemAnimator()
        binding.recyclerSos.adapter = adapter
        vm.getSosList()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(sosRec)
    }

}