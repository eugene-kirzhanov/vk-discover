package by.anegin.vkdiscover.core.util

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class VkGLideModule : AppGlideModule() {

	override fun applyOptions(context: Context, builder: GlideBuilder) {
		builder
			.setMemoryCache(LruResourceCache(1024 * 1024 * 20))
			.setBitmapPool(LruBitmapPool(1024 * 1024 * 30))
			.setDiskCache(InternalCacheDiskCacheFactory(context, 1024 * 1024 * 100))
			.setDefaultRequestOptions(
				RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
			)
	}

}