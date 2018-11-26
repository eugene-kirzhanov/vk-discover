package by.anegin.vkdiscover.features.feed

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkdiscover.R
import by.anegin.vkdiscover.core.model.*
import by.anegin.vkdiscover.core.util.getViewModel
import by.anegin.vkdiscover.core.util.observe
import by.anegin.vkdiscover.core.view.LockableScrollView
import by.anegin.vkdiscover.core.view.SwipeableFeedLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_feed.*
import javax.inject.Inject

class FeedFragment : Fragment(), SwipeableFeedLayout.Listener {

	@Inject
	lateinit var viewModelFactory: ViewModelProvider.Factory

	private val feedViewModel: FeedViewModel by lazy {
		getViewModel<FeedViewModel>(viewModelFactory)
	}

	private lateinit var lockableScrollView: LockableScrollView
	private lateinit var swipeableFeedLayout: SwipeableFeedLayout

	private lateinit var glide: RequestManager

	override fun onCreate(savedInstanceState: Bundle?) {
		AndroidSupportInjection.inject(this)
		super.onCreate(savedInstanceState)
	}

	override fun onAttach(context: Context?) {
		super.onAttach(context)
		glide = Glide.with(this)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_feed, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		lockableScrollView = view.findViewById(R.id.lockableScrollView)

		swipeableFeedLayout = view.findViewById(R.id.swipeableFeedLayout)
		swipeableFeedLayout.setListener(this)

		buttonSkip.setOnClickListener {
			swipeableFeedLayout.swipeToLeft()
		}
		buttonLike.setOnClickListener {
			swipeableFeedLayout.swipeToRight()
		}

		buttonRetry.setOnClickListener {
			showProgress()
			feedViewModel.retryLoadFeed()
		}

		feedViewModel.apply {
			observe(frontPost) {
				getFrontViewHolder()?.bind(it)
			}
			observe(backPost) {
				getBackViewHolder()?.bind(it)
			}
			observe(inProgress) {
				if (it == true) {
					showProgress()
				} else {
					hideProgress()
				}
			}
			observe(postActionError) {
				Toast.makeText(context, R.string.request_failed, Toast.LENGTH_SHORT).show()
			}
			observe(feedRequestError) {
				showRequestFeedError()
			}
		}
	}

	// ======== SwipeableFeedLayout.Listener =========

	override fun onSwipe(value: Float) {
		getFrontViewHolder()?.onSwipe(value)
		getBackViewHolder()?.reset()
	}

	override fun onSwipeCanceled() {
		getFrontViewHolder()?.onSwipe(0f)
		getBackViewHolder()?.reset()
	}

	override fun onSwipedToRight() {
		getBackViewHolder()?.reset()

		swipeableFeedLayout.setSwipeDisabled(false)
		lockableScrollView.setScrollEnabled(false)

		feedViewModel.likeFirstPost()
	}

	override fun onSwipedToLeft() {
		getBackViewHolder()?.reset()

		swipeableFeedLayout.setSwipeDisabled(false)
		lockableScrollView.setScrollEnabled(false)

		feedViewModel.skipFirstPost()
	}

	// =================

	private fun getBackViewHolder() = getPostViewHolder(swipeableFeedLayout.getBackLayout())

	private fun getFrontViewHolder() = getPostViewHolder(swipeableFeedLayout.getFrontLayout())

	private fun getPostViewHolder(itemView: View?): PostViewHolder? {
		var holder = itemView?.tag as? PostViewHolder
		if (holder == null && itemView != null) {
			holder = PostViewHolder(itemView, glide,
				{ isExpanded, isAnimating ->
					val verticalScrollEnabled = isAnimating || isExpanded
					swipeableFeedLayout.setSwipeDisabled(verticalScrollEnabled)
					lockableScrollView.setScrollEnabled(verticalScrollEnabled)
				},
				{ openPostSource(it) },
				{ openAttachment(it) }
			)
			itemView.tag = holder
		}
		return holder
	}

	// =================

	private fun showProgress() {
		groupContent.visibility = View.INVISIBLE
		groupError.visibility = View.GONE
		groupProgress.visibility = View.VISIBLE
	}

	private fun hideProgress() {
		groupContent.visibility = View.VISIBLE
		groupError.visibility = View.GONE
		groupProgress.visibility = View.GONE
	}

	private fun showRequestFeedError() {
		groupContent.visibility = View.INVISIBLE
		groupError.visibility = View.VISIBLE
		groupProgress.visibility = View.GONE
	}

	private fun openPostSource(postSource: PostSource?) {
		if (postSource == null) return
		openUrl(postSource.getSourceUrl())
	}

	private fun openAttachment(attachment: Attachment?) {
		when (attachment) {
			is Video -> {
				openUrl(attachment.getVideoUrl())
			}
			is Audio -> {
				openUrl(attachment.getAudioUrl())
			}
			is Link -> {
				openUrl(attachment.getUrl())
			}
		}
	}

	private fun openUrl(url: String): Boolean {
		val customTabsIntent = CustomTabsIntent.Builder()
			.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
			.build()
		customTabsIntent.intent.putExtra(
			Intent.EXTRA_REFERRER,
			Uri.parse("android-app://" + requireContext().packageName)
		)
		return try {
			customTabsIntent.launchUrl(context, Uri.parse(url))
			true
		} catch (e: ActivityNotFoundException) {
			e.printStackTrace()
			false
		}
	}

}
