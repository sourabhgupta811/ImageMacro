package com.assignment.imagemacro.custom_view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.almeros.android.multitouch.MoveGestureDetector
import com.almeros.android.multitouch.MoveGestureDetector.SimpleOnMoveGestureListener
import com.almeros.android.multitouch.RotateGestureDetector
import com.almeros.android.multitouch.RotateGestureDetector.SimpleOnRotateGestureListener
import com.assignment.imagemacro.data.*
import com.assignment.imagemacro.R
import timber.log.Timber


class CustomMacroCreationView : FrameLayout{
    private val mOverlaysInMacro = mutableListOf<Overlay>()
    private var mActionListener:((action: Action)->Unit)? = null
    constructor(context: Context):super(context){
        isDrawingCacheEnabled = true
    }

    constructor(context: Context, attrs: AttributeSet?):super(context,attrs){
        isDrawingCacheEnabled = true
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr){
        isDrawingCacheEnabled = true
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):super(context, attrs, defStyleAttr, defStyleRes){
        isDrawingCacheEnabled = true
    }

    fun addOverlays(it: List<Overlay>) {
        //remove extra views
        for(overlay in mOverlaysInMacro){
            if(!it.contains(overlay)){
                removeView(overlay)
            }
        }
        //add new views
        for(overlay in it){
            val view = findViewWithTag<View?>(overlay.tag)
            if(view==null){
                when(overlay){
                    is ImageUriOverlay ->{
                        addImageOverlay(overlay)
                    }
                    is TextOverlay ->{
                        addTextOverlay(overlay)
                    }
                }
            }
        }
    }

    fun setActionListener(listener:(action: Action)->Unit){
        mActionListener = listener
    }

    private fun addTextOverlay(textOverlay: TextOverlay){
        Timber.d("Add Text Overlay Called in Custom View with $textOverlay")
        removeView(textOverlay)
        val newView = LayoutInflater.from(context).inflate(R.layout.layout_text_view,this,false) as TextView
        newView.tag = textOverlay.tag
        newView.text = textOverlay.text
        addView(newView)
        mOverlaysInMacro.add(textOverlay)
        applyScaleRotateTranslate(newView,textOverlay)
        makeIntractable(newView,textOverlay)
    }

    private fun addImageOverlay(imageUriOverlay: ImageUriOverlay){
        Timber.d("Add Overlay Image Called in Custom View with $imageUriOverlay")
        removeView(imageUriOverlay)
        val newView = LayoutInflater.from(context).inflate(R.layout.layout_image_view,this,false) as ImageView
        newView.setImageURI(imageUriOverlay.uri)
        newView.tag = imageUriOverlay.tag
        addView(newView)
        mOverlaysInMacro.add(imageUriOverlay)
        applyScaleRotateTranslate(newView,imageUriOverlay)
        makeIntractable(newView,imageUriOverlay)
    }

    private fun removeView(overlay: Overlay){
        val oldView = findViewWithTag<View>(overlay.tag)
        if(oldView!=null) {
            Timber.d("Removed view with tag ${overlay.tag}")
            removeView(oldView)
            mOverlaysInMacro.remove(overlay)
        }
    }

    fun addSolidBackground(tag:String, solidBackgroundData: SolidBackgroundData){
        Timber.d("Add Solid Background Called in Custom View with $solidBackgroundData")
        if(background !is ColorDrawable){
            val newDrawable = ColorDrawable()
            background = newDrawable
        }
        (background as ColorDrawable).color = solidBackgroundData.color
    }

    fun addGradientBackground(tag:String, gradientBackgroundData: GradientBackgroundData){
        Timber.d("Add Gradient Background Called in Custom View with $gradientBackgroundData")
        if(background !is GradientDrawable){
            val newDrawable = GradientDrawable()
            newDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
            background = newDrawable
        }
        (background as GradientDrawable).colors = intArrayOf(gradientBackgroundData.color1,gradientBackgroundData.color2)
    }

    fun getBitmap():Bitmap? {
        return try{
            destroyDrawingCache()
            buildDrawingCache()
            return getDrawingCache()
        }catch (exception:Exception){
            null
        }
    }

