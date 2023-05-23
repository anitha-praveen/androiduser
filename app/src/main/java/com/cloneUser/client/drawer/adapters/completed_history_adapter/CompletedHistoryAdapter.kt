package com.cloneUser.client.drawer.adapters.completed_history_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.regions.Region
//import com.amazonaws.services.s3.AmazonS3
//import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.responseModels.HistoryModel
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.databinding.ItemCompletedHistoryBinding
import com.cloneUser.client.databinding.PaginationLoadingBinding
import com.cloneUser.client.drawer.myRides.completed.CompletedHistoryNavigator
import com.cloneUser.client.ut.SessionMaintainence
import kotlinx.coroutines.*
import java.net.URL
import java.util.*

class CompletedHistoryAdapter(
    private val historyList: MutableList<HistoryModel.History>,
    private val navigator: CompletedHistoryNavigator,
    private val session : SessionMaintainence
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var isLoadingAdded = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {

        return when (viewType) {
            0 -> {
                val binding = ItemCompletedHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                ChildViewHolder(binding, historyList, navigator,session)
            }
            1 -> {
                val paginationLoadingBinding = PaginationLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                PaginationViewHolder(paginationLoadingBinding, navigator)
            }
            else -> {
                val paginationLoadingBinding = PaginationLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                PaginationViewHolder(paginationLoadingBinding, navigator)
            }
        }

    }


    override fun getItemCount(): Int {
        return historyList.size
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(HistoryModel.History())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        val position: Int = historyList.size - 1
        if (historyList.size != 0) {
            val result: HistoryModel.History? = getItem(position)
            if (result != null) {
                historyList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    private fun getItem(position: Int?): HistoryModel.History? {
        return if (position != null)
            historyList[position]
        else
            null
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == historyList.size - 1 && isLoadingAdded) 1 else 0
    }

    fun addList(mHistoryList: MutableList<HistoryModel.History>) {
        for (ff in mHistoryList) {
            add(ff)
        }
    }

    fun add(r: HistoryModel.History) {
        historyList.add(r)
        notifyItemInserted(historyList.size - 1)
    }


    private class PaginationViewHolder(
        paginationLoadingBinding: PaginationLoadingBinding,
        val navigator: CompletedHistoryNavigator
    ) :
        BaseViewHolder(paginationLoadingBinding.root) {
        override fun onBind(position: Int) {}

        override fun isNetworkConnected(): Boolean {
            return navigator.isNetworkConnected()
        }

        override fun showNetworkUnAvailable() {
        }

        override fun showCustomDialog(message: String) {
        }
    }


    class ChildViewHolder(
        private val mBinding: ItemCompletedHistoryBinding,
        private val historyList: MutableList<HistoryModel.History>,
        private val navigator: CompletedHistoryNavigator,
        private val session: SessionMaintainence
    ) : BaseViewHolder(mBinding.root), CompletedAdapterVM.CompletedItemListener {

        private var completedAdapterVM: CompletedAdapterVM? = null

        override fun onBind(position: Int) {
            val history: HistoryModel.History = historyList[position]
            completedAdapterVM = history.data?.let {
                CompletedAdapterVM(
                    it, this,
                    navigator.getTranslation()
                )
            }
            mBinding.viewModel = completedAdapterVM
            mBinding.executePendingBindings()

            Glide.with(mBinding.profile.context).load(history.data?.driver?.profilePic)
                .apply(
                    RequestOptions().error(R.drawable.simple_profile_bg)
                        .placeholder(R.drawable.ic_history_profile_dummy)
                ).into(mBinding.profile)


        }

//        private fun getImageFromS3(profilePic: String?) {
//            val viewModelJob = Job()
//            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
//
//            uiScope.launch {
//                withContext(Dispatchers.IO) {
//                    val cal = Calendar.getInstance()
//                    cal.time = Date()
//                    cal.add(Calendar.HOUR, +1)
//                    val oneHourLater: Date = cal.time
//                    val s3: AmazonS3 = AmazonS3Client(
//                        BasicAWSCredentials(
//                            session.getString(SessionMaintainence.AWS_ACCESS_KEY_ID),
//                            session.getString(SessionMaintainence.AWS_SECRET_ACCESS_KEY)
//                        )
//                    )
//                    s3.setRegion(Region.getRegion(session.getString(SessionMaintainence.AWS_DEFAULT_REGION)))
//                    val url: URL = s3.generatePresignedUrl(
//                        session.getString(SessionMaintainence.AWS_BUCKET),
//                        profilePic,
//                        oneHourLater
//                    )
//                    withContext(Dispatchers.Main) {
//                        Glide.with(mBinding.profile.context).load(url.toString())
//                            .apply(
//                                RequestOptions().error(R.drawable.simple_profile_bg)
//                                    .placeholder(R.drawable.ic_history_profile_dummy)
//                            ).into(mBinding.profile)
//                    }
//                }
//            }
//
//        }

        override fun itemSelected(history: RequestData.Data) {
            navigator.invoice(history)
        }

        override fun onDisputeClicked(history: RequestData.Data) {
            navigator.openDispute(history)
        }


        override fun isNetworkConnected(): Boolean {
            return navigator.isNetworkConnected()
        }

        override fun showNetworkUnAvailable() {

        }

        override fun showCustomDialog(message: String) {

        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

}