package com.assignment.imagemacro

import com.samnetworks.base.application.MvvmApplication
import com.samnetworks.base.dagger.IDaggerComponent
import com.assignment.imagemacro.di.component.DaggerICustomMacroDaggerComponent
import com.assignment.imagemacro.BuildConfig
import timber.log.Timber

class ImageMacroApplication :MvvmApplication(){
    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun provideDaggerComponent(): IDaggerComponent<MvvmApplication> {
        return DaggerICustomMacroDaggerComponent.builder().application(this).build() as IDaggerComponent<MvvmApplication>
    }
}