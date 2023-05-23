package com.cloneUser.client.loginOrSignup.getStarteedScrn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.R
import com.cloneUser.client.connection.responseModels.Languages
import com.cloneUser.client.drawer.language.LanguageNavigator
import com.cloneUser.client.drawer.profile.ProfileNavigator

class LanguageAdapter : RecyclerView.Adapter<LanguageAdapter.ViewHolder> {
    private var langList: List<Languages>
    var navigator: Any? = null
    var id = -1

    internal constructor(lang_list: List<Languages>,
        navigator: GetStartedScreenNavigator?
    ) {
        langList = lang_list
        this.navigator = navigator
    }


    internal constructor(lang_list: List<Languages>, navigator: Any? , currentLanguage : String) {
        langList = lang_list
        this.navigator = navigator
        langList.forEachIndexed { index, element ->
            if(element.code == currentLanguage)
                id = index
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val itemView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.lang_items, viewGroup, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.lang_names.text = langList[i].name
        if (id == i) {
            viewHolder.tickChkBox.visibility = View.VISIBLE
            viewHolder.checkbox.visibility = View.GONE
        } else {
            viewHolder.tickChkBox.visibility = View.GONE
            viewHolder.checkbox.visibility = View.VISIBLE
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var lang_names: TextView
        var checkbox: ImageView
        var tickChkBox: ImageView

        init {
            lang_names = itemView.findViewById(R.id.lang_item)
            checkbox = itemView.findViewById(R.id.chkbox)
            tickChkBox = itemView.findViewById(R.id.tick_chk_box)
            itemView.setOnClickListener {
                if (navigator is GetStartedScreenNavigator ) {
                    ( navigator as GetStartedScreenNavigator).setSelectedLanguage(langList[adapterPosition])
                    id = adapterPosition
                    notifyDataSetChanged()
                }else if(navigator is ProfileNavigator){
                    (navigator as ProfileNavigator).setSelectedLanguage(langList[adapterPosition])
                    id = adapterPosition
                    notifyDataSetChanged()
                }else if(navigator is LanguageNavigator){
                    (navigator as LanguageNavigator).setSelectedLanguage(langList[adapterPosition])
                    id = adapterPosition
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return langList.size
    }
}