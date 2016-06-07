package extensions;

import play.templates.JavaExtensions;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeExtension extends JavaExtensions {

    /**
     * Default time format: 10:30, p. ex.
     */
    private static final String DEFAULT_TIME_FORMAT = "%02d:%02d";

    /**
     * Get the name of the day
     * Used in toolTip of dates on tables in view
     *
     * @param datetime the date to get it's name
     * @return the text with the name of the day
     */
    public static String dayalize(LocalDate datetime) {
        String aux = datetime == null ? "" : datetime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        return (aux.contains("-")) ? aux.substring(0, aux.indexOf('-')) : aux;
    }


    /**
     * Given a number (milliseconds), return its representation in time
     * P. ex.: 1120931 milliseconds = 09:hh
     *
     * @param t the number of milliseconds
     * @return it's representation in time
     */
    public static String formatime(Long t) {
        return formatime(t, DEFAULT_TIME_FORMAT);
    }

    /**
     * Given a number (milliseconds), return its representation in time in the specified format
     *
     * @param l      the number of milliseconds
     * @param format the custom format
     * @return
     */
    public static String formatime(Long l, String format) {
        return String.format(format,
                TimeUnit.MILLISECONDS.toHours(l),
                TimeUnit.MILLISECONDS.toMinutes(l) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(l)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(l) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)));
    }
}