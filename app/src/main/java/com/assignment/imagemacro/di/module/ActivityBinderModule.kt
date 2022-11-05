package com.assignment.imagemacro.di.module

import com.assignment.imagemacro.screens.CustomMacroCreationActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBinderModule {
    @ContributesAndroidInjector
    abstract fun bindCustomMacroActivity(): CustomMacroCreationActivity
}