package by.anegin.vkdiscover.core.model

class Post(

	val id: Int,                               // post id (to send like/skip request)

	val time: Long,                            // post creation time

	val text: String?,                         // post text

	val attachments: List<Attachment>?,        // list of post photos/videos/links

	val source: PostSource                     // post source (profile or group)

)