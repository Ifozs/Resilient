package tech.resilientgym.resilient

import java.time.LocalDate
import java.time.format.DateTimeFormatter
actual class PlatformDate actual constructor(){

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    actual fun todayAsString(): String = LocalDate.now().format(formatter)

    actual fun nextDayAsString(currentDateString: String): String {
        val currentDate = LocalDate.parse(currentDateString, formatter)
        return currentDate.plusDays(1).format(formatter)
    }

    actual fun previousDayAsString(currentDateString: String): String {
        val currentDate = LocalDate.parse(currentDateString, formatter)
        return currentDate.minusDays(1).format(formatter)
    }

    actual fun formatDateString(dateString: String): String {
        val date = LocalDate.parse(dateString, formatter)
        return date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    }
}