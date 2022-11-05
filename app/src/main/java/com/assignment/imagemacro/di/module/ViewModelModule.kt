package com.assignment.imagemacro.di.module

import androidx.lifecycle.ViewModel
import com.samnetworks.base.viewmodel.dagger.DaggerViewModelModule
import com.assignment.imagemacro.screens.CustomMacroCreationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @DaggerViewModelModule.ViewModelKey(CustomMacroCreationViewModel::class)
    abstract fun bindCustomMacroViewModel(viewModel: CustomMacroCreationViewModel): ViewModel
}