package com.cloneUser.client.loginOrSignup.tour.sliderScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cloneUser.client.R
import com.cloneUser.client.connection.TranslationModel

class ScreenSlidePageFragment : Fragment() {
    lateinit var title : TextView
    lateinit var title_desc : TextView
    lateinit var image : ImageView
    companion object{
        var pos:Int = 0
        var translation:TranslationModel? = null
        fun modelPos(pos:Int,translation:TranslationModel){
            this.pos = pos
            this.translation = translation
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_screen_slide_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image = view.findViewById(R.id.image)
        title = view.findViewById(R.id.title)
        title_desc = view.findViewById(R.id.description)
        when(pos){
            0 -> {
                image.setImageResource(R.drawable.ic_intro_screens_first_content)
                title.text = translation?.txt_req_ride
                title_desc.text = translation?.txt_req_desc
            }
            1 ->{
                image.setImageResource(R.drawable.ic_intro_screen_content_second)
                title.text = translation?.txt_live_tracking
                title_desc.text = translation?.txt_live_desc
            }
        }
    }
}