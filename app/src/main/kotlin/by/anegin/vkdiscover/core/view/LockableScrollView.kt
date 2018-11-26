package by.anegin.vkdiscover.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

class LockableScrollView : NestedScrollView {

	private var isScrollEnabled = false        // scroll locked by default

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet?, defStyleAttrs: Int) : super(context, attrs, defStyleAttrs)

	fun setScrollEnabled(enabled: Boolean) {
		this.isScrollEnabled = enabled
	}

	override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
		return if (isScrollEnabled) {
			super.onInterceptTouchEvent(ev)
		} else {
			false
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(ev: MotionEvent): Boolean {
		return if (isScrollEnabled) {
			super.onTouchEvent(ev)
		} else {
			false
		}
	}

}