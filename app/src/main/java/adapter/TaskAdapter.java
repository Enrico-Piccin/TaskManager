package adapter;

import android.content.Context;
import android.graphics.Color;
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

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.taskmanager.R;
import com.google.android.material.transition.Hold;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import helper.CircleTransform;
import helper.FlipAnimator;
import model.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {
    private Context mContext;
    private List<Task> tasks;
    private TaskAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // Array usato per effettuare animazioni multimple in una volta
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // L'indice è usato per animare solo la Task selezionata
    private static int currentSelectedIndex = -1;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView content, dueDate, projectName;
        public ImageView borderIcon, contentIcon, projectColor;
        public LinearLayout messageContainer;
        public RelativeLayout iconContainer, iconBack, iconFront;

        public MyViewHolder(View view) {
            super(view);
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
            listener.onRowLongClicked(getAdapterPosition());
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
    }

    public TaskAdapter(Context mContext, List<Task> tasks, TaskAdapterListener listener) {
        this.mContext = mContext;
        this.tasks = tasks;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Task task = tasks.get(position);

        // Visualizzazione dei dati nelle TextView
        holder.content.setText(task.getContent());
        holder.projectName.setText(/*task.getIdProject()*/"Sistemi");
        holder.dueDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(task.getDueDate()));

        // Modifica dello stato del record in attivato
        holder.itemView.setActivated(selectedItems.get(position, false));

        /*
        // change the font style depending on message read status
        applyReadStatus(holder, message);

        // handle message star
        applyImportant(holder, message);
        */

        // Applicazione dell'animazione dell'icona
        applyIconAnimation(holder, position);

        // Visualizzazione dell'immagine di profilo
        applyProfilePicture(holder, tasks.get(position));

        // Applicazione degli eventi click
        applyClickEvents(holder, position);
    }

    private void applyClickEvents(MyViewHolder holder, final int position) {
        holder.iconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconClicked(position);
            }
        });
/*
        holder.iconImp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconImportantClicked(position);
            }
        });
*/
        holder.messageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMessageRowClicked(position);
            }
        });

        holder.messageContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
    }


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
        /*
        if (!TextUtils.isEmpty(message.getPicture())) {
            Glide.with(mContext).load(message.getPicture())
                    .thumbnail(0.5f)
                    .crossFade()
                    .transform(new CircleTransform(mContext))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.contentIcon);
            holder.contentIcon.setColorFilter(null);
            holder.iconText.setVisibility(View.GONE);
        } else {
            holder.contentIcon.setImageResource(R.drawable.round_background);
            holder.contentIcon.setColorFilter(message.getColor());
            holder.iconText.setVisibility(View.VISIBLE);
        }
        */

    }

    private void applyIconAnimation(MyViewHolder holder, int position) {
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

    public void resetAnimationIndex() {
        reverseAllAnimations = false;
        animationItemsIndex.clear();
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).getIdTask();
    }

    public static int addAlpha(int color, int alpha) {
        return ColorUtils.setAlphaComponent(color, alpha);
    }

/*
    private void applyImportant(MyViewHolder holder, Message message) {
        if (message.isImportant()) {
            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.good_login));
            holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.selected));
        } else {
            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.bad_login));
            holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.message));
        }
    }

    private void applyReadStatus(MyViewHolder holder, Message message) {
        if (message.isRead()) {
            holder.from.setTypeface(null, Typeface.NORMAL);
            holder.subject.setTypeface(null, Typeface.NORMAL);
            holder.from.setTextColor(ContextCompat.getColor(mContext, R.color.subject));
            holder.subject.setTextColor(ContextCompat.getColor(mContext, R.color.message));
        } else {
            holder.from.setTypeface(null, Typeface.BOLD);
            holder.subject.setTypeface(null, Typeface.BOLD);
            holder.from.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            holder.subject.setTextColor(ContextCompat.getColor(mContext, R.color.subject));
        }
    }
*/

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void toggleSelection(int pos) {
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
        reverseAllAnimations = true;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void removeData(int position) {
        tasks.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    public interface TaskAdapterListener {
        void onIconClicked(int position);

        // void onIconImportantClicked(int position);

        void onMessageRowClicked(int position);

        void onRowLongClicked(int position);
    }
}