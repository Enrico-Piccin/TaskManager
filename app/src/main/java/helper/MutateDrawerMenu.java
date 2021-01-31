package helper;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.taskmanager.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import model.User;

public class MutateDrawerMenu {
    // Ottenimento del numero di giorno corrente
    private static int getNumDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        return Integer.parseInt(sdf.format(new Date()));
    }

    // Aggiornamento grafico degli items presenti nel Navigation Drawer Menu
    public static void mutateDrawerMenu(Context c, Menu menu) {
        ((Drawable) menu.getItem(0).getIcon()).mutate().setColorFilter(c.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        menu.getItem(1).setIcon(ResourcesCompat.getDrawable(c.getResources(),
                c.getResources().getIdentifier(EnglishNumberWord.convert(getNumDay()), "drawable", c.getPackageName()), null));
        ((Drawable) menu.getItem(1).getIcon()).mutate().setColorFilter(c.getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        ((Drawable) menu.getItem(2).getIcon()).mutate().setColorFilter(c.getResources().getColor(R.color.purple_200), PorterDuff.Mode.SRC_ATOP);
    }

    // Aggiornamento grafico degli items presenti nel Navigation Drawer Header
    public static void mutateNavHeader(Context c, ImageView iconImage, TextView textIcon, TextView username, User u) {
        iconImage.setColorFilter(u.getColore(), PorterDuff.Mode.SRC_ATOP);  // Aggiornamento del colore dell'icona dell'utente

        textIcon.setText(u.getNome().toUpperCase().substring(0, 1));        // Aggiornamento dell'iniziale dell'utente

        username.setText(u.getNome());  // Aggiornamento del nome dell'utente
    }
}
