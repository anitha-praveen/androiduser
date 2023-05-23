package  com.cloneUser.client.drawer.outStation.adapter

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.cloneUser.client.R
import com.cloneUser.client.connection.responseModels.OutstationModel
import com.cloneUser.client.drawer.outStation.listOutStation.OutStationListNavigator
import java.util.*

class PlacesOutstationAdapter
    (
    var context: Context,
    var outStationList: MutableList<OutstationModel>,
    var navigator: OutStationListNavigator
) :
    RecyclerView.Adapter<PlacesOutstationAdapter.ViewHolder>(), TextWatcher {
    var filteredList: MutableList<OutstationModel> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placesTxt: TextView = itemView.findViewById(R.id.places_name)
        val cardView: CardView = itemView.findViewById(R.id.card_item_outStation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.outstation_list_helper, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.placesTxt.text = filteredList[position].drop
        holder.cardView.setOnClickListener {
            navigator.placeSelected(filteredList[position])
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(editable: CharSequence?, p1: Int, p2: Int, p3: Int) {


    }

    fun addList(list: MutableList<OutstationModel>) {
        outStationList.clear()
        outStationList.addAll(list)
        filteredList = outStationList
        notifyDataSetChanged()
    }

    override fun afterTextChanged(editable: Editable?) {

        val strFilterable = editable.toString()
        val newFilteredList: MutableList<OutstationModel> = arrayListOf()
        if (!TextUtils.isEmpty(editable.toString())) {
            for (row in outStationList) {
                if (row.drop!!.lowercase().trim()
                        .startsWith(strFilterable) || row.drop!!.lowercase().trim()
                        .contains(strFilterable)
                ) {
                    newFilteredList.add(row)
                }
            }
            filteredList = newFilteredList
            if (filteredList.size == 0) {
                navigator.showNoDataFound(true)
            } else
                navigator.showNoDataFound(false)
            notifyDataSetChanged()
        } else {
            filteredList = outStationList
            notifyDataSetChanged()
        }

//        if (filteredList.isNotEmpty()) {
//            val strFilterable = editable.toString()
//            val newFilteredList: MutableList<OutstationModel> = arrayListOf()
//            if (!TextUtils.isEmpty(editable.toString())) {
//                for (row in filteredList) {
//                    if (row.drop!!.lowercase().trim()
//                            .startsWith(strFilterable) || row.drop!!.lowercase().trim().contains(strFilterable)
//                    ) {
//                        newFilteredList.add(row)
//                    }
//                }
//                filteredList = newFilteredList
//                notifyDataSetChanged()
//            } else {
//                filteredList = outStationList
//                notifyDataSetChanged()
//            }
//        }
//        else{
//            if (editable != null) {
//                if (editable.length  == 0 ){
//                    val intent = Intent(Config.SHOW_NO_DATA_FOUND_OUTSTATION)
//                    intent.putExtra(Config.showNoDataFoundOutstation,false)
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                    filteredList = outStationList
//
//                }
//                else{
//                    val intent = Intent(Config.SHOW_NO_DATA_FOUND_OUTSTATION)
//                    intent.putExtra(Config.showNoDataFoundOutstation,true)
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                }
//            }
//        }

    }


}