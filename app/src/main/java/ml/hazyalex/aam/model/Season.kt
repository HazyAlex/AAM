package ml.hazyalex.aam.model

import java.security.InvalidParameterException
import java.text.DateFormat.getDateInstance
import java.time.Month
import java.util.*

enum class Season {
    Winter, Spring, Summer, Fall;

    companion object {
        fun getCurrentSeason(): Season {
            val calendar = getDateInstance().calendar

            when (calendar.get(Calendar.MONTH)) {
                Month.DECEMBER.value, Month.JANUARY.value, Month.FEBRUARY.value -> {
                    return Winter
                }
                Month.MARCH.value, Month.MAY.value -> {
                    return Spring
                }
                Month.JUNE.value, Month.JULY.value, Month.AUGUST.value -> {
                    return Summer
                }
                Month.SEPTEMBER.value, Month.NOVEMBER.value -> {
                    return Fall
                }
            }

            throw InvalidParameterException("Invalid Month!")
        }
    }
}
