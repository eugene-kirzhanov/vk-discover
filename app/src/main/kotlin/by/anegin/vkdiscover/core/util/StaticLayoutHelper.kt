package by.anegin.vkdiscover.core.util

import android.os.Build
import android.text.*
import android.text.TextUtils.TruncateAt
import android.util.LruCache

object StaticLayoutHelper {

	fun obtain(text: CharSequence, paint: TextPaint, width: Int, maxLines: Int, ellipsize: TruncateAt): StaticLayout {
		val cacheKey = StaticLayoutCacheKey(text, paint, width, maxLines)
		val cachedStaticLayout = StaticLayoutCache[cacheKey]
		if (cachedStaticLayout != null) {
			return cachedStaticLayout
		} else {
			val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
					.setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
					.setAlignment(Layout.Alignment.ALIGN_NORMAL)
					.setLineSpacing(0f, 1f)
					.setIncludePad(false)

				if (maxLines > 0) {
					builder.setEllipsize(ellipsize)
						.setEllipsizedWidth(width)
						.setMaxLines(maxLines)
				}

				builder.build()

			} else {
				if (maxLines > 0) {
					StaticLayoutWithMaxLines().create(
						text, 0, text.length, paint,
						width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false,
						ellipsize, width, maxLines
					)
				} else {
					@Suppress("DEPRECATION")
					StaticLayout(
						text, 0, text.length, paint,
						width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false,
						ellipsize, width
					)
				}
			}
			StaticLayoutCache[cacheKey] = staticLayout
			return staticLayout
		}
	}

}

data class StaticLayoutCacheKey(
	val text: CharSequence,
	val paint: TextPaint,
	val width: Int,
	val maxLines: Int
)

object StaticLayoutCache {

	private const val MAX_SIZE = 50 // Max number of cached items
	private val cache = LruCache<StaticLayoutCacheKey, StaticLayout>(MAX_SIZE)

	operator fun set(key: StaticLayoutCacheKey, staticLayout: StaticLayout) {
		cache.put(key, staticLayout)
	}

	operator fun get(key: StaticLayoutCacheKey): StaticLayout? {
		return cache[key]
	}
}

/**
 * StaticLayout with maxLines support on pre-Marshmallow
 * Uses reflection to invoke hidden StaticLayout constructor with "maxLines" argument
 */
class StaticLayoutWithMaxLines {

	@Throws(IllegalStateException::class)
	@Synchronized
	fun create(
		source: CharSequence,
		bufstart: Int,
		bufend: Int,
		paint: TextPaint,
		outerWidth: Int,
		align: Layout.Alignment,
		spacingMult: Float,
		spacingAdd: Float,
		includePad: Boolean,
		ellipsize: TruncateAt,
		ellipsisWidth: Int,
		maxLines: Int
	): StaticLayout {

		try {

			val constructor = StaticLayout::class.java.getDeclaredConstructor(
				CharSequence::class.java,
				Integer::class.javaPrimitiveType,
				Integer::class.javaPrimitiveType,
				TextPaint::class.java,
				Integer::class.javaPrimitiveType,
				Layout.Alignment::class.java,
				TextDirectionHeuristic::class.java,
				Float::class.javaPrimitiveType,
				Float::class.javaPrimitiveType,
				Boolean::class.javaPrimitiveType,
				TruncateAt::class.java,
				Integer::class.javaPrimitiveType,
				Integer::class.javaPrimitiveType
			)
			constructor.isAccessible = true
			return constructor.newInstance(
				source,
				bufstart,
				bufend,
				paint,
				outerWidth,
				align,
				TextDirectionHeuristics.FIRSTSTRONG_LTR,
				spacingMult,
				spacingAdd,
				includePad,
				ellipsize,
				ellipsisWidth,
				maxLines
			)

		} catch (e: Exception) {
			throw IllegalStateException("Error creating StaticLayout with max lines: $e")
		}
	}

}