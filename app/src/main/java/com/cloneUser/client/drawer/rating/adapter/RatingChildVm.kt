package com.cloneUser.client.drawer.rating.adapter

import android.util.Log
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.BaseResponse

class RatingChildVm(val question: BaseResponse.InvoiceQuestionsList, adapterLister: thumbsClicked,val position:Int) {
    val question_txt = ObservableField(question.questions)
    val child: thumbsClicked = adapterLister



    fun ratingSelected(radioGroup: RadioGroup, id:Int) {
        when (id){
            R.id.thumbs_up ->{
                question.isSelected = true
            }
            R.id.thumbs_down ->{
                question.isSelected = false
            }
        }
        child.itemClicked(question,question.isSelected,position)
    }


    interface thumbsClicked : BaseViewOperator {
        fun itemClicked(question: BaseResponse.InvoiceQuestionsList, isSelected:Boolean,postion:Int)
    }
}

@BindingAdapter("custom_tint")
fun ImageView.setImageTint(@ColorInt color: Int) {
    setColorFilter(color)
}