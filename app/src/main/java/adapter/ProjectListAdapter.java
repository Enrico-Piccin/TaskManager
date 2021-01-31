package adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.taskmanager.R;

import java.util.ArrayList;

import model.Project;

public class ProjectListAdapter extends BaseAdapter {

    private ArrayList<Project> mData;       // Lista di dati passati dal chiamante
    private ArrayList<Integer> numTasks;    // Numero di task per progetto
    private Context mContext;               // Contesto dell'activity chiamante
    private LayoutInflater inflater;        // Oggetto per l'inflate del layout personalizzato
    private int bg_color;                   // Colore di bacground del layout

    // Costruttore generico
    public ProjectListAdapter(ArrayList<Project> data, ArrayList<Integer> numTasks, Context context, int bg_Color) {
        mData = data;
        this.numTasks = numTasks;
        mContext = context;
        inflater = LayoutInflater.from(context);
        this.bg_color = bg_Color;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate del layout
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.project_choice_item, parent, false);
        }

        // Impostazione del colore di background della view
        if(bg_color != 0)
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, bg_color));

        // Inizializzazione ed impostazione del numero di task del progetto corrente
        TextView textViewNumTasks = convertView.findViewById(R.id.textViewNumTasks);
        textViewNumTasks.setText(numTasks.get(position + 1) == 0 ? "" : numTasks.get(position + 1).toString());

        // Inizializzazione ed impostazione del nome progetto corrente
        ImageView colorValue = convertView.findViewById(R.id.imgProjectColor);
        TextView colorName = convertView.findViewById(R.id.textViewProjectName);

        // Inizializzazione ed impostazione del colore del progetto corrente
        if(mData.get(position).getColore() == 0) {
            colorValue.setImageResource(R.drawable.inbox);
            colorValue.setColorFilter(R.color.colorPrimary, PorterDuff.Mode.SRC_ATOP);
        } else {
            colorValue.setImageResource(R.drawable.round_background);
            colorValue.setColorFilter(mData.get(position).getColore(), PorterDuff.Mode.SRC_ATOP);
        }
        colorName.setText(mData.get(position).getNome());

        return convertView;
    }
}