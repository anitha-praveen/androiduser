package com.cloneUser.client.drawer.rating.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.base.BaseViewHolder
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.databinding.RatingQuestionHelperBinding
import com.cloneUser.client.dialogs.cropImageDialog.CropImageNavigator
import com.cloneUser.client.drawer.rating.RatingNavigator

class RatingAdapter(private val questions:ArrayList<BaseResponse.InvoiceQuestionsList>,val navigator: RatingNavigator) : RecyclerView.Adapter<BaseViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = RatingQuestionHelperBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ChildViewHolder(binding,questions, navigator)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = questions.size

    class ChildViewHolder(
        private val mBinding: RatingQuestionHelperBinding,
        private val questions:ArrayList<BaseResponse.InvoiceQuestionsList>,
        private val navigator: RatingNavigator
    ) : BaseViewHolder(mBinding.root), RatingChildVm.thumbsClicked{

        private var childVM: RatingChildVm? = null

        override fun onBind(position: Int) {
            val questionObject = questions[position]
            childVM = RatingChildVm(questionObject,this,position)
            mBinding.viewModel = childVM
            mBinding.executePendingBindings()
        }

        override fun itemClicked(question: BaseResponse.InvoiceQuestionsList, isSelected: Boolean,position:Int) {
            var newList = questions
            newList[position] = question
            navigator.updateRating(question,newList)
        }


        override fun isNetworkConnected(): Boolean {
            TODO("Not yet implemented")
        }

        override fun showNetworkUnAvailable() {
            TODO("Not yet implemented")
        }

        override fun showCustomDialog(message: String) {
            TODO("Not yet implemented")
        }


    }






}
