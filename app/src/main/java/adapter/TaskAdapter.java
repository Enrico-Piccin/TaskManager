package adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import helper.FlipAnimator;
import model.Project;
import model.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {
    private Context mContext;                   // Contesto dell'activity chiamante
    private List<Task> tasks;                   // Lista di task
    private List<Project> projects;             // Lista di progetti
    private TaskAdapterListener listener;       // Listener dei click sui progetti
    private SparseBooleanArray selectedItems;   // Flag di controllo selezione

    // Array usato per effettuare animazioni multiple in una volta
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // L'indice è usato per animare solo la task selezionata
    private static int currentSelectedIndex = -1;

    // Container di oggetti grafici rappresentanti la singola task
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView content, dueDate, projectName;
        public ImageView borderIcon, contentIcon, projectColor;
        public LinearLayout messageContainer;
        public RelativeLayout iconContainer, iconBack, iconFront;

        public MyViewHolder(View view) {
            super(view);
            // Ottenimento delle referenze agli oggetti/proprietà di ciascuna task
            content = (TextView) view.findViewById(R.id.content);
            projectName = (TextView) view.findViewById(R.id.project_name);
            dueDate = (TextView) view.findViewById(R.id.due_date);
            iconBack = (RelativeLayout) view.findViewById(R.id.icon_back);
            iconFront = (RelativeLayout) view.findViewById(R.id.icon_front);
            borderIcon = (ImageView) view.findViewById(R.id.border_icon);
            contentIcon = (ImageView) view.findViewById(R.id.content_icon);
            projectColor = (ImageView) view.findViewById(R.id.project_color);
            messageContainer = (LinearLayout) view.findViewById(R.id.message_container);
            iconContainer = (RelativeLayout) view.findViewById(R.id.icon_container);
            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            // Gestione del click prolungato su una task
            listener.onRowLongClicked(getAdapterPosition());
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
    }

    // Costruttore generico
    public TaskAdapter(Context mContext, List<Task> tasks, List<Project> projects, TaskAdapterListener listener) {
        this.mContext = mContext;
        this.tasks = tasks;
        this.projects = projects;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate del layout personalizzato di ciascuna task
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Task task = tasks.get(position);
        Project p = projects.get(getProjectIdx(task.getIdProject()));

        int[] colorValues = mContext.getResources().getIntArray(R.array.mdcolor_500);

        // Visualizzazione dei dati nelle TextView
        holder.content.setText(task.getContent());
        holder.projectName.setText(p.getNome());
        holder.projectColor.setColorFilter(p.getColore() != 0 ? colorValues[getProjectColorPosition(p.getColore(), colorValues)] : mContext.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        holder.dueDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(task.getDueDate()));

        // Modifica dello stato del record in attivato
        holder.itemView.setActivated(selectedItems.get(position, false));

        // Applicazione dell'animazione dell'icona
        applyIconAnimation(holder, position);

        // Visualizzazione dell'immagine di profilo
        applyProfilePicture(holder, tasks.get(position));

        // Applicazione degli eventi click
        applyClickEvents(holder, position);
    }

    // Restituisce l'indice del colore selezionato nell'array
    private int getProjectColorPosition(int colore, int[] colorValues) {
        for(int i = 0; i < colorValues.length; i++) {
            if(colore == colorValues[i]) return i;
        }
        return 0;
    }

    private void applyClickEvents(MyViewHolder holder, final int position) {
        // Click sull'icona della task
        holder.iconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconClicked(position);
            }
        });

        // Click sul contenuto della task
        holder.messageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMessageRowClicked(position);
            }
        });

        // Click prolungato sul contenuto della task
        holder.messageContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
    }

    // Colorazione dell'icona della task in base alla priorità
    private void applyProfilePicture(MyViewHolder holder, Task task) {
        switch (task.getPriority()) {
            case 1:
                holder.contentIcon.setColorFilter(mContext.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                holder.borderIcon.setColorFilter(mContext.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                break;

            case 2:
                holder.contentIcon.setColorFilter(mContext.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                holder.borderIcon.setColorFilter(mContext.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                break;

            case 3:
                holder.contentIcon.setColorFilter(mContext.getResources().getColor(R.color.timestamp), PorterDuff.Mode.SRC_ATOP);
                holder.borderIcon.setColorFilter(mContext.getResources().getColor(R.color.timestamp), PorterDuff.Mode.SRC_ATOP);
                break;

            default:
                holder.contentIcon.setColorFilter(mContext.getResources().getColor(R.color.teal_700), PorterDuff.Mode.SRC_ATOP);
                holder.borderIcon.setColorFilter(mContext.getResources().getColor(R.color.teal_700), PorterDuff.Mode.SRC_ATOP);
                break;
        }
    }

    private void applyIconAnimation(MyViewHolder holder, int position) {
        // Gestione dell'animazione del click sull'icona della task
        if (selectedItems.get(position, false)) {
            holder.iconFront.setVisibility(View.GONE);
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE);
            holder.iconBack.setAlpha(1);
            if (currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, true);
                resetCurrentIndex();
            }
        } else {
            holder.iconBack.setVisibility(View.GONE);
            resetIconYAxis(holder.iconFront);
            holder.iconFront.setVisibility(View.VISIBLE);
            holder.iconFront.setAlpha(1);
            if ((reverseAllAnimations && animationItemsIndex.get(position, false)) || currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, false);
                resetCurrentIndex();
            }
        }
    }


    // Poiché le views saranno riutilizzate, a volte l'icona appare come capovolta perché la view più
    // vecchia viene riutilizzata. Per questo bisogna ripristinare l'asse Y a 0
    private void resetIconYAxis(View view) {
        if (view.getRotationY() != 0) {
            view.setRotationY(0);
        }
    }

    // Reset delle animazioni sulle task
    public void resetAnimationIndex() {
        reverseAllAnimations = false;
        animationItemsIndex.clear();
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).getIdTask();
    }

    // Restituisce l'indice del progetto dato il suo ID
    private int getProjectIdx(int id) {
        for(int i = 0; i < projects.size(); i++){
            if(id == projects.get(i).getIdProject())
                return i;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void toggleSelection(int pos) {
        // Abilitazione e aggiornamento grafico della selezione della task
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

    // Ritorna una una lista di indici delle task selezionate
    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    // Rimuove la task specificata
    public void removeData(int position) {
        tasks.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    // Forzatura dell'implementazione degli eventi di click con un'interfaccia
    public interface TaskAdapterListener {
        void onIconClicked(int position);

        void onMessageRowClicked(int position);

        void onRowLongClicked(int position);
    }
}