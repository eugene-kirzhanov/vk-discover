package by.anegin.vkdiscover.core.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkdiscover.core.di.viewmodel.ViewModelFactory
import by.anegin.vkdiscover.core.di.viewmodel.ViewModelKey
import by.anegin.vkdiscover.features.feed.FeedViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
@Suppress("unused")
abstract class ViewModelModule {

	@Binds
	internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@Binds
	@IntoMap
	@ViewModelKey(FeedViewModel::class)
	internal abstract fun bindFeedViewModel(viewModel: FeedViewModel): ViewModel

}