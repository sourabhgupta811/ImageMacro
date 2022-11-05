package com.assignment.imagemacro.data

import android.graphics.Bitmap
import android.net.Uri

sealed class BackgroundData(val type: Type) {
    enum class Type{
        IMAGE,
        SOLID,
        GRADIENT
    }
}
data class ImageURIBackgroundData(val uri: Uri): BackgroundData(Type.IMAGE)
data class ImageBitmapBackgroundData(val bitmap: Bitmap): BackgroundData(Type.IMAGE)
data class SolidBackgroundData(val color:Int): BackgroundData(Type.SOLID)
data class GradientBackgroundData(val color1:Int,val color2: Int): BackgroundData(Type.GRADIENT)