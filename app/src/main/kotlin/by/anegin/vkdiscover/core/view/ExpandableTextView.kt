package by.anegin.vkdiscover.core.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.content.res.AppCompatResources
import by.anegin.vkdiscover.R
import by.anegin.vkdiscover.core.util.StaticLayoutHelper

class ExpandableTextView : View {

	interface Listener {
		fun onAnimationStarted(expanding: Boolean)
		fun onStateChanged(expanded: Boolean)
	}

	companion object {
		private const val ANIM_DURATION = 250L
	}

	enum class State {
		IDLE, COLLAPSING, EXPANDING
	}

	private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	private val morePaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

	private val p = TextPaint(Paint.ANTI_ALIAS_FLAG)

	private var collapsedTextStaticLayout: StaticLayout? = null
	private var expandedTextStaticLayout: StaticLayout? = null
	private var moreTextStaticLayout: StaticLayout? = null
	private var moreCollapseTextStaticLayout: StaticLayout? = null

	private var animState: State = State.IDLE
	private var expandRatio = 0f
	private var expandAnimation: Animator? = null

	private var text: String = ""
	private var maxLines = 0

	private var moreText: String = ""
	private var moreTextCollapse: String = ""
	private var moreIcon: Drawable? = null
	private var moreIconPadding = 0f
	private var moreSpacing = 0f

	private var listener: Listener? = null

