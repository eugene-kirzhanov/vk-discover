package by.anegin.vkdiscover.core.di.module

import android.app.Application
import android.content.Context
import by.anegin.vkdiscover.core.repository.FeedRepository
import by.anegin.vkdiscover.api.VkFeedRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class AppModule {

	@Singleton
	@Provides
	fun provideContext(application: Application): Context = application

	@Singleton
	@Provides
	fun provideFeedRepository(): FeedRepository = VkFeedRepository()

}