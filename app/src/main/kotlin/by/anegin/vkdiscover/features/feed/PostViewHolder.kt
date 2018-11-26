package by.anegin.vkdiscover.features.feed

import android.animation.ValueAnimator
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import by.anegin.vkdiscover.R
import by.anegin.vkdiscover.core.model.*
import by.anegin.vkdiscover.core.util.DateUtils
import by.anegin.vkdiscover.core.view.ExpandableTextView
import by.anegin.vkdiscover.core.view.LinePageIndicator
import by.anegin.vkdiscover.core.view.NonSwipeableViewPager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

class PostViewHolder(
	private val itemView: View,
	private val glide: RequestManager,
	private val onExpandStateChangeListener: (Boolean, Boolean) -> Unit,            // (isExpanded, isAnimating)
	private val onProfileClickListener: (PostSource?) -> Unit,                      // (postSource)
	private val onOpenAttachmentListener: (Attachment?) -> Unit                     // (attachment)
) {

	private val imageAvatar: ImageView = itemView.findViewById(R.id.imageAvatar)
	private val textName: TextView = itemView.findViewById(R.id.textName)
	private val textTime: TextView = itemView.findViewById(R.id.textTime)
	private val textPost: ExpandableTextView = itemView.findViewById(R.id.textPost)

	private val viewPagerPhotos: NonSwipeableViewPager = itemView.findViewById(R.id.viewPagerPhotos)
	private val indicatorPhotos: LinePageIndicator = itemView.findViewById(R.id.indicatorPhotos)

	private val buttonPrevPhoto: View = itemView.findViewById(R.id.buttonPrevPhoto)
	private val buttonNextPhoto: View = itemView.findViewById(R.id.buttonNextPhoto)

	private val buttonOpenAttachment: View = itemView.findViewById(R.id.buttonOpenAttachment)
	private val imageAttachmentIcon: ImageView = itemView.findViewById(R.id.imageAttachmentIcon)
	private val textAttachmentInfo: TextView = itemView.findViewById(R.id.textAttachmentInfo)

	private val badgeLike: View = itemView.findViewById(R.id.badgeLike)
	private val badgeSkip: View = itemView.findViewById(R.id.badgeSkip)

	private var likeAnimator: ValueAnimator? = null
	private var likeAnimatorEndValue: Float? = null
	private var skipAnimator: ValueAnimator? = null
	private var skipAnimatorEndValue: Float? = null

	private var currentPostId = 0
	private var currentAttachment: Attachment? = null

	init {
		reset()
		textPost.setListener(object : ExpandableTextView.Listener {
			override fun onAnimationStarted(expanding: Boolean) {
				onExpandStateChangeListener(!expanding, true)
			}

			override fun onStateChanged(expanded: Boolean) {
				onExpandStateChangeListener(expanded, false)
			}
		})
		buttonOpenAttachment.setOnClickListener {
			onOpenAttachmentListener(currentAttachment)
		}
	}

	fun bind(post: Post?) {
		if (post == null) {
			currentPostId = 0
			itemView.visibility = View.GONE
			return
		}
		itemView.visibility = View.VISIBLE

		if (post.id == currentPostId) return
		currentPostId = post.id

		// profile/group avatar
		if (post.source.photo != null) {
			val url = post.source.photo.getUrl("photo_100", "photo_50", "photo_200")
			glide
				.load(url)
				.apply(
					RequestOptions
						.circleCropTransform()
						.error(R.drawable.no_avatar)
				)
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(imageAvatar)
		} else {
			glide
				.load(R.drawable.no_avatar)
				.apply(RequestOptions.circleCropTransform())
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(imageAvatar)
		}
		imageAvatar.setOnClickListener {
			onProfileClickListener(post.source)
		}

		// profile/group name
		textName.text = post.source.name ?: "..."
		textName.setOnClickListener {
			onProfileClickListener(post.source)
		}

		// post time
		textTime.text = DateUtils.formatDateTime(textTime.context, post.time)

		// post text
		textPost.setText(post.text)
		textPost.setOnClickListener {
			textPost.toggle()
		}

		// photos
		val attachments = if (!post.attachments.isNullOrEmpty()) {
			post.attachments
		} else {
			listOf(Photo(emptyMap()))
		}
		viewPagerPhotos.adapter = AttachmentsPagerAdapter(attachments, glide)

		if (attachments.size > 1) {
			buttonPrevPhoto.setOnClickListener {
				val currentItem = indicatorPhotos.getCurrentItem()
				if (currentItem > 0) {
					indicatorPhotos.setCurrentItem(currentItem - 1)
				}
			}
			buttonNextPhoto.setOnClickListener {
				val currentItem = indicatorPhotos.getCurrentItem()
				if (currentItem < attachments.size - 1) {
					indicatorPhotos.setCurrentItem(currentItem + 1)
				}
			}

			indicatorPhotos.visibility = View.VISIBLE
			indicatorPhotos.setViewPager(viewPagerPhotos)
			indicatorPhotos.setCurrentItem(0)

			indicatorPhotos.setListener(object : LinePageIndicator.Listener {
				override fun onPageChanged(position: Int) {
					updateAttachmentInfo(attachments[position])
				}
			})

		} else {
			buttonPrevPhoto.setOnClickListener(null)
			buttonNextPhoto.setOnClickListener(null)
			buttonPrevPhoto.isClickable = false
			buttonNextPhoto.isClickable = false

			indicatorPhotos.visibility = View.GONE
			indicatorPhotos.setViewPager(null)
			indicatorPhotos.setListener(null)

		}

		updateAttachmentInfo(attachments[0])
	}

	private fun updateAttachmentInfo(attachment: Attachment) {
		currentAttachment = attachment
		when (attachment) {
			is Photo -> {
				buttonOpenAttachment.visibility = View.GONE
				imageAttachmentIcon.visibility = View.GONE
				textAttachmentInfo.visibility = View.GONE
			}
			is Video -> {
				buttonOpenAttachment.visibility = View.VISIBLE
				imageAttachmentIcon.visibility = View.VISIBLE
				textAttachmentInfo.visibility = View.VISIBLE
				imageAttachmentIcon.setImageResource(R.drawable.ic_play_video)

				var text = textAttachmentInfo.context.getString(R.string.press_to_watch_video)
				if (!attachment.title.isNullOrBlank()) {
					text += "\n" + attachment.title
				}
				textAttachmentInfo.text = text
			}
			is Audio -> {
				buttonOpenAttachment.visibility = View.VISIBLE
				imageAttachmentIcon.visibility = View.VISIBLE
				textAttachmentInfo.visibility = View.VISIBLE
				imageAttachmentIcon.setImageResource(R.drawable.ic_play_audio)

				var text = textAttachmentInfo.context.getString(R.string.press_to_play_audio)
				val audioTitle = attachment.getFullTitle()
				if (audioTitle.isNotBlank()) {
					text += "\n" + audioTitle
				}
				textAttachmentInfo.text = text
			}
			is Link -> {
				buttonOpenAttachment.visibility = View.VISIBLE
				imageAttachmentIcon.visibility = View.VISIBLE
				textAttachmentInfo.visibility = View.VISIBLE
				imageAttachmentIcon.setImageResource(R.drawable.ic_open_link)
				textAttachmentInfo.setText(R.string.press_to_open_link)
			}
		}
	}

	fun reset() {
		likeAnimatorEndValue = null
		likeAnimator?.cancel()
		skipAnimatorEndValue = null
		skipAnimator?.cancel()

		badgeLike.alpha = 0f
		badgeSkip.alpha = 0f

		textPost.setExpanded(false, false)
	}

	fun onSwipe(value: Float) {

		var likeAlpha = 0f
		var skipAlpha = 0f
		if (value > 0f) {
			likeAlpha = 1f
			skipAlpha = 0f
		} else if (value < 0f) {
			likeAlpha = 0f
			skipAlpha = 1f
		}

		if (likeAnimatorEndValue != likeAlpha) {
			likeAnimatorEndValue = likeAlpha
			likeAnimator?.cancel()
			likeAnimator = makeAlphaAnimation(badgeLike, likeAlpha)
			likeAnimator?.start()
		}

		if (skipAnimatorEndValue != skipAlpha) {
			skipAnimatorEndValue = skipAlpha
			skipAnimator?.cancel()
			skipAnimator = makeAlphaAnimation(badgeSkip, skipAlpha)
			skipAnimator?.start()
		}

	}

	private fun makeAlphaAnimation(view: View, toAlpha: Float): ValueAnimator {
		val anim = ValueAnimator.ofFloat(view.alpha, toAlpha)
		anim.duration = if (toAlpha > view.alpha) 250L else 150L
		anim.addUpdateListener {
			view.alpha = it.animatedValue as Float
		}
		return anim
	}

}