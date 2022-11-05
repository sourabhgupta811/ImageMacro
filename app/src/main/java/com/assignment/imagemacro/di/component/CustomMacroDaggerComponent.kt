package com.assignment.imagemacro.di.component

import com.samnetworks.base.dagger.IDaggerComponent
import com.samnetworks.base.viewmodel.dagger.DaggerViewModelModule
import com.assignment.imagemacro.ImageMacroApplication
import com.assignment.imagemacro.di.module.ActivityBinderModule
import com.assignment.imagemacro.di.module.ApplicationBinderModule
import com.assignment.imagemacro.di.module.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class,AndroidInjectionModule::class,DaggerViewModelModule::class, ActivityBinderModule::class, ApplicationBinderModule::class, ViewModelModule::class])
interface ICustomMacroDaggerComponent: IDaggerComponent<ImageMacroApplication> {
    @Component.Builder
    interface Builder{
        @BindsInstance
        fun application(imageMacroApplication: ImageMacroApplication): Builder
        fun build(): ICustomMacroDaggerComponent
    }
}