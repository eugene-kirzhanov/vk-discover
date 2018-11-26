package by.anegin.vkdiscover

import android.app.Application
import androidx.fragment.app.Fragment
import by.anegin.vkdiscover.core.di.DaggerAppComponent
import com.vk.sdk.VKSdk
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class VkDiscover : Application(), HasSupportFragmentInjector {

	@Inject
	lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

	override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

	override fun onCreate() {
		super.onCreate()

		DaggerAppComponent.create().inject(this)

		VKSdk.initialize(this)
	}

}