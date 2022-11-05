package com.assignment.imagemacro.data

import android.net.Uri

sealed class Overlay(val tag:String){
    //TODO: normalize these values so that these can fit different aspect ratios of devices.
    var mScale:Float? = null
    var mRotation:Float? = null
    var mTranslation:Pair<Float,Float>? = null
}

data class ImageUriOverlay(private val _tag:String,val uri: Uri): Overlay(_tag)
data class TextOverlay(private val _tag:String,val text:String): Overlay(_tag)
