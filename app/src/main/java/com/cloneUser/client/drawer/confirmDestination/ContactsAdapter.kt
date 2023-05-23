package com.cloneUser.client.drawer.confirmDestination

//class ContactsAdapter internal constructor(
//    lang_list: List<ContactList.MyContacts>,
//    navigator: Any?,
//    currentSelection: String
//) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() */ {
//    private var contactsLists: List<ContactList.MyContacts> = lang_list
//    var nav: Any? = navigator
//    var id = -1
//
//    init {
//        contactsLists.forEachIndexed { index, element ->
//            if(element.name == currentSelection)
//                id = index
//        }
//        this.nav = navigator
//    }
//
//    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
//        val itemView: View = LayoutInflater.from(viewGroup.context)
//            .inflate(R.layout.contacts_item, viewGroup, false)
//        return ViewHolder(itemView)
//    }
//
//    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
//        viewHolder.lang_names.text = contactsLists[i].name
//        if (id == i) {
//            viewHolder.checkbox.setImageResource(R.drawable.ic_circle_selected)
//        } else {
//            viewHolder.checkbox.setImageResource(R.drawable.ic_circle_unselected)
//        }
//    }
//
//    fun addItem(lang_list: List<ContactList.MyContacts>){
//        contactsLists = lang_list
//        id = contactsLists.lastIndex
//        notifyDataSetChanged()
//    }
//
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        var lang_names: TextView
//        var checkbox: ImageView
//
//        init {
//            lang_names = itemView.findViewById(R.id.lang_item)
//            checkbox = itemView.findViewById(R.id.checkbox)
//            itemView.setOnClickListener {
//                if (nav is ConfirmDestinationNavigator) {
//                    ( nav as ConfirmDestinationNavigator).setSelectedContact(contactsLists[adapterPosition])
//                    id = adapterPosition
//                    notifyDataSetChanged()
//                }
//            }
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return contactsLists.size
//    }
//}