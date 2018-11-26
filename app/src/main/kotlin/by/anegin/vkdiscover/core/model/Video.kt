package by.anegin.vkdiscover.core.model

class Video(

	private val id: Int,

	private val ownerId: Int,

	val title: String?,

	private val photoUrls: Map<String, String>?

) : Attachment {

	fun getPhotoUrl(vararg keys: String): String? {
		if (photoUrls == null) return null
		for (key in keys) {
			val url = photoUrls[key]
			if (url != null) return url
		}
		return photoUrls.values.firstOrNull()
	}

	fun getVideoUrl() = "https://vk.com/video${ownerId}_$id"

}