    private fun applyScaleRotateTranslate(view:View,overlay: Overlay){
        overlay.mScale?.let {
            view.scaleX = it
            view.scaleY = it
        }
        overlay.mRotation?.let {
            view.rotation = it
        }
        overlay.mTranslation?.let {
            view.translationX = it.first
            view.translationY = it.second
        }
        if(overlay.mScale==null){
            overlay.mScale = view.scaleX
        }
        if(overlay.mRotation==null){
            overlay.mRotation = view.rotation
        }
        if(overlay.mTranslation==null){
            overlay.mTranslation = Pair(view.translationX,view.translationY)
        }
    }

    private fun makeIntractable(view:View,overlay: Overlay){
        view.setOnTouchListener(object : View.OnTouchListener{
            private var mScaleFactor = 1.0f
            private var mRotationDegrees = 0f
            private var mFocusX = 0f
            private var mFocusY = 0f
            private var mScaleDetector: ScaleGestureDetector? = null
            private var mRotateDetector: RotateGestureDetector? = null
            private var mMoveDetector: MoveGestureDetector? = null
            private var startOverlayPos: Pair<Float,Float>? = null
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                when(event?.action){
                    MotionEvent.ACTION_DOWN->{
                        view?.let {
                            startOverlayPos = Pair(it.translationX,it.translationY)
                        }
                    }
                    MotionEvent.ACTION_UP->{
                        if(overlay.mTranslation?.equals(startOverlayPos)!=true){
                            val startPos = startOverlayPos
                            view?.let {
                                val endPos = Pair(it.translationX,it.translationY)
                                if(startPos!=null){
                                    mActionListener?.invoke(MoveAction(overlay,startPos, endPos))
                                }
                            }
                        }
                    }
                }
                if(mScaleDetector==null){
                    mScaleDetector = ScaleGestureDetector(context, ScaleListener{
                        mScaleFactor *= it // scale change since previous event
                        view?.scaleX = mScaleFactor
                        view?.scaleY = mScaleFactor
                        overlay.mScale = mScaleFactor
                    })
                }
                if(mRotateDetector==null){
                    mRotateDetector = RotateGestureDetector(context, RotateListener{
                        mRotationDegrees -= it
                        overlay.mRotation = mRotationDegrees
                        view?.rotation = mRotationDegrees
                    })
                }
                if(mMoveDetector==null){
                    mMoveDetector = MoveGestureDetector(context, MoveListener{dx,dy->
                        mFocusX += dx
                        mFocusY += dy
                        view?.translationX = mFocusX
                        view?.translationY = mFocusY
                        overlay.mTranslation = Pair(mFocusX,mFocusY)
                    })
                }
                event?.let {
                    mScaleDetector?.onTouchEvent(it)
                    mRotateDetector?.onTouchEvent(it)
                    mMoveDetector?.onTouchEvent(it)
                }
                return true
            }
        })
    }

    fun moveOverlay(overlay: Overlay, newPos: Pair<Float, Float>) {
        val view = findViewWithTag<View?>(overlay.tag)
        view?.let {
            view.translationX = newPos.first
            view.translationY = newPos.second
        }
        overlay.mTranslation = newPos
    }

    private class MoveListener(val listener: (dx:Float, fy:Float)->Unit) : SimpleOnMoveGestureListener() {
        override fun onMoveBegin(detector: MoveGestureDetector?): Boolean {
            return super.onMoveBegin(detector)
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            val d = detector.focusDelta
            listener.invoke(d.x,d.y)
            return true
        }

        override fun onMoveEnd(detector: MoveGestureDetector?) {
            super.onMoveEnd(detector)
        }
    }
    private class RotateListener(val listener:(delta:Float)->Unit) : SimpleOnRotateGestureListener() {
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            listener.invoke(detector.rotationDegreesDelta)
            return true
        }
    }
    private class ScaleListener(val listener:(scaleFactor:Float)->Unit) : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            listener.invoke(detector.scaleFactor)
            return true // indicate event was handled
        }
    }
}