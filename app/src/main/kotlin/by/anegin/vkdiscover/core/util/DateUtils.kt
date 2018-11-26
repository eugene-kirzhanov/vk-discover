package by.anegin.vkdiscover.core.util

import android.content.Context
import by.anegin.vkdiscover.R
import java.util.*

object DateUtils {

	fun formatDateTime(context: Context, dateTimeMs: Long): String {
		if (dateTimeMs == 0L) return ""

		val dateTime = Calendar.getInstance()
		dateTime.timeInMillis = dateTimeMs * 1000

		val todayStart = Calendar.getInstance()
		todayStart.set(Calendar.HOUR_OF_DAY, 0)
		todayStart.set(Calendar.MINUTE, 0)
		todayStart.set(Calendar.SECOND, 0)
		todayStart.set(Calendar.MILLISECOND, 0)

		val dateString: String
		if (dateTimeMs > todayStart.timeInMillis) {

			// сегодня
			dateString = context.getString(R.string.today)

		} else {

			val yesterdayStart = Calendar.getInstance()
			yesterdayStart.add(Calendar.DAY_OF_MONTH, -1)
			yesterdayStart.set(Calendar.HOUR_OF_DAY, 0)
			yesterdayStart.set(Calendar.MINUTE, 0)
			yesterdayStart.set(Calendar.SECOND, 0)
			yesterdayStart.set(Calendar.MILLISECOND, 0)

			if (dateTimeMs > yesterdayStart.timeInMillis) {

				// вчера
				dateString = context.getString(R.string.yesterday)

			} else {

				val months = context.resources.getStringArray(R.array.months_short)

				val now = Calendar.getInstance()
				if (now.get(Calendar.YEAR) == dateTime.get(Calendar.YEAR)) {

					// 24 апр
					dateString = String.format(
						"%02d %s",
						dateTime.get(Calendar.DAY_OF_MONTH),
						months[dateTime.get(Calendar.MONTH)]
					)

				} else {

					// 24 апреля 2007
					dateString = String.format(
						"%02d %s %d",
						dateTime.get(Calendar.DAY_OF_MONTH),
						months[dateTime.get(Calendar.MONTH)],
						dateTime.get(Calendar.YEAR)
					)

				}

			}
		}

		val timeString = String.format(
			"%02d:%02d",
			dateTime.get(Calendar.HOUR_OF_DAY),
			dateTime.get(Calendar.MINUTE)
		)

		return context.getString(R.string.post_date_format, dateString, timeString)
	}

}