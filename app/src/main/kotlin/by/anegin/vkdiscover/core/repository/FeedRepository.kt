package by.anegin.vkdiscover.core.repository

import by.anegin.vkdiscover.core.model.Feed
import by.anegin.vkdiscover.core.model.Post
import io.reactivex.Completable
import io.reactivex.Single

interface FeedRepository {

	fun getRecommendedFeed(count: Int, startFrom: String? = null): Single<Feed>

	fun likePost(post: Post): Completable

	fun skipPost(post: Post): Completable

}