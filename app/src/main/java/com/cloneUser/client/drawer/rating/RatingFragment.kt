package com.cloneUser.client.drawer.rating

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.databinding.FragmentRatingBinding
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.drawer.rating.adapter.RatingAdapter
import javax.inject.Inject


class RatingFragment : BaseFragment<FragmentRatingBinding, RatingVM>(),
    RatingNavigator {
    companion object {
        const val TAG = "RatingFragment"
    }

    private var totalRating = 0
    private lateinit var binding: FragmentRatingBinding
    private var latLng = ObservableField<LatLng>()


    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(RatingVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                /* did nothing because rating is mandatory */
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        arguments?.let {
            vm.requestData = it.getSerializable("REQUEST_DATA") as? RequestData.Data
        }
        vm.setData()

        vm.getQuestions()
    }


    override fun getLayoutId() = R.layout.fragment_rating

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm
    override fun goToHome() {
        (mActivity as DrawerActivity).navigateFirstTabWithClearStack()
    }

    override fun loadList(list: ArrayList<BaseResponse.InvoiceQuestionsList>) {
        val mLayoutManager = LinearLayoutManager(requireContext())
        val adapter = RatingAdapter(list, this)
        binding.questionsRecycler.layoutManager = mLayoutManager
        binding.questionsRecycler.adapter = adapter
    }

    override fun updateRating(
        questionsList: BaseResponse.InvoiceQuestionsList,
        list: ArrayList<BaseResponse.InvoiceQuestionsList>
    ) {

        totalRating = list.count { it.isSelected }
        vm.savedRating.set(totalRating.toFloat())
        vm.list = list
    }


}