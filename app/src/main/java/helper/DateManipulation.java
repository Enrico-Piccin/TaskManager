package helper;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateManipulation {

    // Controllo se due date coincidono
    public static boolean areSameDate(Date d1, Date d2) {
        return new SimpleDateFormat("yyyyMMdd").format(d1).equals(new SimpleDateFormat("yyyyMMdd").format(d2));
    }

    // Verifica se la data passata come parametro coincide con il giorno precedente a quello corrente
    public static boolean isYesterday(Date d) {
        return DateUtils.isToday(d.getTime() + DateUtils.DAY_IN_MILLIS);
    }

    // Verifica se la data passata come parametro coincide con il giorno corrente
    public static boolean isToday(Date d) {
        return DateUtils.isToday(d.getTime());
    }

    // Verifica se la data passata come parametro coincide con il giorno successivo a quello corrente
    public static boolean isTomorrow(Date d) {
        return DateUtils.isToday(d.getTime() - DateUtils.DAY_IN_MILLIS);
    }

    // Restituisce il nome del giorno della settimana
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

    // Verifica se la data passata come parametro si trova all'interno del range di sette giorni rispetto alla data corrente
    public static boolean dateWithinWeek(Date d) {
        Calendar c = Calendar.getInstance();

        c.setTime(c.getTime());
        c.add(Calendar.DATE,7);

        return c.getTime().compareTo(d) > 0;
    }
}
