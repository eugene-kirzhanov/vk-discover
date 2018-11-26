package by.anegin.vkdiscover.core.di

import by.anegin.vkdiscover.VkDiscover
import by.anegin.vkdiscover.core.di.module.AppModule
import by.anegin.vkdiscover.core.di.module.FragmentBindingModule
import by.anegin.vkdiscover.core.di.module.ViewModelModule
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
	modules = [
		AndroidSupportInjectionModule::class,
		AppModule::class,
		FragmentBindingModule::class,
		ViewModelModule::class
	]
)
interface AppComponent {

	fun inject(application: VkDiscover)

}