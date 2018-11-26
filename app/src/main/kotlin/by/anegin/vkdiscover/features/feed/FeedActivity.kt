package by.anegin.vkdiscover.features.feed

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.anegin.vkdiscover.R
import by.anegin.vkdiscover.features.login.LoginActivity
import com.vk.sdk.VKSdk

class FeedActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (VKSdk.isLoggedIn()) {

			setContentView(R.layout.activity_feed)

			if (savedInstanceState == null) {
				supportFragmentManager
					.beginTransaction()
					.replace(R.id.fragmentContainer, FeedFragment())
					.commit()
			}

		} else {
			proceedToLoginActivity()
		}
	}

	private fun proceedToLoginActivity() {
		startActivity(Intent(this, LoginActivity::class.java))
		finish()
		overridePendingTransition(0, 0)
	}

}
