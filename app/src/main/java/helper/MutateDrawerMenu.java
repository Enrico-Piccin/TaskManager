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
    private static int getNumDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        return Integer.parseInt(sdf.format(new Date()));
    }

    public static void mutateDrawerMenu(Context c, Menu menu) {
        ((Drawable) menu.getItem(0).getIcon()).mutate().setColorFilter(c.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        menu.getItem(1).setIcon(ResourcesCompat.getDrawable(c.getResources(),
                c.getResources().getIdentifier(EnglishNumberWord.convert(getNumDay()), "drawable", c.getPackageName()), null));
        ((Drawable) menu.getItem(1).getIcon()).mutate().setColorFilter(c.getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        ((Drawable) menu.getItem(2).getIcon()).mutate().setColorFilter(c.getResources().getColor(R.color.purple_200), PorterDuff.Mode.SRC_ATOP);
    }

    public static void mutateNavHeader(Context c, ImageView iconImage, TextView textIcon, TextView username, User u) {
        /*
        int arrayId = c.getResources().getIdentifier("mdcolor_" + u.getColore().substring(u.getColore().length() - 3), "array", c.getPackageName());
        TypedArray colors = c.getResources().obtainTypedArray(arrayId);
        iconImage.setBackgroundColor(colors.getResources().getIdentifier(u.getColore(), "color", c.getPackageName())); */

        iconImage.setColorFilter(u.getColore(), PorterDuff.Mode.SRC_ATOP);

        textIcon.setText(u.getNome().toUpperCase().substring(0, 1));

        username.setText(u.getNome());
    }
}
