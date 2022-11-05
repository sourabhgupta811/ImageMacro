package com.assignment.imagemacro.screens

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.assignment.imagemacro.data.*
import com.samnetworks.base.utils.SingleLiveEvent
import com.samnetworks.base.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.Stack
import javax.inject.Inject
import kotlin.random.Random

class CustomMacroCreationViewModel @Inject constructor(private val application: Application) :
    BaseViewModel() {
    private val mSolidBackgroundDataObserver = MutableLiveData<Pair<String, SolidBackgroundData>?>()
    private val mGradientBackgroundDataObserver =
        MutableLiveData<Pair<String, GradientBackgroundData>?>()

    private val mOverlayList = mutableListOf<Overlay>()
    private val mOverlayDataObserver = MutableLiveData<List<Overlay>>()

    private val mSaveMacroObserver = SingleLiveEvent<File?>()
    private val mMacroSaveStatusObserver = SingleLiveEvent<Pair<File?,String>>()

    private val mOverlayMoveObserver = SingleLiveEvent<Pair<Overlay, Pair<Float, Float>>?>()
    private val mActionTaskStack = Stack<Action>()


    fun getSolidBackgroundObserver(): LiveData<Pair<String, SolidBackgroundData>?> =
        mSolidBackgroundDataObserver

    fun getGradientBackgroundDataObserver(): LiveData<Pair<String, GradientBackgroundData>?> =
        mGradientBackgroundDataObserver

    fun getOverlayDataObserver(): LiveData<List<Overlay>> = mOverlayDataObserver

    fun getSaveMacroObserver(): LiveData<File?> = mSaveMacroObserver
    fun getMacroSaveStatusObserver(): LiveData<Pair<File?,String>> = mMacroSaveStatusObserver
    fun getOverlayMoveObserver(): LiveData<Pair<Overlay, Pair<Float, Float>>?> =
        mOverlayMoveObserver

    fun applyBackground(backgroundData: BackgroundData) {
        when (backgroundData) {
            is SolidBackgroundData -> {
                addSolidBackgroundData(backgroundData)
            }
            is GradientBackgroundData -> {
                addGradientBackgroundData(backgroundData)
            }
            else -> {

            }
        }
    }

    private fun addSolidBackgroundData(solidBackgroundData: SolidBackgroundData) {
        Timber.d("Posted value to Observer: $solidBackgroundData")
        mSolidBackgroundDataObserver.postValue(Pair("background", solidBackgroundData))
    }

    private fun addGradientBackgroundData(gradientBackgroundData: GradientBackgroundData) {
        Timber.d("Posted value to Observer: $gradientBackgroundData")
        mGradientBackgroundDataObserver.postValue(Pair("background", gradientBackgroundData))
    }

    fun addImageOverlay(uri: Uri) {
        val tag = Random.nextInt().toString()
        val overlay = ImageUriOverlay(tag, uri)
        mOverlayList.add(overlay)
        mOverlayDataObserver.postValue(mOverlayList)
        mActionTaskStack.push(OverlayAddAction(overlay))
    }

    fun addTextOverlay(text: String) {
        val tag = Random.nextInt().toString()
        val overlay = TextOverlay(tag, text)
        mOverlayList.add(overlay)
        mOverlayDataObserver.postValue(mOverlayList)
        mActionTaskStack.push(OverlayAddAction(overlay))
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun saveMacro() {
        mSaveMacroObserver.postValue(File(application.applicationContext.cacheDir,
            "SavedMacro.png"))
    }

    fun undoOverlayAddition() {
        if (mActionTaskStack.isEmpty()) return
        when (val action = mActionTaskStack.pop()) {
            is OverlayAddAction -> {
                val lastAddedOverlay = action.overlay
                mOverlayList.remove(lastAddedOverlay)
                mOverlayDataObserver.postValue(mOverlayList)
            }
            is MoveAction -> {
                val newPos = action.startPos
                mOverlayMoveObserver.postValue(Pair(action.overlay, newPos))
            }
        }
    }

    fun addAction(it: Action) {
        mActionTaskStack.push(it)
    }

    fun saveMacroBitmap(bitmap: Bitmap?, file: File?) {
        if (file != null && bitmap != null) {
            mIoScope.launch {
                try {
                    if(file.exists()) file.delete()
                    val os = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                    mMacroSaveStatusObserver.postValue(Pair(file,"Bitmap Saved Successfully at :${file.absolutePath}"))
                } catch (exception: Exception) {
                    mMacroSaveStatusObserver.postValue(Pair(null,"Failed to save Macro to File with exception: ${exception.message}"))
                }
            }
        } else {
            mMacroSaveStatusObserver.postValue(Pair(null,"Failed to generate Bitmap!!"))
        }
    }
}