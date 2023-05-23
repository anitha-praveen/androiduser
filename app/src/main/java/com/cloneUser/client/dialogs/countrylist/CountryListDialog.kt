package com.cloneUser.client.dialogs.countrylist

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.connection.responseModels.AvailableCountryAndKLang
import com.cloneUser.client.connection.responseModels.Country
import com.cloneUser.client.databinding.DialogCountryListBinding
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import android.content.Intent
import android.content.IntentFilter
import com.cloneUser.client.ut.Config


/*
If mode 0 -> from login screen
If mode 1 -> from profile update
 */

class CountryListDialog(mode: Int) : BaseDialog<DialogCountryListBinding, CountryListVM>(),
    CountryListNavigator {
    companion object {
        const val TAG = "CountryListDialog"
    }

    val mod = mode
    private lateinit var binding: DialogCountryListBinding
    var countryListAdapter: CountryListAdapter? = null
    var linearLayoutManager: LinearLayoutManager? = null
    lateinit var data: AvailableCountryAndKLang

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(CountryListVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setDialogFullSCreen()
        setUpCountryList()
    }


    private val noItemVisibilityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.getBooleanExtra(Config.RECEIVE_NO_ITEM_FOUND, false).let {
                if (it)
                    view?.let { it1 -> hideKeyBoard(it1) }
                vm.showNoItem.set(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            noItemVisibilityReceiver,
            IntentFilter(Config.RECEIVE_NO_ITEM_FOUND)
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(noItemVisibilityReceiver)
        super.onDestroy()
    }

    override fun getLayout(): Int = R.layout.dialog_country_list


    override fun getBindingVariable(): Int = BR.viewModel


    override fun getViewModel(): CountryListVM = vm
    override fun clickedItem(country: Country) {
        val intent = Intent(Config.RECEIVE_COUNTRY)
        intent.putExtra(Config.country, country)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        dismiss()
    }

    override fun close() {
        dismiss()
    }

    override fun showSnackBar(message: String) =
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun isNetworkConnected(): Boolean = Utilz.checkForInternet(requireActivity())

    override fun showCustomDialog(message: String) {

    }

    private fun setUpCountryList() {
        data = vm.session.getAvailableCountryAndLanguages()!!
        linearLayoutManager = LinearLayoutManager(activity)
        countryListAdapter = CountryListAdapter(requireActivity(), data.country!!, this)
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.adapter = countryListAdapter
        binding.searchEdit.addTextChangedListener(countryListAdapter)
    }
}