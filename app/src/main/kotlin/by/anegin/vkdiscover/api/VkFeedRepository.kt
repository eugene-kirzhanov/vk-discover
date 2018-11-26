package by.anegin.vkdiscover.api

import by.anegin.vkdiscover.core.model.*
import by.anegin.vkdiscover.core.repository.FeedRepository
import com.vk.sdk.api.*
import io.reactivex.Completable
import io.reactivex.Single
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class VkFeedRepository : FeedRepository {

	override fun likePost(post: Post): Completable {
		return Completable.create {

			val params = VKParameters.from(
				"type", "post",
				VKApiConst.OWNER_ID, post.source.id,
				"item_id", post.id
			)

			val request = VKRequest("likes.add", params)

			request.executeWithListener(object : VKRequest.VKRequestListener() {
				override fun onComplete(response: VKResponse) {
					it.onComplete()
				}

				override fun onError(error: VKError) {
					it.onError(IOException(error.errorMessage))
				}
			})
		}
	}

	override fun skipPost(post: Post): Completable {
		return Completable.create {

			val params = VKParameters.from(
				"type", "wall",
				VKApiConst.OWNER_ID, post.source.id,
				"item_id", post.id
			)

			val request = VKRequest("newsfeed.ignoreItem", params)

			request.executeWithListener(object : VKRequest.VKRequestListener() {
				override fun onComplete(response: VKResponse) {
					it.onComplete()
				}

				override fun onError(error: VKError) {
					it.onError(IOException(error.errorMessage))
				}
			})
		}
	}

	override fun getRecommendedFeed(count: Int, startFrom: String?): Single<Feed> {
		return Single.create<Feed> {

			val params = VKParameters.from(
				"start_from", startFrom,
				VKApiConst.COUNT, count,
				VKApiConst.EXTENDED, 1
			)

			val request = VKRequest("newsfeed.getDiscoverForContestant", params)

			request.executeWithListener(object : VKRequest.VKRequestListener() {
				override fun onComplete(response: VKResponse) {
					val feed = parseFeedResponse(response)
					if (feed != null) {
						it.onSuccess(feed)
					} else {
						it.onError(IOException("Invalid API response"))
					}
				}

				override fun onError(error: VKError) {
					it.onError(IOException(error.errorMessage))
				}
			})
		}
	}

	// ============

	private fun parseFeedResponse(vkResponse: VKResponse): Feed? {
		try {
			val responseJson = vkResponse.json?.getJSONObject("response")
				?: return null

			val nextFrom = responseJson.optString("next_from")
				?: return null

			val profiles = parseProfiles(responseJson)
			val groups = parseGroups(responseJson)
			val posts = parsePosts(responseJson, profiles, groups)

			return Feed(posts, nextFrom)

		} catch (e: JSONException) {
			e.printStackTrace()
			return null
		}
	}

	private fun parseProfiles(responseJson: JSONObject): Map<Int, PostSource> {
		val profiles = ArrayList<PostSource>()

		val profilesJsonArray = responseJson.optJSONArray("profiles")
		if (profilesJsonArray != null) {

			// iterate through response.profiles
			for (i in 0 until profilesJsonArray.length()) {
				val profileJson = profilesJsonArray.optJSONObject(i) ?: continue

				// profile id is required
				val profileId = profileJson.optInt("id")
				if (profileId == 0) continue

				// get full name
				val firstName = profileJson.optString("first_name")
				val lastName = profileJson.optString("last_name")
				val fullName = StringBuilder()
				if (!firstName.isNullOrBlank()) {
					fullName.append(firstName)
				}
				if (!lastName.isNullOrBlank()) {
					if (fullName.isNotEmpty()) {
						fullName.append(' ')
					}
					fullName.append(lastName)
				}

				val screenName = profileJson.optString("screen_name")

				// parse photo urls
				val photoUrls = findPhotoUrls(profileJson)
				val photo = if (photoUrls != null) Photo(photoUrls) else null

				profiles.add(PostSource(profileId, fullName.toString(), screenName, photo))
			}

		}

		val profilesMap = HashMap<Int, PostSource>()
		profiles.forEach { profilesMap[it.id] = it }

		return profilesMap
	}

	private fun parseGroups(responseJson: JSONObject): Map<Int, PostSource> {
		val groups = ArrayList<PostSource>()

		val groupsJsonArray = responseJson.optJSONArray("groups")
		if (groupsJsonArray != null) {

			// iterate through response.groups
			for (i in 0 until groupsJsonArray.length()) {
				val groupJson = groupsJsonArray.optJSONObject(i) ?: continue

				// group id is required
				val groupId = groupJson.optInt("id")
				if (groupId == 0) continue

				val name = groupJson.optString("name")

				val screenName = groupJson.optString("screen_name")

				// parse photo urls
				val photoUrls = findPhotoUrls(groupJson)
				val photo = if (photoUrls != null) Photo(photoUrls) else null

				groups.add(PostSource(-groupId, name, screenName, photo))        // group.id must be negative
			}

		}

		val groupsMap = HashMap<Int, PostSource>()
		groups.forEach { groupsMap[it.id] = it }

		return groupsMap
	}

	private fun parsePosts(responseJson: JSONObject, profiles: Map<Int, PostSource>, groups: Map<Int, PostSource>): List<Post> {
		val posts = ArrayList<Post>()

		val itemsJsonArray = responseJson.optJSONArray("items")
		if (itemsJsonArray != null) {

			// iterate through response.items
			for (i in 0 until itemsJsonArray.length()) {
				val itemJson = itemsJsonArray.optJSONObject(i)
				if (itemJson == null || itemJson.optString("type") != "post") continue

				// postId is required
				val postId = itemJson.optInt("post_id")
				if (postId == 0) continue

				// parse optional fields
				val postSourceId = itemJson.optInt("source_id")    // profile/group id
				val postDate = itemJson.optLong("date")
				val postText = itemJson.optString("text")
				val postAttachments = ArrayList<Attachment>()

				// parse attachments list
				val attachmentsJsonArray = itemJson.optJSONArray("attachments")
				if (attachmentsJsonArray != null) {

					// iterate through response.items[].attachments
					for (j in 0 until attachmentsJsonArray.length()) {
						val attachmentJson = attachmentsJsonArray.optJSONObject(j) ?: continue
						val attachmentType = attachmentJson.optString("type")
						when (attachmentType) {
							"photo" -> {
								val photoJson = attachmentJson.getJSONObject("photo")
								val photoUrls = findPhotoUrls(photoJson)
								if (photoUrls != null) {
									postAttachments.add(Photo(photoUrls))
								}
							}
							"video" -> {
								val videoJson = attachmentJson.getJSONObject("video")
								val videoId = videoJson.optInt("id")
								val videoOwnerId = videoJson.optInt("owner_id")
								if (videoId != 0 && videoOwnerId != 0) {
									val title = videoJson.optString("title")
									val photoUrls = findPhotoUrls(videoJson)
									postAttachments.add(Video(videoId, videoOwnerId, title, photoUrls))
								}
							}
							"audio" -> {
								val audioJson = attachmentJson.getJSONObject("audio")
								val audioId = audioJson.optInt("id")
								val audioOwnerId = audioJson.optInt("owner_id")
								if (audioId != 0 && audioOwnerId != 0) {
									val artist = audioJson.optString("artist")
									val title = audioJson.optString("title")
									postAttachments.add(Audio(audioId, audioOwnerId, artist, title))
								}
							}
							"link" -> {
								val linkJson = attachmentJson.getJSONObject("link")
								val linkUrl = linkJson.optString("url")
								if (!linkUrl.isNullOrEmpty()) {
									postAttachments.add(Link(linkUrl))
								}
							}
						}
					}

				}

				// find post source
				val postSource = (if (postSourceId > 0) profiles[postSourceId] else groups[postSourceId])
					?: PostSource(postSourceId, null, null, null)

				posts.add(Post(postId, postDate, postText, postAttachments, postSource))
			}

		}

		return posts
	}

	private fun findPhotoUrls(json: JSONObject?): Map<String, String>? {
		val photoUrls = HashMap<String, String>()
		json?.keys()?.forEach {
			if (it.startsWith("photo_")) {
				val url = json.optString(it)
				if (url != null) {
					photoUrls[it] = url
				}
			}
		}
		return if (photoUrls.isNotEmpty()) photoUrls else null
	}

}