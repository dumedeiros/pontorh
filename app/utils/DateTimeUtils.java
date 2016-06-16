package utils;

import java.time.*;
import java.util.Date;

import static java.time.temporal.TemporalAdjusters.*;

public class DateTimeUtils {

    /**
     * Get a date that represents the start of a day (MidNight)
     *
     * @param date the date to get it's start
     * @return
     */
    public static Date getStartOfDay(Date date) {
        LocalDateTime ldt = dateToLocalDateTime(date);
        LocalDateTime startOfDay = ldt.with(LocalTime.MIN);
        return localDateTimeToDate(startOfDay);
    }

    /**
     * Get a date that represents the end of a day (23:59:5999)
     *
     * @param date the date to get it's end
     * @return
     */
    public static Date getEndOfDay(Date date) {
        LocalDateTime ldt = dateToLocalDateTime(date);
        LocalDateTime endOfDay = ldt.with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }


    /**
     * Get a date that represents the start of a week (Monday - 00:00)
     *
     * @param date any date in the week
     * @return
     */
    public static Date getStartOfWeek(Date date) {
        LocalDateTime ldt = dateToLocalDateTime(date);
        LocalDateTime startOfWeek = ldt.with(previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        return localDateTimeToDate(startOfWeek);
    }

    /**
     * Get a date that represents the end of a week (Sunday - 23:59:5999)
     *
     * @param date any date in the week
     * @return
     */
    public static Date getEndOfWeek(Date date) {
        LocalDateTime ldt = dateToLocalDateTime(date);
        LocalDateTime endOfWeek = ldt.with(nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
        return localDateTimeToDate(endOfWeek);
    }

    /**
     * Get a date that represents the start of a month (01/month - 00:00)
     *
     * @param date any date in the month
     * @return
     */
    public static Date getStartOfMonth(Date date) {
        LocalDateTime ldt = dateToLocalDateTime(date);
        LocalDateTime startOfMonth = ldt.with(firstDayOfMonth()).with(LocalTime.MIN);
        return localDateTimeToDate(startOfMonth);
    }

    /**
     * Get a date that represents the end of a month (28|29|30|31/month - 23:59:5999)
     *
     * @param date any date in the month
     * @return
     */
    public static Date getEndOfMonth(Date date) {
        LocalDateTime ldt = dateToLocalDateTime(date);
        LocalDateTime endOfMonth = ldt.with(lastDayOfMonth()).with(LocalTime.MAX);
        return localDateTimeToDate(endOfMonth);
    }

    /**
     * Convert a LocalDateTime (Java 8) to old fashioned Date
     *
     * @param d
     * @return
     */
    public static Date localDateTimeToDate(LocalDateTime d) {
        return Date.from(d.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert a old fashioned Date to a LocalDateTime (Java 8)
     *
     * @param d
     * @return
     */
    public static LocalDateTime dateToLocalDateTime(Date d) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(d.getTime()), ZoneId.systemDefault());
    }

    /**
     * Convert a old fashioned Date to a LocalDate (Java 8)
     *
     * @param d
     * @return
     */
    public static LocalDate dateToLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


}
