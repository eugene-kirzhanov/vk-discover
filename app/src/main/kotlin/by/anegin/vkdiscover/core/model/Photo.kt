package by.anegin.vkdiscover.core.model

class Photo(

	private val urls: Map<String, String>        // photo urls with different dimensions: key looks like "photo_{size}", value is url

) : Attachment {

	fun getUrl(vararg keys: String): String? {
		for (key in keys) {
			val url = urls[key]
			if (url != null) return url
		}
		return urls.values.firstOrNull()
	}

}