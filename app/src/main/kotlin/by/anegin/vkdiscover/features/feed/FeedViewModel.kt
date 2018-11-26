package by.anegin.vkdiscover.features.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import by.anegin.vkdiscover.core.model.Feed
import by.anegin.vkdiscover.core.model.Post
import by.anegin.vkdiscover.core.repository.FeedRepository
import by.anegin.vkdiscover.core.util.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FeedViewModel
@Inject
constructor(private val feedRepository: FeedRepository) : ViewModel() {

	companion object {
		private const val POSTS_IN_REQUEST = 15
		private const val POSTS_PRELOAD_TRESHOLD = 10
	}

	private val disposables = CompositeDisposable()

	private var nextFrom: String? = null

	private val posts = MutableLiveData<List<Post>>()

	private val _frontPost = MediatorLiveData<Post>()
	val frontPost: LiveData<Post>
		get() = _frontPost

	private val _backPost = MediatorLiveData<Post>()
	val backPost: LiveData<Post>
		get() = _backPost

	private val _inProgress = MediatorLiveData<Boolean>()
	val inProgress: LiveData<Boolean>
		get() = _inProgress

	private val _postActionErrorEvent = SingleLiveEvent<Boolean>()
	val postActionError: LiveData<Boolean>
		get() = _postActionErrorEvent

	private val _feedRequestErrorEvent = SingleLiveEvent<Any>()
	val feedRequestError: LiveData<Any>
		get() = _feedRequestErrorEvent

	init {
		_frontPost.addSource(posts) {
			if (it.isNotEmpty() && _frontPost.value?.id != it[0].id) {
				_frontPost.value = it[0]
			}
		}
		_backPost.addSource(posts) {
			if (it.size > 1 && _backPost.value?.id != it[1].id) {
				_backPost.value = it[1]
			}
		}

		_inProgress.value = true
		loadFeed()
	}

	override fun onCleared() {
		disposables.dispose()
		super.onCleared()
	}

	fun retryLoadFeed() {
		_inProgress.value = true
		loadFeed()
	}

	private fun loadFeed() {
		disposables.add(
			feedRepository.getRecommendedFeed(POSTS_IN_REQUEST, nextFrom)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
					{ onFeedLoaded(it) },
					{ onFeedLoadError(it) }
				)
		)
	}

	private fun onFeedLoaded(loadedFeed: Feed) {
		nextFrom = loadedFeed.nextFrom

		val postsList = posts.value?.toMutableList() ?: ArrayList()
		postsList.addAll(loadedFeed.posts)
		posts.value = postsList

		if (_inProgress.value == true) {
			_inProgress.value = false
		}
	}

	private fun onFeedLoadError(t: Throwable) {
		val postsCount = posts.value?.size ?: 0
		if (postsCount == 0) {
			_feedRequestErrorEvent.call()
		} else {
			t.printStackTrace()
		}
	}

	fun likeFirstPost() {
		requestFirstPostAction(true)
	}

	fun skipFirstPost() {
		requestFirstPostAction(false)
	}

	private fun requestFirstPostAction(liked: Boolean) {
		// find first post
		val post = posts.value?.firstOrNull() ?: return

		// send request to like/skip post
		val completable = if (liked) {
			feedRepository.likePost(post)
		} else {
			feedRepository.skipPost(post)
		}
		disposables.add(
			completable
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
					{ onPostActionSuccess(post.id, liked) },
					{ onPostActionError(it, liked) }
				)
		)

		// remove post from list and check if we need to preload posts
		val postsList = posts.value?.toMutableList() ?: ArrayList()
		if (postsList.isNotEmpty()) {
			postsList.remove(post)
			posts.value = postsList
		}
		if (postsList.size < POSTS_PRELOAD_TRESHOLD) {
			loadFeed()
		}
	}

	private fun onPostActionSuccess(postId: Int, liked: Boolean) {
		// nothing to do
		Log.v("VkDiscover", "Post $postId " + (if (liked) "liked" else "skipped"))
	}

	private fun onPostActionError(t: Throwable, liked: Boolean) {
		t.printStackTrace()
		_postActionErrorEvent.value = liked
	}

}