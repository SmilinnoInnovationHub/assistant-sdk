package com.smilinno.smilinnolibrary.util

import android.text.format.DateFormat
import com.smilinno.smilinnolibrary.repository.MainRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


internal object TimeUtils {

    fun getCurrentHourAndMinute(): String {
        val rightNow = Calendar.getInstance()
        var currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY).toString()
        var currentMinute = rightNow.get(Calendar.MINUTE).toString()
        if (currentHourIn24Format.length == 1) {
            currentHourIn24Format = "0".plus(currentHourIn24Format)
        }
        if (currentMinute.length == 1) {
            currentMinute = "0".plus(currentMinute)
        }
        return currentMinute.plus(" : ").plus(currentHourIn24Format)
    }

    fun getHourAndMinute(timestamp: Long): String {
        val rightNow = Calendar.getInstance()
        rightNow.timeInMillis = timestamp
        var currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY).toString()
        var currentMinute = rightNow.get(Calendar.MINUTE).toString()
        if (currentHourIn24Format.length == 1) {
            currentHourIn24Format = "0".plus(currentHourIn24Format)
        }
        if (currentMinute.length == 1) {
            currentMinute = "0".plus(currentMinute)
        }
        return currentMinute.plus(" : ").plus(currentHourIn24Format)
    }

    fun getTimestampDiff(time: String): Long {
        val date = parseTime(time)
        if (date != null) {
            return date.time - System.currentTimeMillis()
        }
        return 0
    }

    fun parseTime(time: String): Date? {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return sdf.parse(time)
    }

    fun getServerTime(): Long {
        return System.currentTimeMillis() + MainRepository.serverTimeDiff
    }

    fun getRawServerTime(): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis() + MainRepository.serverTimeDiff
        return DateFormat.format("yyyy-MM-dd'T'HH:mm:ss", cal).toString()
    }

    fun notEqualDays(firstDay: String, secondDay: String): Boolean {
        parseTime(firstDay)?.let { firstTime ->
            parseTime(secondDay)?.let { secondTime ->
                val calendar = Calendar.getInstance()
                calendar.time = firstTime
                val firstDayOfWeek = calendar[Calendar.DAY_OF_MONTH]
                calendar.time = secondTime
                val secondDayOfWeek = calendar[Calendar.DAY_OF_MONTH]
                return firstDayOfWeek != secondDayOfWeek
            }
        }
        return true
    }
}