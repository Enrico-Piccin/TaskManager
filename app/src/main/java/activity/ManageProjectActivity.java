package activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.taskmanager.DatabaseHelper;
import com.example.taskmanager.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import adapter.ProjectAdapter;
import helper.DividerItemDecoration;
import model.Project;
import model.Task;
import model.User;

public class ManageProjectActivity extends AppCompatActivity implements ProjectAdapter.ProjectAdapterListener{
    private User u;
    private DatabaseHelper db;
    private List<Task> tasks = new ArrayList<>();                   // Lista di task
    private List<Project> projects = new ArrayList<>();             // Lista di progetti
    private List<Project> tempDeletedProjects = new ArrayList<>();  // Lista di progetti temporaneamente eliminati
    private List<Integer> selectedItemPositions;                    // Lista di progetti selezionati
    private ProjectAdapter mAdapter;                                // Adapter per la gestione delle modifiche grafiche alla lista di progetti
    private RecyclerView recyclerView;                              // Visualizzazione dinamica dei progetti
    private ActionModeCallback actionModeCallback;                  // Handling della selezione dei progetti
    private ActionMode actionMode;                                  // Gestione della modifica/cancellazione dei progetti

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_project);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Abilito la freccia in alto a sinistra per ritornare alla precedente activitv
        assert getSupportActionBar() != null;                   // Controllo NULL
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Mostro il back button

        db = new DatabaseHelper(this);
        if(getIntent().hasExtra("key_object"))
            u = getIntent().getParcelableExtra("key_object");   // Recupero l'oggetto passato dalla precedente activity

        ImageButton btnSend = toolbar.findViewById(R.id.btnAdd);
        // Gestione del click di aggiunta di un nuovo progetto
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageProjectActivity.this, AddProjectActivity.class);
                // Passaggio dell'utente e della modalità alla AddProjectActivity
                intent.putExtra("key_object", u);
                intent.putExtra("mode", "Add project");
                Log.d("Errore", "L'utente " + u.getNome());
                startActivityForResult(intent, TaskListActivity.PROJECT_LIST_CHANGED);
            }
        });

        // Ottenimento referenza della RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Creazione dell'Adapter di gestione grafica della lista di progetti e binding con la recyclerView
        mAdapter = new ProjectAdapter(this, projects, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        // Gestione dell'abilitazione dell'action mode per la selezione dei progetti
        actionModeCallback = new ActionModeCallback();

        getProjects();  // Ottenimento dei progetti dal database
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cancellazione della selezione, se attiva
        if(mAdapter.getSelectedItemCount() > 0) {
            mAdapter.clearSelections();
            actionMode.finish();
        }
        getProjects();  // Ottenimento dei progetti dal database
    }

    // Gestione del click sul Back Button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Ottenimento dei progetti dal database
    private void getProjects() {
        // Recupero progetti dal database
        List<Project> retrievedProjects = db.getAllUserProjects(u.getEmail());
        projects.clear();   // Pulizia della lista attuale
        if(retrievedProjects != null) {
            projects.addAll(retrievedProjects); // Aggiunta dei progetti recuperati
            projects.remove(0);           // Rimozione del progetti di default Inbox
        }
        mAdapter.notifyDataSetChanged();
    }

    // Ottenimento lista di task dal database
    private void getTasks() {
        // Recupero task dal database
        List<Task> retrivedTasks = db.getAllUserTasks(u.getEmail());
        if (retrivedTasks != null) {
            tasks.clear();                  // Pulizia della lista attuale
            tasks.addAll(retrivedTasks);    // Aggiunta delle task recuperate
        }
    }

    @Override
    public void onFavoriteClicked(int position) {
        projects.get(position).setFavorite(!projects.get(position).isFavorite());   // Aggiornamento grafico dello stato della preferenza
        db.updateProject(projects.get(position));                                   // Aggiornamento fisico nel database
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSelectProjectClicked(int position) {
        // Se un progetto viene premuto viene abilitata l'action mode
        enableActionMode(position);
    }

    @Override
    public void onRowLongClicked(int position) {
        // Se viene premuto a lungo un progetto, viene abilitata l'action mode
        enableActionMode(position);
    }

    private void enableActionMode(int position) {
        // Se action mode non è ancora stata attivata, viene istanziata
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);  // Abilitazione della selezione
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {        // Non ci sono più elementi selezionati
            actionMode.finish(); // Terminazione dell'actionMode
        } else {
            MenuItem edit = actionMode.getMenu().findItem(R.id.action_edit);

            // Se è selezionato più di un elemento disabilita l'edit dei progetti
            edit.setEnabled(count <= 1);
            edit.setVisible(edit.isEnabled());

            // Aggiornamento grafico del numero di elementi selezionati
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inserimento del menu personalizzato actionmode
            mode.getMenuInflater().inflate(R.menu.menu_manage_project, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                // Se si vuole modificare il progetto selezionato
                case R.id.action_edit:
                Intent intent = new Intent(ManageProjectActivity.this, AddProjectActivity.class);
                // Passaggio dell'utente, della modalità e del progetto da modificare alla AddProjectActivity
                intent.putExtra("key_object", u);
                intent.putExtra("mode", "Edit project");
                intent.putExtra("edit_project", projects.get(mAdapter.getSelectedItems().get(0)));
                Log.d("Errore", "L'utente " + u.getNome());
                startActivityForResult(intent, TaskListActivity.PROJECT_LIST_CHANGED);
                return true;

                case R.id.action_delete:
                // Cancellazione grafica dei progetti selezionati
                deleteProjects();
                mode.finish();
                // Messaggio progetti eliminati
                Snackbar snackBar;
                Log.d("Errore", "Ho creato la snackbar");
                if(tempDeletedProjects.size() == 1)
                    snackBar = Snackbar.make(findViewById(R.id.recycler_view), tempDeletedProjects.size() + " progetto è stato eliminato.", Snackbar.LENGTH_LONG);
                else
                    snackBar = Snackbar.make(findViewById(R.id.recycler_view), tempDeletedProjects.size() + " progetti sono stati eliminati.", Snackbar.LENGTH_LONG);
                snackBar.setAction("ANNULLA", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        restoreProjects();
                    }
                })
                // Quando la Snackbar non è più visualizzata a schermo
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            List<Integer> definitevelyDeleteProjects = new ArrayList<>();   // Progetti da eliminare fisicamente
                            List<Integer> definitevelyDeleteTasks = new ArrayList<>();      // Task da eliminare fisicamente
                            getTasks();
                            for(Project dp : tempDeletedProjects) { // Scorrimento dei progetti selezionati ed eliminati
                                definitevelyDeleteProjects.add(dp.getIdProject());  // Aggiunta dei progetti da eliminare
                                for (Task dt : tasks) {
                                    if(dt.getIdProject() == dp.getIdProject())
                                        definitevelyDeleteTasks.add(dt.getIdTask()); // Aggiunta delle task da eliminare
                                }
                            }
                            db.deleteProjects(definitevelyDeleteProjects);  // Eliminazione fisica dei progetti
                            db.deleteTasks(definitevelyDeleteTasks);        // Eliminazione fisica delle task
                            // Pulizia delle liste temporanee
                            selectedItemPositions.clear();
                            tempDeletedProjects.clear();
                        }
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
                return true;

                default:
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Reset della actionMode e della recyclerView
            mAdapter.clearSelections();
            actionMode = null;
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    // Cancellazione logica dei progetti dalla RecyclerView
    private void deleteProjects() {
        // Indici selezionati
        selectedItemPositions = mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            // Aggiunta dei progetti da eliminare in una lista temporanea e rimozione degli stessi
            tempDeletedProjects.add(projects.get(selectedItemPositions.get(i)));
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }

    // Ripristino dei progetti nella RecyclerView
    private void restoreProjects() {
        for (int i = 0; i < selectedItemPositions.size(); i++) {
            // Aggiunta dei progetti eliminati nella lista corrente, nella stessa posizione di prima
            projects.add(selectedItemPositions.get(i), tempDeletedProjects.get(selectedItemPositions.size() - 1 - i));
        }
        // Pulizia della lista temporanea e della selezione
        selectedItemPositions.clear();
        tempDeletedProjects.clear();
        mAdapter.notifyDataSetChanged();
    }
}