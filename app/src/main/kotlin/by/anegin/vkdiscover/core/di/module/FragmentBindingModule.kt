package by.anegin.vkdiscover.core.di.module

import by.anegin.vkdiscover.core.di.scope.PerFragment
import by.anegin.vkdiscover.features.feed.FeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
@Suppress("unused")
abstract class FragmentBindingModule {

	@PerFragment
	@ContributesAndroidInjector
	abstract fun bindFeedFragment(): FeedFragment

}