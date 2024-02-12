package tech.resilientgym.resilient

expect class PlatformDate() {
    fun todayAsString(): String
    fun nextDayAsString(currentDateString: String): String
    fun previousDayAsString(currentDateString: String): String
    fun formatDateString(dateString: String): String
}