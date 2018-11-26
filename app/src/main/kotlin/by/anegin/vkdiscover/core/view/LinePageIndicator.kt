package by.anegin.vkdiscover.core.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import by.anegin.vkdiscover.R
import kotlin.math.ceil

/**
 * Modified LinePagerIndicator from Jake Wharton's ViewPagerIndicator library
 * https://github.com/JakeWharton/ViewPagerIndicator
 *
 * Removed "centered" and "lineWidth" attributes and added lines autosizing to fit view width
 *
 */
class LinePageIndicator : View, ViewPager.OnPageChangeListener {

	interface Listener {
		fun onPageChanged(position: Int)
	}

	private val mPaintUnselected = Paint(Paint.ANTI_ALIAS_FLAG)
	private val mPaintSelected = Paint(Paint.ANTI_ALIAS_FLAG)

	private var mViewPager: ViewPager? = null

	private var mCurrentPage = 0
	private var mGapWidth = 0f

	private var listener: Listener? = null

	constructor(context: Context) : this(context, null)

	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		if (isInEditMode) return

		val a = context.obtainStyledAttributes(attrs, R.styleable.LinePageIndicator, defStyleAttr, 0)

		mGapWidth = a.getDimension(R.styleable.LinePageIndicator_gapWidth, 0f)

		val strokeWidth = a.getDimension(R.styleable.LinePageIndicator_strokeWidth, 0f)
		mPaintSelected.strokeWidth = strokeWidth
		mPaintUnselected.strokeWidth = strokeWidth

		mPaintUnselected.color = a.getColor(R.styleable.LinePageIndicator_unselectedColor, Color.TRANSPARENT)
		mPaintSelected.color = a.getColor(R.styleable.LinePageIndicator_selectedColor, Color.TRANSPARENT)

		a.recycle()
	}

	fun setViewPager(view: ViewPager?) {
		if (mViewPager === view) return

		if (mViewPager != null) {
			//Clear us from the old pager.
			mViewPager?.removeOnPageChangeListener(this)
		}
		mViewPager = view
		mViewPager?.addOnPageChangeListener(this)
		invalidate()
	}

	fun setCurrentItem(item: Int) {
		if (mViewPager == null) {
			throw IllegalStateException("ViewPager has not been bound.")
		}
		mViewPager?.currentItem = item
		mCurrentPage = item
		invalidate()
	}

	fun getCurrentItem() = mCurrentPage

	fun setListener(listener: Listener?) {
		this.listener = listener
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		val count = mViewPager?.adapter?.count ?: 0
		if (count == 0) return

		if (mCurrentPage >= count) {
			mViewPager?.currentItem = 0
			mCurrentPage = 0
			return
		}

		val lineWidth = (width - paddingLeft - paddingRight - mGapWidth * (count - 1)) / count
		val cy = paddingTop + (height - paddingTop - paddingBottom) / 2f

		var dx = paddingLeft.toFloat()
		for (i in 0 until count) {
			canvas.drawLine(dx, cy, dx + lineWidth, cy, if (i == mCurrentPage) mPaintSelected else mPaintUnselected)
			dx += lineWidth + mGapWidth
		}
	}

	override fun onPageScrollStateChanged(state: Int) {}

	override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

	override fun onPageSelected(position: Int) {
		mCurrentPage = position
		listener?.onPageChanged(position)
		invalidate()
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val width = MeasureSpec.getSize(widthMeasureSpec)
		setMeasuredDimension(width, measureHeight(heightMeasureSpec))
	}

	/**
	 * Determines the height of this view
	 *
	 * @param measureSpec
	 * A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private fun measureHeight(measureSpec: Int): Int {
		var result: Float
		val specMode = View.MeasureSpec.getMode(measureSpec)
		val specSize = View.MeasureSpec.getSize(measureSpec)

		if (specMode == View.MeasureSpec.EXACTLY) {
			//We were told how big to be
			result = specSize.toFloat()
		} else {
			//Measure the height
			result = mPaintSelected.strokeWidth + paddingTop + paddingBottom
			//Respect AT_MOST value if that was what is called for by measureSpec
			if (specMode == View.MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize.toFloat())
			}
		}
		return ceil(result).toInt()
	}

	public override fun onRestoreInstanceState(state: Parcelable) {
		val savedState = state as SavedState
		super.onRestoreInstanceState(savedState.superState)
		mCurrentPage = savedState.currentPage
		requestLayout()
	}

	public override fun onSaveInstanceState(): Parcelable? {
		val superState = super.onSaveInstanceState()
		val savedState = SavedState(superState)
		savedState.currentPage = mCurrentPage
		return savedState
	}

	internal class SavedState : View.BaseSavedState {

		var currentPage: Int = 0

		constructor(superState: Parcelable?) : super(superState)

		private constructor(src: Parcel) : super(src) {
			currentPage = src.readInt()
		}

		override fun writeToParcel(dest: Parcel, flags: Int) {
			super.writeToParcel(dest, flags)
			dest.writeInt(currentPage)
		}

		companion object {

			@Suppress("unused")
			@JvmField
			val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
				override fun createFromParcel(src: Parcel): SavedState {
					return SavedState(src)
				}

				override fun newArray(size: Int): Array<SavedState?> {
					return arrayOfNulls(size)
				}
			}

		}

	}
}