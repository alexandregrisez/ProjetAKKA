// ------------------------------------------------- Imports

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

import java.time.temporal.ChronoUnit
import cats.instances.boolean
import java.time.LocalDate

// ------------------------------------------------- Calendar object used to manage dates

object Calendar {
  
    // Les dates sont au format jj-mm-aaaa

    // Date du jour
    def now(): String = {
        val currentDateTime: LocalDate = LocalDate.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        currentDateTime.format(formatter)
    }

    def getDay(dateStr: String): Int = {
        parseDate(dateStr).getDayOfMonth
    }

    // Mois de 1 à 12
    def getMonth(dateStr: String): Int = {
        parseDate(dateStr).getMonthValue
    }

    def getYear(dateStr: String): Int = {
        parseDate(dateStr).getYear
    }

    def chronological(startDate:String, endDate:String):Boolean = {
        val start = parseDate(startDate) 
        val end = parseDate(endDate)
        start.isBefore(end)
    }

    def daysBetween(startDate:String, endDate:String):Double = {
        val start = parseDate(startDate) 
        val end = parseDate(endDate)
        ChronoUnit.DAYS.between(start, end).toDouble
    }

    def parseDate(dateStr: String): LocalDate = {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        try {
            LocalDate.parse(dateStr, formatter)
        } catch {
            case e: DateTimeParseException => 
                println(s"Invalid date format: $dateStr. Expected format: dd-MM-yyyy")
                throw e
        }
    }
}
