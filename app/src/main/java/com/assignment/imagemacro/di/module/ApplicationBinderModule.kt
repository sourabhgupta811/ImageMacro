package com.assignment.imagemacro.di.module

import android.app.Application
import com.assignment.imagemacro.ImageMacroApplication
import dagger.Binds
import dagger.Module

@Module
abstract class ApplicationBinderModule {
    @Binds
    abstract fun bindApplication(application: ImageMacroApplication):Application
}