	constructor(context: Context) : super(context) {
		init(null, 0)
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		init(attrs, 0)
	}

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
		init(attrs, defStyle)
	}

	private fun init(attrs: AttributeSet?, defStyle: Int) {
		val a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView, defStyle, 0)

		val isExpanded = a.getBoolean(R.styleable.ExpandableTextView_is_expanded, false)

		paint.textSize = a.getDimension(R.styleable.ExpandableTextView_text_size, 0f)
		paint.color = a.getColor(R.styleable.ExpandableTextView_text_color, Color.TRANSPARENT)

		text = a.getString(R.styleable.ExpandableTextView_text) ?: ""
		maxLines = a.getInteger(R.styleable.ExpandableTextView_max_lines, 0)

		moreText = a.getString(R.styleable.ExpandableTextView_more_text) ?: ""
		moreTextCollapse = a.getString(R.styleable.ExpandableTextView_more_text_collapse) ?: ""
		morePaint.textSize = a.getDimension(R.styleable.ExpandableTextView_more_text_size, 0f)
		morePaint.color = a.getColor(R.styleable.ExpandableTextView_more_text_color, Color.TRANSPARENT)

		val moreIconResId = a.getResourceId(R.styleable.ExpandableTextView_more_icon, 0)
		moreIcon = if (moreIconResId != 0) AppCompatResources.getDrawable(context, moreIconResId) else null

		moreIconPadding = a.getDimension(R.styleable.ExpandableTextView_more_icon_padding, 0f)
		moreSpacing = a.getDimension(R.styleable.ExpandableTextView_more_spacing, 0f)

		a.recycle()

		expandRatio = if (isExpanded) 1f else 0f
		animState = State.IDLE

		p.color = Color.RED
		p.style = Paint.Style.STROKE
		p.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)

		// text
		measureText(measuredWidth - paddingLeft - paddingRight, text)
		val collapsedTextHeight = collapsedTextStaticLayout?.height ?: 0
		val expandedTextHeight = expandedTextStaticLayout?.height ?: 0
		val textHeight = collapsedTextHeight + (expandedTextHeight - collapsedTextHeight) * expandRatio

		// more text and icon
		var moreHeight = 0f
		if (expandedTextHeight > collapsedTextHeight) {
			val moreTextHeight = if (expandRatio == 0f) {
				moreTextStaticLayout?.height ?: 0
			} else {
				moreCollapseTextStaticLayout?.height ?: 0
			}
			val moreIconHeight = moreIcon?.intrinsicHeight ?: 0
			moreHeight = Math.max(moreTextHeight, moreIconHeight).toFloat()
			if (moreHeight > 0) moreHeight += moreSpacing
		}

		val minHeight = paddingTop + paddingBottom + textHeight + moreHeight

		val height = MeasureSpec.getSize(heightMeasureSpec)
		val measuredHeight = when (MeasureSpec.getMode(heightMeasureSpec)) {
			MeasureSpec.AT_MOST -> Math.min(height, minHeight.toInt())
			MeasureSpec.EXACTLY -> height
			MeasureSpec.UNSPECIFIED -> minHeight.toInt()
			else -> height
		}

		setMeasuredDimension(measuredWidth, measuredHeight)
	}

	private fun measureText(width: Int, text: String?) {
		if (!text.isNullOrEmpty() && width > 0) {
			collapsedTextStaticLayout =
				StaticLayoutHelper.obtain(text, paint, width, maxLines, TextUtils.TruncateAt.END)
			expandedTextStaticLayout = StaticLayoutHelper.obtain(text, paint, width, 0, TextUtils.TruncateAt.END)

			if (isExpandable()) {
				val moreText = this.moreText
				moreTextStaticLayout = if (!moreText.isEmpty()) {
					val moreTextWidth = width - (moreIcon?.intrinsicWidth ?: 0) - moreIconPadding
					StaticLayoutHelper.obtain(moreText, morePaint, moreTextWidth.toInt(), 1, TextUtils.TruncateAt.END)
				} else {
					null
				}
				val moreTextCollapse = this.moreTextCollapse
				moreCollapseTextStaticLayout = if (!moreTextCollapse.isEmpty()) {
					val moreTextCollapseWidth = width - (moreIcon?.intrinsicWidth ?: 0) - moreIconPadding
					StaticLayoutHelper.obtain(
						moreTextCollapse,
						morePaint,
						moreTextCollapseWidth.toInt(),
						1,
						TextUtils.TruncateAt.END
					)
				} else {
					null
				}
			}

		} else {
			collapsedTextStaticLayout = null
			expandedTextStaticLayout = null
			moreTextStaticLayout = null
		}
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		if (width == 0 || height == 0) return

		val left = paddingLeft.toFloat()
		val top = paddingTop.toFloat()
		val right = (width - paddingRight).toFloat()
		val bottom = (height - paddingBottom).toFloat()

		var moreHeight = 0f
		val moreTextStaticLayout = this.moreTextStaticLayout
		val moreCollapseTextStaticLayout = this.moreCollapseTextStaticLayout
		val moreStaticLayout = if (expandRatio == 0f && moreTextStaticLayout != null) {
			moreTextStaticLayout
		} else if (expandRatio > 0f && moreCollapseTextStaticLayout != null) {
			moreCollapseTextStaticLayout
		} else {
			null
		}
		val moreIcon = this.moreIcon
		if ((moreStaticLayout != null || moreIcon != null) && isExpandable()) {

			val moreTextHeight = moreStaticLayout?.height ?: 0
			val moreTextWidth = moreStaticLayout?.getLineWidth(0) ?: 0f

			val moreIconHeight = moreIcon?.intrinsicHeight ?: 0
			val moreIconWidth = moreIcon?.intrinsicWidth ?: 0

			moreHeight = Math.max(moreTextHeight, moreIconHeight).toFloat()
			val moreWidth = moreTextWidth + moreIconPadding + moreIconWidth
			val moreTop = bottom - moreHeight

			canvas.save()
			canvas.clipRect(left, moreTop, right, bottom)

			val tx = left + (right - left - moreWidth) / 2f
			val ty = moreTop + (moreHeight - moreTextHeight) / 2f

			canvas.translate(tx, ty)
			moreStaticLayout?.draw(canvas)
			canvas.translate(-tx, -ty)

			val icx = tx + moreTextWidth + moreIconPadding
			val icy = moreTop + (moreHeight - moreIconHeight) / 2f

			canvas.translate(icx, icy)
			canvas.rotate(expandRatio * 180f, moreIconWidth / 2f, moreIconHeight / 2f)
			moreIcon?.setBounds(0, 0, moreIconWidth, moreIconHeight)
			moreIcon?.draw(canvas)

			canvas.restore()

			moreHeight += moreSpacing
		}

		val text = this.text
		if (text.isNotEmpty() && bottom - top - moreHeight > 0) {

			canvas.save()
			canvas.clipRect(left, top, right, bottom - moreHeight)
			canvas.translate(left, top)

			if (expandRatio == 0f) {
				collapsedTextStaticLayout?.draw(canvas)
			} else {
				expandedTextStaticLayout?.draw(canvas)
			}

			canvas.restore()
		}
	}

	// =====================

	fun setListener(listener: Listener?) {
		this.listener = listener
	}

	fun setExpanded(expand: Boolean, animate: Boolean) {
		if (animate) {
			if (expand && expandRatio < 1f) {
				expand()
			} else if (!expand && expandRatio > 0f) {
				collapse()
			}
		} else {
			if (animState != State.IDLE) cancelAnimation()
			animState = State.IDLE
			if (expand && expandRatio < 1f && isExpandable()) {
				updateExpandRatio(1f)
				listener?.onStateChanged(true)
			} else if (!expand && expandRatio > 0f) {
				updateExpandRatio(0f)
				listener?.onStateChanged(false)
			}
		}
	}

	fun setText(text: String?) {
		this.text = text ?: ""
		requestLayout()
		postInvalidate()    // not invalidate(), because sometime text was not updated
	}

	fun toggle() {
		if (expandRatio < 1f) {
			expand()
		} else if (expandRatio > 0f) {
			collapse()
		}
	}

	// =====================

	private fun getAnimDurationDependingOnLinesCount(): Long {
		val linesCount = expandedTextStaticLayout?.lineCount ?: 0
		return if (linesCount > 30) 5L * linesCount else ANIM_DURATION
	}

	private fun isExpandable() = (expandedTextStaticLayout?.height ?: 0) > (collapsedTextStaticLayout?.height ?: 0)

	private fun expand() {
		if (animState == State.EXPANDING || expandRatio == 1f || !isExpandable()) return

		if (animState == State.COLLAPSING) cancelAnimation()
		animState = State.EXPANDING

		val duration = getAnimDurationDependingOnLinesCount() * (1f - expandRatio)
		executeAnimation(expandRatio, 1f, duration.toLong())

		listener?.onAnimationStarted(true)
	}

	private fun collapse() {
		if (animState == State.COLLAPSING || expandRatio == 0f) return

		if (animState == State.EXPANDING) cancelAnimation()
		animState = State.COLLAPSING

		val duration = getAnimDurationDependingOnLinesCount() * expandRatio
		executeAnimation(expandRatio, 0f, duration.toLong())

		listener?.onAnimationStarted(false)
	}

	private fun executeAnimation(fromExpandRatio: Float, toExpandRatio: Float, duration: Long) {
		val anim = ValueAnimator.ofFloat(fromExpandRatio, toExpandRatio)
		anim.interpolator = DecelerateInterpolator()
		anim.duration = duration
		anim.addUpdateListener {
			updateExpandRatio(it.animatedValue as Float)
		}
		anim.addListener(object : Animator.AnimatorListener {
			override fun onAnimationEnd(animation: Animator?) {
				updateExpandRatio(toExpandRatio)
				animState = State.IDLE

				listener?.onStateChanged(toExpandRatio == 1f)
			}

			override fun onAnimationRepeat(animation: Animator?) {
			}

			override fun onAnimationCancel(animation: Animator?) {
			}

			override fun onAnimationStart(animation: Animator?) {
			}
		})
		expandAnimation = anim
		anim.start()
	}

	private fun cancelAnimation() {
		expandAnimation?.cancel()
		expandAnimation = null
	}

	private fun updateExpandRatio(ratio: Float) {
		expandRatio = ratio
		requestLayout()
		invalidate()
	}

}