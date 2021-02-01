package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.example.taskmanager.DatabaseHelper;
import com.example.taskmanager.R;
import activity.TaskListActivity;

import java.util.ArrayList;
import java.util.List;

import activity.AddProjectActivity;
import activity.ManageProjectActivity;
import adapter.ProjectListAdapter;
import adapter.TaskAdapter;
import model.Project;
import model.Task;
import model.User;

public class NavProjectsFragment extends Fragment {
    List<Task> tasks;                           // Lista di task
    TaskAdapter mAdapter;                       // Adapter per l'aggiornamento dinamico delle task
    List<Project> projects = new ArrayList<>(); // Lista di progetti
    List<Integer> numTasks = new ArrayList<>(); // Lista con il numero di task per progetto
    User u;
    FrameLayout fragmentContainer;              // FrameLayout per la visualizzazione di un messaggio

    // Costruttore di default
    public NavProjectsFragment() { }

    // Costruttore generico
    public NavProjectsFragment(List<Task> tasks, TaskAdapter mAdapter, List<Project> projects, List<Integer> numTasks, User u, FrameLayout fragmentContainer) {
        this.tasks = tasks;
        this.mAdapter = mAdapter;
        this.projects = projects;
        this.numTasks = numTasks;
        this.u = u;
        this.fragmentContainer = fragmentContainer;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate del layout personalizzato
        View view = inflater.inflate(R.layout.fragment_project_drawer, container, false);

        // Referenza alla ListView delle layout
        ListView listView = view.findViewById(R.id.listView);

        // Caricamento lista di progetti
        ArrayList<Project> itemList = new ArrayList<>();
        for(int i = 1; i < projects.size(); i++) itemList.add(projects.get(i));

        ProjectListAdapter mAdapter = new ProjectListAdapter(itemList, (ArrayList<Integer>) numTasks, view.getContext(), 0);
        listView.setAdapter(mAdapter);
        // Gestione del click su un singolo progetto
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Project p = itemList.get(position);
                itemList.set(position, p);
                TaskListActivity.viewType.setText(p.getNome()); // Aggiornamento della modalità di visualizzazione
                tasks.clear();  // Pulizia lista di task
                // Aggiunta delle task dal database
                ArrayList<Task> tempTasks = new DatabaseHelper(getContext()).getAllUserTasks(u.getEmail());
                if(tempTasks != null)
                    tasks.addAll(tempTasks);
                // Visualizzazione delle task e chiusura del Navigation Drawer
                updateProjectTask(p.getIdProject());
                if(TaskListActivity.drawerLayout.isDrawerOpen(GravityCompat.START))
                    TaskListActivity.drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        mAdapter.notifyDataSetChanged();
        RelativeLayout relativeLayout = view.findViewById(R.id.relativeLayoutContainer);

        // Aggiustamento grafico dell'altezza del Fragment
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) ((projects.size() + 2) * 32 * scale + 0.5f);

        RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, pixels);
        rel_btn.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        relativeLayout.setLayoutParams(rel_btn);
        relativeLayout.setScrollbarFadingEnabled(true);

        // Gestione del click sul layout per l'aggiunta di un nuovo progetto
        ConstraintLayout add = view.findViewById(R.id.constraintLayoutAdd);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddProjectActivity.class);
                // Passaggio dei dati all'AddProjectActivity
                intent.putExtra("key_object", u);
                intent.putExtra("mode", "Add project");
                Log.d("Errore", "L'utente " + u.getNome());
                startActivityForResult(intent, TaskListActivity.PROJECT_LIST_CHANGED);
            }
        });

        // Gestione del click sul layout per la gestione dei progetti correnti
        ConstraintLayout manage = view.findViewById(R.id.constraintLayoutColor);
        manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ManageProjectActivity.class);
                // Passaggio dei dati alla ManageProjectActivity
                intent.putExtra("key_object", u);
                Log.d("Errore", "L'utente " + u.getNome());
                startActivityForResult(intent, TaskListActivity.PROJECT_LIST_CHANGED);
            }
        });

        return view;
    }

    // Aggiornamento grafico delle task appartenenti al progetto selezionato
    private void updateProjectTask(int projectID) {
        ArrayList<Task> tempTasks = new ArrayList<>();
        tempTasks.addAll(tasks);
        for (Task t : tempTasks)
            if(t.getIdProject() != projectID)
                mAdapter.removeData(tasks.indexOf(t));  // Rimozione delle task che non appartengono al progetto selezionato
        mAdapter.notifyDataSetChanged();

        if(mAdapter.getItemCount() == 0) {
            // Visualizzazione del messaggio che comunica l'assenza di task
            if(fragmentContainer.getVisibility() != View.VISIBLE) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, new NoTaskFragment())
                        .addToBackStack(null)
                        .commit();
            }
        } else fragmentContainer.setVisibility(View.GONE);  // Il FrameLayout di visualizzazione viene nascosto
    }
}
