package helper;

import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class DateManipulation {

    public static boolean isYesterday(Date d) {
        return DateUtils.isToday(d.getTime() + DateUtils.DAY_IN_MILLIS);
    }

    public static boolean isToday(Date d) {
        return DateUtils.isToday(d.getTime());
    }

    public static boolean isTomorrow(Date d) {
        return DateUtils.isToday(d.getTime() - DateUtils.DAY_IN_MILLIS);
    }

    public static String getWeekDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        if(dateWithinWeek(d)) {
            switch (c.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    return "Lunedì";
                case Calendar.TUESDAY:
                    return "Martedì";
                case Calendar.WEDNESDAY:
                    return "Mercoledì";
                case Calendar.THURSDAY:
                    return "Giovedì";
                case Calendar.FRIDAY:
                    return "Venerdì";
                case Calendar.SATURDAY:
                    return "Sabato";
                case Calendar.SUNDAY:
                    return "Domenica";
                default:
                    return null;
            }
        }
        else
            return null;
    }

    public static boolean dateWithinWeek(Date d) {
        Calendar c = Calendar.getInstance();

        c.setTime(c.getTime());
        c.add(Calendar.DATE,7);

        return c.getTime().compareTo(d) > 0;
    }
}
