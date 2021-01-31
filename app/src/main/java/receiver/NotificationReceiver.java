package receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taskmanager.DatabaseHelper;
import com.example.taskmanager.R;

import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import activity.LoginActivity;
import activity.TaskListActivity;
import helper.DateManipulation;
import model.Task;
import model.User;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "TASK_CHANNEL";    // Canale di notifica
    User u;
    private DatabaseHelper db;

    // Costruttore di default
    public NotificationReceiver() { }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Inizializzazione del NotificationManager e creazione del canale di notifica
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(context, notificationManager);

        db = new DatabaseHelper(context);

        Bundle bundle = intent.getBundleExtra("bundle");
        if(bundle != null)
            u = bundle.getParcelable("key_object");   // Recupero l'oggetto passato dalla precedente activity

        // Intent per passare alla LoginActivity al click della notifica
        Intent resultIntent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Ottenimento del messaggio di notifica
        int[] overdueUpcoming = new int[2];
        String msg = getNotificationMessage(overdueUpcoming);

        // Costruzione della notifica
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle(overdueUpcoming[0] + " arretrate, " + overdueUpcoming[1] + " da fare")
                        .setContentText("Espandi per vedere le task")
                        // Viene impostato l'intent che si attiva quando l'utente tocca la notifica
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        // Abilitazione e attivazione della notifica
        if (intent.getAction().equals("MY_NOTIFICATION_MESSAGE")) {
            notificationManagerCompat.notify(100, mBuilder.build());
        }
    }

    private void createNotificationChannel(Context context, NotificationManager notificationManager) {
        // Viene creato il NotificationChannel, ma solo su API 26+ perché la classe
        // NotificationChannel è nuova e non si trova nella libreria di supporto
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Viene registra il canale con il sistema
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Ottenimento del messaggio di notifica
    public String getNotificationMessage(int[] overdueUpcoming) {
        // Recupero task dal database
        List<Task> retrivedTasks = db.getAllUserTasks(u.getEmail());
        StringBuilder msg = new StringBuilder();

        if(retrivedTasks != null) {
            // Ordinamento delle task in ordine crescente di data
            Collections.sort(retrivedTasks, new Comparator<Task>() {
                public int compare(Task t1, Task t2) {
                    return t1.getDueDate().compareTo(t2.getDueDate());
                }
            });

            // Creazione del messaggio della notifica
            for (Task t : retrivedTasks) {
                if(t.getDueDate().before(Calendar.getInstance().getTime())) {
                    overdueUpcoming[0]++;
                    msg.append((int) ((Calendar.getInstance().getTime().getTime() - t.getDueDate().getTime()) / (1000 * 60 * 60 * 24))).append(" giorni fa, ").append(t.getContent()).append("\n");
                }
                else {
                    overdueUpcoming[1]++;
                    msg.append("Fra ").append((int)((t.getDueDate().getTime() -Calendar.getInstance().getTime().getTime()) / (1000 * 60 * 60 * 24))).append(" giorni, ").append(t.getContent()).append("\n");
                }
            }
        }
        return msg.toString();  // Ritorno al chiamante del messaggio di notifica
    }
}