package com.cloneUser.client.drawer.rental.rentalFareDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.cloneUser.client.R
import com.cloneUser.client.connection.responseModels.RentalPackageTypes
import com.cloneUser.client.databinding.BottomsheetRentalFairBinding
import com.cloneUser.client.ut.Utilz
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.cloneUser.client.connection.TranslationModel

class RentalFairBottom : BottomSheetDialogFragment(), RentalFairBottomNav {

    companion object{
        const val TAG = "RentalFairBottom"
        private lateinit var mTranslationModel : TranslationModel
        private lateinit var mType : RentalPackageTypes.Type
        private lateinit var pack : String
        fun newInstance(translationModel: TranslationModel , type: RentalPackageTypes.Type , pkg : String) =
            RentalFairBottom().apply {
            mTranslationModel = translationModel
            mType = type
            pack = pkg
        }
    }
    lateinit var binding: BottomsheetRentalFairBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.bottomsheet_rental_fair, container, false
        )
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val factory = RentalFairBottomVM.Factory(
            requireActivity().application, mTranslationModel,
            mType, pack
        )
        val model: RentalFairBottomVM = ViewModelProvider(this, factory)
            .get(RentalFairBottomVM::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = model
    }

    override fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun showSnackBar(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun isNetworkConnected(): Boolean {
        return Utilz.checkForInternet(requireContext())
    }

    override fun showNetworkUnAvailable() {
        Toast.makeText(requireContext(), "Network Unavailable, Connect to internet!", Toast.LENGTH_SHORT)
            .show()
    }

    override fun showCustomDialog(message: String) {}
}