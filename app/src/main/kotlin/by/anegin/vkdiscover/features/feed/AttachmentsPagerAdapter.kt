package by.anegin.vkdiscover.features.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import by.anegin.vkdiscover.R
import by.anegin.vkdiscover.core.model.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

class AttachmentsPagerAdapter(
	private val attachments: List<Attachment>,
	private val glide: RequestManager
) : PagerAdapter() {

	override fun getCount() = attachments.size

	override fun isViewFromObject(view: View, obj: Any) = view == obj

	override fun instantiateItem(container: ViewGroup, position: Int): Any {

		val inflater = LayoutInflater.from(container.context)
		val itemView = inflater.inflate(R.layout.item_attachment, container, false)
		val imagePhoto = itemView.findViewById<ImageView>(R.id.imagePhoto)
		val imageIcon = itemView.findViewById<ImageView>(R.id.imageIcon)
		container.addView(itemView)

		val attachment = attachments[position]
		when (attachment) {

			is Photo -> {
				val photoUrl = attachment.getUrl("photo_1280", "photo_807", "photo_604")
				if (photoUrl != null) {
					glide
						.load(photoUrl)
						.apply(RequestOptions.centerCropTransform())
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(imagePhoto)
					imageIcon.setImageDrawable(null)
				} else {
					glide.clear(imagePhoto)
					imageIcon.setImageResource(R.drawable.ic_no_photo)
				}
			}

			is Video -> {
				val photoUrl = attachment.getPhotoUrl("photo_1280", "photo_807", "photo_604")
				if (photoUrl != null) {
					glide
						.load(photoUrl)
						.apply(RequestOptions.centerCropTransform())
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(imagePhoto)
					imageIcon.setImageDrawable(null)
				} else {
					glide.clear(imagePhoto)
					imageIcon.setImageResource(R.drawable.ic_no_video)
				}
			}

			is Audio -> {
				glide.clear(imagePhoto)
				imageIcon.setImageResource(R.drawable.ic_audio)
			}

			is Link -> {
				glide.clear(imagePhoto)
				imageIcon.setImageResource(R.drawable.ic_link)
			}
		}

		return itemView
	}

	override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
		try {
			container.removeView(obj as View)
		} catch (ignored: Exception) {
		}
	}

}