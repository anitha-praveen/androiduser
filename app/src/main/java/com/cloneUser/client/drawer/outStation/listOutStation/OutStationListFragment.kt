package com.cloneUser.client.drawer.outStation.listOutStation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.connection.responseModels.OutstationModel
import com.cloneUser.client.databinding.OutstationListFragmentBinding
import com.cloneUser.client.drawer.outStation.adapter.PlacesOutstationAdapter
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.Utilz
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class OutStationListFragment : BaseDialog<OutstationListFragmentBinding, OutStationListVm>(),
    OutStationListNavigator {
    companion object {
        const val TAG = "OutStationListDialog"
    }

    lateinit var binding: OutstationListFragmentBinding
    private lateinit var adapter: PlacesOutstationAdapter
    private val latLng = ObservableField<LatLng>()
    private val address = ObservableField("")


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(OutStationListVm::class.java)
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
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        vm.getOutstaionApi()
        setFullSCreen()
        setUpAdapter()
        binding.backImg.setOnClickListener { closeFragment() }
    }


    override fun getLayout() = R.layout.outstation_list_fragment

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getViewModel(): OutStationListVm = vm

    fun closeFragment() {
        dismiss()
    }

    override fun placeSelected(outStationModel: OutstationModel) {
        val intent = Intent(Config.GETOUTSTATIONSELECTEDPLACE)
        intent.putExtra(Config.outStationSelcetedPlace, outStationModel)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        closeFragment()
    }

    override fun loadList(list: MutableList<OutstationModel>) {
        adapter.addList(list)
    }

    override fun filterList(string: Editable) {
        adapter.afterTextChanged(string)
    }

    override fun showNoDataFound(show: Boolean) {
        vm.showNoResultsFound.set(show)
    }

    override fun showSnackBar(message: String) {

    }

    override fun isNetworkConnected(): Boolean = Utilz.checkForInternet(requireActivity())

    override fun showCustomDialog(message: String) {

    }


    fun setUpAdapter() {
        adapter = context?.let { PlacesOutstationAdapter(it, ArrayList(), this) }!!
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.outStationListRecycler.layoutManager = mLayoutManager
        binding.outStationListRecycler.itemAnimator = DefaultItemAnimator()
        binding.outStationListRecycler.adapter = adapter
    }

    /**
     * set the dialog fragment to fullscreen
     */
    open fun setFullSCreen() {
        if (dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    mActivity as Context,
                    R.color.clr_trans
                )
            )
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        dialog!!.setCanceledOnTouchOutside(false)
    }

}