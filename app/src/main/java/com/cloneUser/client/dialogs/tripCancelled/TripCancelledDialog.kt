package com.cloneUser.client.dialogs.tripCancelled

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.databinding.DialogTripCancelBinding
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject
import android.media.AudioAttributes
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cloneUser.client.ut.Config


class TripCancelledDialog : BaseDialog<DialogTripCancelBinding, TripCancelledVM>(),
    TripCancelledNavigator {
    companion object {
        const val TAG = "TripCancelledDialog"
    }

    private lateinit var binding: DialogTripCancelBinding
    var mMediaPlayer: MediaPlayer? = null

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(TripCancelledVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setDialogFullSCreen()
    }

    override fun getLayout(): Int = R.layout.dialog_trip_cancel


    override fun getBindingVariable(): Int = BR.viewModel



    override fun getViewModel(): TripCancelledVM = vm
    override fun close() {
        val intent = Intent(Config.RECEIVE_CLOSE_TRIP)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        dismiss()
    }


    override fun showSnackBar(message: String) =
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun isNetworkConnected(): Boolean = Utilz.checkForInternet(requireActivity())

    override fun showCustomDialog(message: String) {

    }

    private fun voiceMsg() {
        val audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)

            mMediaPlayer = MediaPlayer.create(activity, R.raw.speech)
        if (!mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        }
    }

    override fun onResume() {
        super.onResume()
        voiceMsg()
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.reset()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mMediaPlayer != null) mMediaPlayer!!.stop()
    }

    override fun onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer!!.stop()
        super.onDestroy()
    }
}