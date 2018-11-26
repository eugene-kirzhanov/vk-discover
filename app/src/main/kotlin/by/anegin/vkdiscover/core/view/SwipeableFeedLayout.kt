package by.anegin.vkdiscover.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.OverScroller
import by.anegin.vkdiscover.R

class SwipeableFeedLayout : FrameLayout {

	interface Listener {
		fun onSwipe(value: Float)
		fun onSwipeCanceled()
		fun onSwipedToRight()
		fun onSwipedToLeft()
	}

	// ===========

	companion object {
		private const val BACK_LAYOUT_INITIAL_SCALE = 0.7f
	}

	private var frontLayout: View? = null
	private var backLayout: View? = null

	private var touchslop = 0
	private var minFlingVelocity = 0
	private var maxFlingVelocity = 0

	private var velocityTracker: VelocityTracker? = null

	private var downX = 0f
	private var downPointerId: Int? = 0
	private var downTranslation = 0f

	private var isMoving = false

	private lateinit var scroller: OverScroller
	private var swipedToRight: Boolean? = null

	private var listener: Listener? = null

	private var swipeDisabled = false

	constructor(context: Context) : super(context) {
		init(context)
	}

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
		init(context, attrs)
	}

	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		init(context, attrs, defStyleAttr)
	}

	private fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
		val a = context.obtainStyledAttributes(attrs, R.styleable.SwipeableFeedLayout, defStyleAttr, 0)
		val itemLayoutId = a.getResourceId(R.styleable.SwipeableFeedLayout_item_layout, 0)
		a.recycle()

		if (itemLayoutId == 0)
			throw IllegalArgumentException("app:item_layout must be specified")

		val inflater = LayoutInflater.from(context)
		backLayout = inflater.inflate(itemLayoutId, this, false)
		frontLayout = inflater.inflate(itemLayoutId, this, false)

		clipChildren = false
		clipToPadding = false

		val viewConfig = ViewConfiguration.get(context)
		touchslop = viewConfig.scaledTouchSlop
		minFlingVelocity = viewConfig.scaledMinimumFlingVelocity
		maxFlingVelocity = viewConfig.scaledMaximumFlingVelocity

		scroller = OverScroller(context)

		val touchableView = View(context)
		touchableView.isFocusable = true
		touchableView.isClickable = true
		touchableView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

		addView(touchableView)
		addView(backLayout)
		addView(frontLayout)

		backLayout?.apply {
			scaleX = BACK_LAYOUT_INITIAL_SCALE
			scaleY = BACK_LAYOUT_INITIAL_SCALE
		}

	}

	override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
		if (childCount < 3) {
			super.addView(child, index, params)
		} else {
			throw UnsupportedOperationException("Adding childs allowed only through attributes")
		}
	}

	// ===========

	fun setListener(listener: Listener?) {
		this.listener = listener
	}

	fun setSwipeDisabled(disabled: Boolean) {
		this.swipeDisabled = disabled
	}

	fun getFrontLayout() = frontLayout

	fun getBackLayout() = backLayout

	fun swipeToLeft() {
		downPointerId = null
		isMoving = false
		scroller.forceFinished(true)

		val currTranslation = frontLayout?.translationX?.toInt() ?: 0
		swipedToRight = false
		scroller.fling(currTranslation, 0, -maxFlingVelocity / 2, 0, -getSwipeOutMaxTranslation().toInt(), 0, 0, 0)
		postInvalidateOnAnimation()
	}

	fun swipeToRight() {
		downPointerId = null
		isMoving = false
		scroller.forceFinished(true)

		val currTranslation = frontLayout?.translationX?.toInt() ?: 0
		swipedToRight = true
		scroller.fling(currTranslation, 0, maxFlingVelocity / 2, 0, 0, getSwipeOutMaxTranslation().toInt(), 0, 0)
		postInvalidateOnAnimation()
	}

	// ===========

	@SuppressLint("Recycle")
	override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
		if (frontLayout == null || swipeDisabled) return false

		val action = ev.actionMasked
		when (action) {
			MotionEvent.ACTION_DOWN -> {
				downPointerId = ev.getPointerId(0)
				downX = ev.x
				isMoving = false

				velocityTracker?.clear()
				velocityTracker = velocityTracker ?: VelocityTracker.obtain()
				velocityTracker?.addMovement(ev)

				swipedToRight = null
				downTranslation = frontLayout?.translationX ?: 0f

				scroller.forceFinished(true)

				return false
			}
			MotionEvent.ACTION_MOVE -> {
				if (!isMoving && ev.getPointerId(0) == downPointerId) {
					val dx = ev.x - downX
					if (Math.abs(dx) > touchslop) {
						isMoving = true
						parent?.requestDisallowInterceptTouchEvent(true)
						return true
					}
				}
				return false
			}
			MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
				cancelMove()
				return false
			}
			else -> {
				return super.onInterceptTouchEvent(ev)
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(ev: MotionEvent): Boolean {
		velocityTracker?.addMovement(ev)
		when (ev.actionMasked) {
			MotionEvent.ACTION_MOVE -> {
				if (isMoving) {
					moveLayouts(downTranslation + (ev.x - downX))
					return true
				}
			}
			MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
				cancelMove()
				return false
			}
		}
		return false
	}

	private fun moveLayouts(translation: Float) {
		// front
		val frontScale = 1f + 0.2f * Math.abs(translation) / width
		var opacity = Math.abs(translation) / width
		if (opacity > 1f) opacity = 1f
		frontLayout?.apply {
			rotation = 16f * translation / width
			scaleX = frontScale
			scaleY = frontScale
			translationX = translation
			alpha = 1f - opacity * 0.3f
		}

		// back
		var backScale = BACK_LAYOUT_INITIAL_SCALE + (1 - BACK_LAYOUT_INITIAL_SCALE) * Math.abs(translation) / width
		if (backScale > 1f) backScale = 1f
		backLayout?.apply {
			scaleX = backScale
			scaleY = backScale
		}

		var value = translation / width
		if (value < -1f) value = -1f
		if (value > 1f) value = 1f
		listener?.onSwipe(value)
	}

	private fun cancelMove() {
		downPointerId = null
		isMoving = false

		velocityTracker?.computeCurrentVelocity(1000)
		val xVelocity = velocityTracker?.xVelocity ?: 0f
		velocityTracker?.recycle()
		velocityTracker = null

		scroller.forceFinished(true)

		val currTranslation = frontLayout?.translationX ?: 0f
		if (currTranslation != 0f) {
			if (Math.abs(xVelocity) < minFlingVelocity) {

				if (Math.abs(currTranslation) < width / 3f) {
					// return frontlayout back to initial position
					swipedToRight = null
					scroller.startScroll(currTranslation.toInt(), 0, -currTranslation.toInt(), 0)
				} else {
					// move frontlayout outside of screen
					swipedToRight = currTranslation > 0f
					scroller.startScroll(currTranslation.toInt(), 0, (Math.signum(currTranslation) * getSwipeOutMaxTranslation()).toInt(), 0)
				}

			} else {

				var velocity = xVelocity
				if (Math.abs(velocity) > maxFlingVelocity) {
					velocity = Math.signum(velocity) * maxFlingVelocity.toFloat() * 2f
				} else if (Math.abs(velocity) < maxFlingVelocity / 5f) {
					velocity = Math.signum(velocity) * maxFlingVelocity / 1.5f
				}

				var minX = 0f
				var maxX = 0f
				if (currTranslation > 0) {
					maxX = getSwipeOutMaxTranslation()
				} else {
					minX = -getSwipeOutMaxTranslation()
				}
				swipedToRight = currTranslation > 0f
				scroller.fling(currTranslation.toInt(), 0, velocity.toInt(), 0, minX.toInt(), maxX.toInt(), 0, 0)

			}
		}

		postInvalidateOnAnimation()
	}

	private fun getSwipeOutMaxTranslation(): Float {
		return 1.3f * width
	}

	override fun computeScroll() {
		super.computeScroll()
		if (scroller.computeScrollOffset()) {
			moveLayouts(scroller.currX.toFloat())
			postInvalidateOnAnimation()
		} else {
			if (scroller.currX == 0) {
				// front layout returned back to initial position
				listener?.onSwipeCanceled()
			} else {
				// front layout was swiped out
				if (swipedToRight == true) {
					swipedToRight = null
					swapLayouts()
					listener?.onSwipedToRight()
				} else if (swipedToRight == false) {
					swipedToRight = null
					swapLayouts()
					listener?.onSwipedToLeft()
				}
			}
		}
	}

	private fun swapLayouts() {
		val prevFrontLayout = this.frontLayout
		if (prevFrontLayout != null) {
			removeView(prevFrontLayout)
			addView(prevFrontLayout, 1)    // after touchView
			prevFrontLayout.apply {
				translationX = 0f
				rotation = 0f
				scaleX = 1f
				scaleY = 1f
			}
		}
		this.frontLayout = backLayout
		this.backLayout = prevFrontLayout

		backLayout?.apply {
			scaleX = BACK_LAYOUT_INITIAL_SCALE
			scaleY = BACK_LAYOUT_INITIAL_SCALE
		}

		prevFrontLayout?.apply {
			alpha = 0f
			animate()
				.alpha(1f)
				.setDuration(300)
				.start()
		}

		invalidate()
	}

}