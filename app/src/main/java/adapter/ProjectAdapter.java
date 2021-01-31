package adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;

import java.util.ArrayList;
import java.util.List;

import model.Project;


public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.MyViewHolder> {
    private Context mContext;                   // Contesto dell'activity chiamante
    private List<Project> projects;             // Lista di progetti
    private ProjectAdapterListener listener;    // Listener dei click sui progetti
    private SparseBooleanArray selectedItems;   // Flag di controllo selezione

    // Array usato per effettuare animazioni multiple in una volta
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // L'indice è usato per animare solo il progetto selezionato
    private static int currentSelectedIndex = -1;

    // Container di oggetti grafici rappresentanti il singolo progetto
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public ConstraintLayout selectProject, projectFavorite;
        public ImageView projectColor, favorite;
        public TextView projectName;

        public MyViewHolder(View view) {
            super(view);
            // Ottenimento delle referenze agli oggetti/proprietà di ciascun progetto
            selectProject = (ConstraintLayout) view.findViewById(R.id.selectProject);
            projectFavorite = (ConstraintLayout) view.findViewById(R.id.projectFavorite);
            projectColor = (ImageView) view.findViewById(R.id.projectColor);
            favorite = (ImageView) view.findViewById(R.id.favorite);
            projectName = (TextView) view.findViewById(R.id.projectName);
            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            // Gestione del click prolungato su un progetto
            listener.onRowLongClicked(getAdapterPosition());
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
    }

    // Costruttore generico
    public ProjectAdapter(Context mContext, List<Project> projects, ProjectAdapterListener listener) {
        this.mContext = mContext;
        this.projects = projects;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate del layout personalizzato di ciascun progetto
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Project p = projects.get(position);

        // Visualizzazione dei dati nelle TextView
        holder.projectColor.setColorFilter(p.getColore(), PorterDuff.Mode.SRC_ATOP);
        holder.projectName.setText(p.getNome());

        // Aggiornamento grafico del widget favorite
        if(p.isFavorite()) {
            holder.favorite.setImageResource(mContext.getResources().getIdentifier("heart_full", "drawable", mContext.getPackageName()));
            holder.favorite.setColorFilter(mContext.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.favorite.setImageResource(mContext.getResources().getIdentifier("no_heart", "drawable", mContext.getPackageName()));
            holder.favorite.setColorFilter(mContext.getResources().getColor(R.color.teal_700), PorterDuff.Mode.SRC_ATOP);
        }

        // Modifica dello stato del record in attivato
        holder.itemView.setActivated(selectedItems.get(position, false));

        // Applicazione degli eventi click
        applyClickEvents(holder, position);
    }

    private void applyClickEvents(MyViewHolder holder, final int position) {
        // Click sul singolo progetto
        holder.selectProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSelectProjectClicked(position);
            }
        });

        // Click sul widget favorite
        holder.projectFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onFavoriteClicked(position);
            }
        });

        // Click prolungato sul singolo progetto
        holder.selectProject.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return projects.get(position).getIdProject();
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void toggleSelection(int pos) {
        // Abilitazione e aggiornamento grafico della selezione del progetto
        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            animationItemsIndex.delete(pos);
        } else {
            selectedItems.put(pos, true);
            animationItemsIndex.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        // Pulizia della selezione
        reverseAllAnimations = true;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    // Ritorna una una lista di indici dei progetti selezionati
    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    // Rimuove il progetto specificato
    public void removeData(int position) {
        projects.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    // Forzatura dell'implementazione degli eventi di click con un'interfaccia
    public interface ProjectAdapterListener {
        void onFavoriteClicked(int position);

        void onSelectProjectClicked(int position);

        void onRowLongClicked(int position);
    }
}