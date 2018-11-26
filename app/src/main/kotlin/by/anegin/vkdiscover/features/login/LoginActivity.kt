package by.anegin.vkdiscover.features.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.anegin.vkdiscover.R
import by.anegin.vkdiscover.features.feed.FeedActivity
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)

		buttonLogin.setOnClickListener {
			requestLogin()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (!handleLogin(requestCode, resultCode, data)) {
			super.onActivityResult(requestCode, resultCode, data)
		}
	}

	private fun requestLogin() {
		VKSdk.logout()
		VKSdk.login(this, VKScope.WALL)
	}

	private fun handleLogin(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
		return VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
			override fun onResult(token: VKAccessToken) {
				proceedToMainActivity()
			}

			override fun onError(error: VKError) {
				onLoginError(error)
			}
		})
	}

	private fun proceedToMainActivity() {
		startActivity(Intent(this, FeedActivity::class.java))
		finish()
		overridePendingTransition(0, 0)
	}

	private fun onLoginError(error: VKError) {
		if (error.errorCode != VKError.VK_CANCELED) {
			Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show()
		}
	}

}