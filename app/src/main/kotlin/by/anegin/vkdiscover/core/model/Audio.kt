package by.anegin.vkdiscover.core.model

class Audio(

	private val id: Int,

	private val ownerId: Int,

	private val artist: String?,

	private val title: String?

) : Attachment {

	fun getAudioUrl() = "https://vk.com/audio${ownerId}_$id"

	fun getFullTitle(): String {
		val sb = StringBuilder()
		if (!artist.isNullOrBlank()) {
			sb.append(artist)
		}
		if (!title.isNullOrBlank()) {
			if (sb.isNotEmpty()) {
				sb.append(" - ")
			}
			sb.append(title)
		}
		return sb.toString()
	}

}