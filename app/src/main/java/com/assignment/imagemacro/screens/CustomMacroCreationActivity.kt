package com.assignment.imagemacro.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.samnetworks.base.view.BaseActivity
import com.assignment.imagemacro.BuildConfig
import com.assignment.imagemacro.R
import com.assignment.imagemacro.data.GradientBackgroundData
import com.assignment.imagemacro.data.SolidBackgroundData
import com.assignment.imagemacro.databinding.ActivityCustomMacroCreationBinding
import com.assignment.imagemacro.utils.dialog.DialogUtil
import timber.log.Timber
import java.io.File

class CustomMacroCreationActivity : BaseActivity<ActivityCustomMacroCreationBinding, CustomMacroCreationViewModel>(){
    companion object{
        private const val PICK_IMAGE_CODE = 0x01
        private const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0x02
    }
    private var permissionGranted = false

    override fun onActivityCreated(binding: ActivityCustomMacroCreationBinding) {
        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.addSolidBackground.setOnClickListener {
            Timber.d("Solid Background Listener Called")
            DialogUtil.showColorPickerDialog(this,"Choose Solid Background"){
                viewModel.applyBackground(SolidBackgroundData(it))
            }
        }
        binding.addGradientBackgroundButton.setOnClickListener {
            Timber.d("Gradient Background Listener Called")
            viewModel.applyBackground(GradientBackgroundData(ContextCompat.getColor(this,R.color.teal_700),ContextCompat.getColor(this,R.color.teal_200)))
        }
        binding.gradientColor1.setOnClickListener {
            DialogUtil.showColorPickerDialog(this,"Choose Gradient Start Color"){
                viewModel.applyBackground(GradientBackgroundData(it,viewModel.getGradientBackgroundDataObserver().value?.second?.color2?:ContextCompat.getColor(this,R.color.teal_200)))
            }
        }
        binding.gradientColor2.setOnClickListener {
            DialogUtil.showColorPickerDialog(this,"Choose Gradient End Color"){
                viewModel.applyBackground(GradientBackgroundData(viewModel.getGradientBackgroundDataObserver().value?.second?.color2?:ContextCompat.getColor(this,R.color.teal_700),it))
            }
        }
        binding.addImage.setOnClickListener {
            Timber.d("Add Image Listener Called")
            launchImageIntent()
        }
        binding.addText.setOnClickListener {
            DialogUtil.showTextEditDialog(this,"Add Text"){
                viewModel.addTextOverlay(it)
            }
            Timber.d("Add text Listener Called")
        }
        binding.undoOverlayAddition.setOnClickListener {
            viewModel.undoOverlayAddition()
        }
        binding.saveMacro.setOnClickListener {
            viewModel.saveMacro()
        }
        binding.customMacroView.setActionListener {
            viewModel.addAction(it)
        }
    }

    private fun setupObservers() {
        viewModel.getOverlayDataObserver().observe(this){
            it?.let {
                Timber.d("Image Overlay Observer Called : {$it}")
                binding.customMacroView.addOverlays(it)
            }
        }
        viewModel.getSolidBackgroundObserver().observe(this){
            it?.let {
                Timber.d("Solid Background Observer Called: {$it}")
                if(binding.gradientColor1.isVisible){
                    binding.gradientColor1.visibility = View.GONE
                }
                if(binding.gradientColor2.isVisible){
                    binding.gradientColor2.visibility = View.GONE
                }
                binding.customMacroView.addSolidBackground(it.first,it.second)
            }
        }
        viewModel.getGradientBackgroundDataObserver().observe(this){
            it?.let {
                Timber.d("Gradient Background Observer Called: {$it}")
                binding.customMacroView.addGradientBackground(it.first,it.second)
                if(!binding.gradientColor1.isVisible){
                    binding.gradientColor1.visibility = View.VISIBLE
                }
                if(!binding.gradientColor2.isVisible){
                    binding.gradientColor2.visibility = View.VISIBLE
                }
                binding.gradientColor1.background.setTint(it.second.color1)
                binding.gradientColor2.background.setTint(it.second.color2)
            }
        }
        viewModel.getSaveMacroObserver().observe(this){
            it?.let {
                val bitmap  = binding.customMacroView.getBitmap()
                viewModel.saveMacroBitmap(bitmap,it)
            }
        }
        viewModel.getOverlayMoveObserver().observe(this){
            it?.let {
                binding.customMacroView.moveOverlay(it.first,it.second)
            }
        }

        viewModel.getMacroSaveStatusObserver().observe(this){ it ->
            it.first?.let { file ->
                share(file)
            }
            Toast.makeText(applicationContext,it.second,Toast.LENGTH_SHORT).show()
        }
    }

    private fun share(file: File) {
        val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".media_provider", file)
        val videoShareIntent = Intent(Intent.ACTION_SEND)
        videoShareIntent.type = "*/*"
        videoShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        videoShareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(videoShareIntent)
    }

    private fun launchImageIntent() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionGranted = checkPermission(permissions, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        if(permissionGranted) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGE_CODE)
        }else{
            ActivityCompat.requestPermissions(this,permissions, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkPermission(permissions: Array<out String>,permissionCode:Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        var permissionGranted = true
        for(permission in permissions){
            permissionGranted = permissionGranted && (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
        }
        if (!permissionGranted) {
            requestPermissions(permissions, permissionCode)
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== PICK_IMAGE_CODE && resultCode== RESULT_OK) {
            val uri = data?.data
            if(data!=null && uri != null) {
                viewModel.addImageOverlay(uri)
            }else{
                Toast.makeText(this, "Could not import Image File!",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE ->{
                if(checkPermissionGranted(grantResults)){
                    launchImageIntent()
                }
            }
        }
    }

    private fun checkPermissionGranted(grantResults: IntArray):Boolean{
        if (grantResults.isNotEmpty()) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        applicationContext,
                        "Permission not Granted",
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
            }
            return true
        }
        return false
    }

    override fun onActivityDestroyed(binding: ActivityCustomMacroCreationBinding) {

    }

    override fun getLayoutId(): Int  = R.layout.activity_custom_macro_creation

    override fun getViewModel(): Class<CustomMacroCreationViewModel>  = CustomMacroCreationViewModel::class.java
}