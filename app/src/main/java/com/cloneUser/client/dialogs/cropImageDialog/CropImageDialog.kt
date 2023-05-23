package com.cloneUser.client.dialogs.cropImageDialog

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.navArgs
import com.canhub.cropper.CropImageView
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.databinding.CropImageLayoutBinding
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.Utilz
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import javax.inject.Inject

class CropImageDialog : BaseDialog<CropImageLayoutBinding, CropImageVm>(),
    CropImageNavigator {
    companion object {
        const val TAG = "CropImageDialog"
    }

    private lateinit var binding: CropImageLayoutBinding
    private val args: CropImageDialogArgs by navArgs()
    private lateinit var imageUri: Uri
    private lateinit var ok_text: TextView
    private lateinit var imageView: CropImageView
    private lateinit var backImg: ImageView

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(CropImageVm::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        imageUri = Uri.parse(args.uri)
        setDialogFullSCreen()
        ok_text = view.findViewById(R.id.choose_crop)
        imageView = view.findViewById(R.id.cropImageView)
        imageView.setImageUriAsync(imageUri)
        imageView.isAutoZoomEnabled = true
        backImg = view.findViewById(R.id.back_crop)
        imageView.setMaxCropResultSize(1600, 1200)
        // binding.showProfileImage.setImageURI(imageUri)
        //startCameraWithUri()

        ok_text.setOnClickListener {
            val bitmap = imageView.getCroppedImage(120, 120)
            val uri = getUri(context!!, bitmap!!)
            if (uri != null) {
                val intent = Intent(Config.RECEIVE_PROFILE_IMAGE)
                intent.putExtra(Config.PASS_URI, uri.toString())
                context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(intent) }
                dialog?.dismiss()
            }

        }
        backImg.setOnClickListener {
            dialog?.dismiss()
        }
    }

    override fun getLayout(): Int = R.layout.crop_image_layout


    override fun getBindingVariable(): Int = BR.viewModel
    override fun getViewModel(): CropImageVm = vm

    override fun showSnackBar(message: String) =
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun isNetworkConnected(): Boolean = Utilz.checkForInternet(requireActivity())

    override fun showCustomDialog(message: String) {

    }

    private fun getUri(context: Context, inImage: Bitmap): URI? {
//        val bytes = ByteArrayOutputStream()
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//        val path =
//            MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Title", null)
//        return Uri.parse(path)
        val file = File(context.cacheDir, "CUSTOM NAME") //Get Access to a local file.
        file.delete() // Delete the File, just in Case, that there was still another File
        file.createNewFile()
        val fileOutputStream = file.outputStream()
        val byteArrayOutputStream = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bytearray = byteArrayOutputStream.toByteArray()
        fileOutputStream.write(bytearray)
        fileOutputStream.flush()
        fileOutputStream.close()
        byteArrayOutputStream.close()

        val URI = file.toURI()
        return URI
    }
}