package by.anegin.vkdiscover.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager : ViewPager {

	private var swipeDisabled = false

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

	override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
		return if (swipeDisabled) false else super.onInterceptTouchEvent(ev)
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(ev: MotionEvent): Boolean {
		return if (swipeDisabled) false else super.onInterceptTouchEvent(ev)
	}

}