package com.cloneUser.client.drawer.complaint.history.suggestionHistory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.R
import com.cloneUser.client.connection.responseModels.SuggestionHistoryModel

class SuggestionHistoryAdapter(
    private var list: List<SuggestionHistoryModel.Suggestion>,
    private val nav: SuggestionHistoryNav
) : RecyclerView.Adapter<SuggestionHistoryAdapter.ViewModel>() {
    class ViewModel(view: View) : RecyclerView.ViewHolder(view) {
        val date = view.findViewById<TextView>(R.id.date)
        val dateValue = view.findViewById<TextView>(R.id.dateValue)
        val suggType = view.findViewById<TextView>(R.id.suggType)
        val suggTypeValue = view.findViewById<TextView>(R.id.suggTypeValue)
        val title = view.findViewById<TextView>(R.id.title)
        val answer = view.findViewById<TextView>(R.id.answer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewModel {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion_history, parent, false)
        return ViewModel(item)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewModel, position: Int) {
        holder.date.text = nav.getTranslation().txt_registered_on + " : "
        holder.dateValue.text = list[position].date
        holder.suggTypeValue.text = if (list[position].suggestionType == 1) "${nav.getTranslation().txt_trip_suggestion}" else "${nav.getTranslation().txt_normal_suggestion}"
        holder.suggType.text = "${nav.getTranslation().txt_suggestion_type} : "
        holder.title.text = list[position].title
        holder.answer.text = list[position].answer
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addData(listItems: List<SuggestionHistoryModel.Suggestion>) {
        list = listItems
        notifyDataSetChanged()
    }
}