package by.anegin.vkdiscover.core.model

class PostSource(

	val id: Int,

	val name: String?,

	private val screenName: String?,

	val photo: Photo?

) {

	fun getSourceUrl() = "https://vk.com/$screenName"

}