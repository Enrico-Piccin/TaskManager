package activity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import androidx.appcompat.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.taskmanager.DatabaseHelper;
import com.example.taskmanager.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import helper.KeyboardUtils;
import adapter.ProjectListAdapter;
import adapter.TaskAdapter;
import fragment.DatePickerFragment;
import fragment.NavProjectsFragment;
import fragment.NoTaskFragment;
import helper.DateManipulation;
import helper.DividerItemDecoration;
import helper.MutateDrawerMenu;
import model.Project;
import model.Task;
import model.User;
import receiver.NotificationReceiver;

public class TaskListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        DatePickerDialog.OnDateSetListener, SwipeRefreshLayout.OnRefreshListener, TaskAdapter.TaskAdapterListener {
    public static final int PROJECT_LIST_CHANGED = 0xe110;  // Codice univoco per startActivityForResult
    private List<Task> tasks = new ArrayList<>();           // Lista di task
    private List<Task> tempDeleted = new ArrayList<>();     // Lista di task temporaneamente eliminate
    private List<Project> projects = new ArrayList<>();     // Lista di progetti
    private List<Integer> selectedItemPositions;            // Lista degli indici delle task selezionate
    private DatabaseHelper db;
    private RecyclerView recyclerView;                      // Visualizzazione dinamica delle task
    private TaskAdapter mAdapter;                           // Adapter per la gestione delle modifiche grafiche alla lista di task
    private SwipeRefreshLayout swipeRefreshLayout;          // Gestione del refresh delle task
    private ActionModeCallback actionModeCallback;          // Handling della selezione delle task
    private ActionMode actionMode;                          // Gestione della modifica/cancellazione delle task
    private User u;
    public static DrawerLayout drawerLayout;                // Navigation Drawer laterale (sinistra o START)
    private NavigationView navigationView;                  // Menu contestuale incapsulato nel DrawerLayout
    private boolean hasChanged = false;                     // Flag booleano di controllo di eventuali modifiche ai campi della addTask
    // Widget grafici per l'inserimento delle task
    Button btnDateTimePicker, btnProjectSelector;
    EditText editTextTextMultiLine;
    ImageButton btnPriority;
    public static TextView viewType;                        // TextView per la visualizzazione della modalità di visualizzazione delle task
    Task t;                                                 // Task utility di inserimento o modifica
    AlertDialog alert, dialogAddModify;                     // Istanze di AlertDialog visibili contestualmente nella classe
    FrameLayout fragmentContainer;                          // Frame container per la visualizzazione dinamica di un messaggio

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Referenziazione e inizializzazione degli oggetti grafici
        viewType = findViewById(R.id.view_type);
        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.GONE);

        db = new DatabaseHelper(this);
        if(getIntent().hasExtra("key_object"))
            u = getIntent().getParcelableExtra("key_object");   // Recupero l'oggetto passato dalla precedente activity

        // Aggiornamento della data di ultimo accesso e delle task completate
        if(!DateManipulation.areSameDate(u.getLastAccess(), Calendar.getInstance().getTime())) {
            u.setTask(0);
            u.setLastAccess(Calendar.getInstance().getTime());
            db.updateUser(u);
        }

        // Referenziazione e inizializzazione del drawerLayout e del navigationViwe
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); // Gestione dei click sugli item del menu drawer

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Gestione dello slide del drawer
        drawerLayout.addDrawerListener(new CustomDrawer());
        // Aggiornamento dello stato del menu drawer
        MutateDrawerMenu.mutateDrawerMenu(TaskListActivity.this, navigationView.getMenu());

        // Se il drawerLayout è aperto viene aggiornato
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            updateDrawer();

        // Pulsante dinamico per l'aggiunta delle task
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addModifyTask(null, true);
            }
        });

        // Messaggio di benvenuto
        Snackbar snackBar = Snackbar.make(fab, "Benvenuto/a " + u.getNome(), Snackbar.LENGTH_LONG);
            snackBar.setAction("ANNULLA", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackBar.dismiss();
                }
            })
            .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
            .show();

        // Ottenimento referenza della RecyclerView e dello swipeRefreshLayout
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        // Creazione dell'Adapter di gestione grafica della lista di task e binding con la recyclerView
        mAdapter = new TaskAdapter(this, tasks, projects, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        // Gestione dell'abilitazione dell'action mode per la selezione delle task
        actionModeCallback = new ActionModeCallback();

        // Viene mostrato un caricamento e si recuperano i progetti e le tasks
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        getProjects();
                        updateViewTasks(viewType.getText().toString());
                    }
                }
        );

        // Se le notifiche sono abilitate
        if(getSharedPreferences(u.getEmail(), Context.MODE_PRIVATE).getInt("NOTIFICATIONS", 1) != 0) {
            Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
            // Passaggio dell'utente al BroadcastReceiver che genera le notifiche
            Bundle bundle = new Bundle();
            bundle.putParcelable("key_object", u);
            intent.putExtra("bundle", bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setAction("MY_NOTIFICATION_MESSAGE");

            // Creazione del PendinIntent che verrà eseguito solo allo scadere del tempo dell'AlarmManger
            PendingIntent servicePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Gestore di un timer/alarm per la visualizzazione delle notifiche
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // Ora, minuti e secondi in cui visualizzare la notifica
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // Impostazione di una notifica ciclica ogni 24 ore
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, servicePendingIntent);
        }
    }

    // Gestione dell'aggiunta/modifica delle task
    public void addModifyTask(Task defaultTask, boolean isAdd) {
        // Creazione dell'AlertDialog con layout personalizzato
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TaskListActivity.this);
        View layoutView = TaskListActivity.this.getLayoutInflater().inflate(R.layout.add_task, null);

        // A seconda che la task debba essere aggiunta o meno si inizializza l'oggetto t
        if(defaultTask != null) {
            t = defaultTask;
            t.setEmail(u.getEmail());
            Log.d("Errore", "L'utente della default task è " + t.getEmail());
        }
        else
            t = new Task(0, null, 4, Calendar.getInstance().getTime(), 1, u.getEmail());

        ImageButton btnSend = layoutView.findViewById(R.id.send);
        // Gestione del click di aggiunta di una nuova Task
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.setContent(editTextTextMultiLine.getText().toString());
                if(isAdd) {
                    db.addTask(t);      // Aggiunta task
                    updateViewTasks(viewType.getText().toString().toLowerCase());
                    // Reinizializzazione della task t
                    t = new Task(0, null, 4, Calendar.getInstance().getTime(), 1, u.getEmail());
                    resetField();       // Pulizia dei campi di inserimento
                } else {
                    db.updateTask(t);   // Modifica task
                    updateViewTasks(viewType.getText().toString().toLowerCase());
                    dialogAddModify.dismiss();
                }
                // Il FrameLayout di visualizzazione viene nascosto
                fragmentContainer.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            }
        });

        ImageView btnSendBg = layoutView.findViewById(R.id.send_bg);
        editTextTextMultiLine = layoutView.findViewById(R.id.editTextTextMultiLine);
        editTextTextMultiLine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Se sono state apportate delle modifiche al contenuto della task si abilita l'inserimento
                if(s.length() != 0) {
                    btnSendBg.setColorFilter(TaskListActivity.this.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                    btnSend.setEnabled(true);
                    // Aggiornamento flag di modifica
                    hasChanged = hasChanged ? hasChanged : !editTextTextMultiLine.getText().toString().equals(t.getContent());
                }
                else {
                    btnSend.setEnabled(false);
                    btnSendBg.setColorFilter(TaskListActivity.this.getResources().getColor(R.color.dark_card), PorterDuff.Mode.SRC_ATOP);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        // Impostazione del testo della EditText con il contenuto della task da modificare, se != null
        editTextTextMultiLine.setText(t != null ? t.getContent() : "");
        hasChanged = false;

        // Inizializzazione del bottone di inserimento data
        btnDateTimePicker = layoutView.findViewById(R.id.btnDateTimePicker);
        if(t != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(t.getDueDate());
            btnDateTimePicker.setText(datePicked(c));
        }
        btnDateTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Impostazione della lingua e del fuso orario del DatePicker
                Configuration configuration = TaskListActivity.this.getResources().getConfiguration();
                Locale locale = Locale.ITALY;
                configuration.setLocale(locale);
                configuration.setLayoutDirection(locale);
                TaskListActivity.this.createConfigurationContext(configuration);
                Locale.setDefault(locale);

                // Inizializzazione del DatePicker
                DialogFragment datePicker = new DatePickerFragment(t.getDueDate());
                datePicker.show(getSupportFragmentManager(), "Seleziona data");
            }
        });

        // Inizializzazione del bottone di selezione del progetto di appartenenza della task
        btnProjectSelector = layoutView.findViewById(R.id.btnProjectSelector);
        if(defaultTask != null) {
            // Impostazione del testo, del colore e della icona del bottone
            setBtnProjectSelector();
        }
        btnProjectSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creazione di un AlertDialog personalizzato
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);
                builder.setCancelable(true);
                View customView = LayoutInflater.from(TaskListActivity.this).inflate(
                        R.layout.list_view_color, null, false);
                ListView listView = customView.findViewById(R.id.listView);

                ProjectListAdapter mAdapter = new ProjectListAdapter((ArrayList<Project>) projects, new ArrayList<>(Collections.nCopies(projects.size() + 1, 0)), TaskListActivity.this, R.color.dark_card);
                listView.setAdapter(mAdapter);
                // Gestione del click di impostazione del progetto
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Project p = projects.get(position);
                        // Aggiornamento flag di modifica
                        hasChanged = hasChanged ? hasChanged : t.getIdProject() != p.getIdProject();

                        // Memorizzazione dell'ID del progetto scelto
                        t.setIdProject(p.getIdProject());

                        // Impostazione del testo, del colore e della icona del bottone
                        setBtnProjectSelector();
                        alert.dismiss();    // Dismiss del Dialog
                    }
                });
                builder.setView(customView);
                alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();   // Visualizzazione del Dialog
            }
        });

        // Inizializzazione del bottone di selezione della priorità della task
        btnPriority = layoutView.findViewById(R.id.priority);
        if(defaultTask != null) {
            // Impostazione della colorazione del bottone a seconda della priorità
            changeDrawableColor(btnPriority, t.getPriority() == 1 ? R.color.red : t.getPriority() == 2 ?
                    R.color.colorAccent : t.getPriority() == 3 ? R.color.timestamp : R.color.teal_700);
        }
        // Gestione del click di impostazione della priorità
        btnPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creazione di un AlertDialog con layout personalizzato
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);
                builder.setCancelable(true);

                LayoutInflater inflater = TaskListActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.priority_layout, null);
                builder.setView(dialogView);
                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialogView.findViewById(R.id.p1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Aggiornamento flag di modifica
                        hasChanged = hasChanged ? hasChanged : t.getPriority() != 1;
                        t.setPriority(1);   // Impostazione priorità
                        alert.dismiss();
                        // Aggiornamento grafico
                        changeDrawableColor(btnPriority, R.color.red);
                    }
                });

                dialogView.findViewById(R.id.p2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Aggiornamento flag di modifica
                        hasChanged = hasChanged ? hasChanged : t.getPriority() != 2;
                        t.setPriority(2);   // Impostazione priorità
                        alert.dismiss();
                        // Aggiornamento grafico
                        changeDrawableColor(btnPriority, R.color.colorAccent);
                    }
                });

                dialogView.findViewById(R.id.p3).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Aggiornamento flag di modifica
                        hasChanged = hasChanged ? hasChanged : t.getPriority() != 3;
                        t.setPriority(3);   // Impostazione priorità
                        alert.dismiss();
                        // Aggiornamento grafico
                        changeDrawableColor(btnPriority, R.color.timestamp);
                    }
                });

                dialogView.findViewById(R.id.p4).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Aggiornamento flag di modifica
                        hasChanged = hasChanged ? hasChanged : t.getPriority() != 4;
                        t.setPriority(4);   // Impostazione priorità
                        alert.dismiss();
                        // Aggiornamento grafico
                        changeDrawableColor(btnPriority, R.color.teal_700);
                    }
                });
                alert.show();   // Visualizzazione del Dialog
            }
        });

        dialogBuilder.setView(layoutView);
        dialogAddModify = dialogBuilder.create();

        // Impostazione della finestra del dialog
        Window window = dialogAddModify.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.gravity = Gravity.BOTTOM;
        wlp.windowAnimations = R.style.AddTask;
        window.setAttributes(wlp);

        // Listener per il controllo della visualizzazione della tastiera
        KeyboardUtils.addKeyboardToggleListener(TaskListActivity.this, new KeyboardUtils.SoftKeyboardToggleListener()
        {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible)
            {
                if(isVisible) {     // Sposta dialog in alto
                    wlp.y = KeyboardUtils.getHeightDiff() - KeyboardUtils.getNavigationHeight(TaskListActivity.this);
                }else {             // Sposta dialog in basso
                    window.setGravity(Gravity.BOTTOM);
                    wlp.y = 0;
                }
                window.setAttributes(wlp);
            }
        });

        // Listener per il controllo del click al di fuori del dialog
        dialogAddModify.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // Visualizzazione di un semplice Dialog di conferma
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);
                builder.setCancelable(false);
                builder.setTitle("Attenzione");
                builder.setMessage("Vuoi cancellare le modifiche?");
                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogAddModify.dismiss();  // Dismiss dell'AddTask Dialog
                            }
                        });

                builder.setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

                AlertDialog cancelableDialog = builder.create();
                cancelableDialog.getWindow().getDecorView().getBackground().setColorFilter(TaskListActivity.this.getResources().getColor(R.color.cardview_dark_background), PorterDuff.Mode.SRC_ATOP);

                // Solamente se sono state apportate delle modifiche visualizzo i dialog corrispondenti
                if(hasChanged) {
                    dialogAddModify.show();
                    cancelableDialog.show();
                }
            }
        });
        dialogAddModify.show(); // Visualizzazione dell'AddTask Dialog
    }

    // Pulizia dei campi di inserimento, reset della data e della priorità
    public void resetField() {
        editTextTextMultiLine.setText("");
        btnDateTimePicker.setText("Oggi");
        changeDrawableColor(btnPriority, R.color.white);
        setBtnProjectSelector();
        hasChanged = false;
    }

    // Impostazione del bottone di selezione progetto
    public void setBtnProjectSelector() {
        // Impostazione del testo del bottone
        Project defaultP = projects.get(getProjectIdx(t.getIdProject()));
        btnProjectSelector.setText(defaultP.getNome());

        // Impostazione del colore e della icona del bottone
        Drawable img = ResourcesCompat.getDrawable(getResources(),
                getResources().getIdentifier(t.getIdProject() == 1 ? "drawable_left_project" : "round_background", "drawable", getPackageName()), null);

        img.setColorFilter(defaultP.getColore() == 0 ? getResources().getColor(R.color.colorPrimary) : defaultP.getColore(), PorterDuff.Mode.SRC_ATOP);
        img.setBounds(0, 0, 60, 60);
        btnProjectSelector.setCompoundDrawables(img, null, null, null);
    }

    // Cambiamento del colore del bottone di priorità
    public void changeDrawableColor(ImageButton btnImg, int color) {
        Drawable buttonDrawable = btnImg.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);
        DrawableCompat.setTint(buttonDrawable, TaskListActivity.this.getResources().getColor(color));
        btnPriority.setBackground(buttonDrawable);
    }

    @Override
    public void onBackPressed() {
        // Chiusura del Navigation Drawer laterale, se aperto
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    // Ottenimento delle task dal database
    private void getTasks() {
        // Recuper dele task dal database
        List<Task> retrivedTasks = db.getAllUserTasks(u.getEmail());
        if(retrivedTasks != null) {
            // Il FrameLayout di visualizzazione viene nascosto
            fragmentContainer.setVisibility(View.GONE);
            tasks.clear();                  // Pulizia della lista attuale
            tasks.addAll(retrivedTasks);    // Aggiunta delle task recuperate
        }
        else {
            // Viene visualizzato un messaggio per indicare l'assenza di task
            if(fragmentContainer.getVisibility() != View.VISIBLE) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, new NoTaskFragment())
                        .addToBackStack(null)
                        .commit();
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    // Ottenimento dei progetti dal database
    private void getProjects() {
        // Recupero progetti dal database
        List<Project> retrievedProjects = db.getAllUserProjects(u.getEmail());
        if(retrievedProjects != null) {
            projects.clear();                   // Pulizia della lista attuale
            projects.addAll(retrievedProjects); // Aggiunta dei progetti recuperati
        }
        else {
            // Aggiunta del progetto di default
            Project p = new Project(1, "Inbox", 0, 0, false, u.getEmail());
            db.addProject(p);
            projects.addAll(db.getAllUserProjects(u.getEmail()));
            Log.d("Errore", "L'ID dell'Inbox è = " + p.getIdProject());
        }
    }

    // Ottenimento del numero di task per ciascun progetto
    private ArrayList<Integer> getNumTasksPerProject() {
        ArrayList<Integer> numTasks = new ArrayList<>(Collections.nCopies(projects.size(), 0));
        List<Task> retrivedTasks = db.getAllUserTasks(u.getEmail());    // Recupero delle task dal database
        if(retrivedTasks != null) {
            for(Task t : retrivedTasks) {
                int idxProject = getProjectIdx(t.getIdProject());       // Ottenimento dell'indice del progetto corrispondente
                Log.d("Errore", t.getIdTask() + " task ha come contenuto = \"" + t.getContent() + "\" e il progetto è " + projects.get(idxProject).getIdProject() + " = " + projects.get(idxProject).getNome());
                numTasks.set(idxProject, numTasks.get(idxProject) + 1); // Calcolo del numero delle task per progetto
            }
        }
        return numTasks;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate del menu action bar, se presente
        getMenuInflater().inflate(R.menu.right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Si gestiscono qui i click degli elementi della actionbar.

        switch (item.getItemId()) { // Gestione delle modalità di sorting
            case R.id.alphabetic:   // Alfabetico crescente
            sortTask(1);
            break;

            case R.id.by_due_date:  // Per data crescente
            sortTask(2);
            break;

            case R.id.by_priority:  // Per priorità decrescente
            sortTask(3);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        // Quanto viene eseguito uno swipe refresh, viene aggiornata la visualizzazione delle task
        updateViewTasks(viewType.getText().toString());
    }

    @Override
    public void onIconClicked(int position) {
        // Se action mode non è ancora stata attivata, viene istanziata
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);  // Abilitazione della selezione
    }

    @Override
    public void onMessageRowClicked(int position) {
        // Si verifica se action mode è avilitata o no
        // Se è abilitata, si procede all'attivazione della modifica
        if (mAdapter.getSelectedItemCount() > 0) {
            // Se una task viene premuta viene abilitata l'action mode
            enableActionMode(position);
        } else {
            // Si procede alla modifica della task selezionata
            Task task = tasks.get(position);
            addModifyTask(task, false);
            mAdapter.notifyDataSetChanged();

        }
    }

    @Override
    public void onRowLongClicked(int position) {
        // Se viene premuto a lungo una task, viene abilitata l'action mode
        enableActionMode(position);
    }

    private void enableActionMode(int position) {
        // Se action mode non è ancora stata attivata, viene istanziata
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position); // Abilitazione della selezione
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {         // Non ci sono più elementi selezionati
            actionMode.finish();  // Terminazione dell'actionMode
        } else {
            // Aggiornamento grafico del numero di elementi selezionati
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d("Errore", "L'item selezionato è " + item.getItemId());

        item.setChecked(false);

        // Gestione delle diverse modalità di visualizzazione
        switch (item.getItemId()) {
            case R.id.nav_inbox:
                viewType.setText(item.getTitle());
                changeViewTasks(1, null, null); // Visualizzazione delle task in Inbox
                drawerLayout.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_today:
                viewType.setText(item.getTitle());
                changeViewTasks(2, Calendar.getInstance().getTime(), null); // Visualizzazione delle task odierne
                drawerLayout.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_upcoming:
                viewType.setText(item.getTitle());
                changeViewTasks(3, Calendar.getInstance().getTime(), null); // Visualizzazione delle task imminenti
                drawerLayout.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_setting:
                Intent intent = new Intent(TaskListActivity.this, SettingsActivity.class);
                // Passaggio dell'utente alla SettingsActivity
                intent.putExtra("key_object", u);
                startActivity(intent);
                break;
        }
        return true;
    }

    // Gestione della visualizzazione delle task a seconda della modalità impostata
    private void updateViewTasks(String param) {
        final String[] viewTypes = {"inbox", "odierni", "imminenti"};
        Log.d("Errore", "Il parametro vale: " + param);

        if(param.equalsIgnoreCase(viewTypes[0]))                // Inbox
            changeViewTasks(1, null, null);
        else if(param.equalsIgnoreCase(viewTypes[1]))           // Odierni
            changeViewTasks(2, Calendar.getInstance().getTime(), null);
        else if(param.equalsIgnoreCase(viewTypes[2]))           // Imminenti
            changeViewTasks(3, Calendar.getInstance().getTime(), null);
        else                                                    // Progetto
            changeViewTasks(4, null, param);
    }

    private void changeViewTasks(int mod, Date filter, String name) {
        // Ottenimento dell'indice del progetto selezionato
        int idx = name != null ? getProjectIdx(name) : 0;
        getTasks();                                     // Ottenimento task dal database
        ArrayList<Task> tempTasks = new ArrayList<>();  // Lista temporanea di task
        tempTasks.addAll(tasks);
        for (Task t : tempTasks) {
            switch (mod) {
                case 1:
                if(t.getIdProject() != 1)   // Rimozione task che non sono in Inbox
                    mAdapter.removeData(tasks.indexOf(t));
                break;

                case 2:
                if(!DateManipulation.areSameDate(t.getDueDate(), filter))   // Rimozione task non odierne
                    mAdapter.removeData(tasks.indexOf(t));
                break;

                case 3:
                if(t.getDueDate().before(filter))   // Rimozione task arretrate
                    mAdapter.removeData(tasks.indexOf(t));
                break;

                default:    // Rimozione delle task che non appartengono al progetto selezionato
                if(idx == 0 && !viewType.getText().toString().equalsIgnoreCase("inbox")) viewType.setText("Inbox");
                if(t.getIdProject() != projects.get(idx).getIdProject())
                    mAdapter.removeData(tasks.indexOf(t));
                break;
            }
        }

        // Sorting descrescente per data
        if(mod == 3) {
            Collections.sort(tasks, new Comparator<Task>() {
                public int compare(Task t1, Task t2) {
                    return t1.getDueDate().compareTo(t2.getDueDate());
                }
            });
        }

        mAdapter.notifyDataSetChanged();
        if(mAdapter.getItemCount() == 0) {
            // Visualizzazione del messaggio che comunica l'assenza di task
            if(fragmentContainer.getVisibility() != View.VISIBLE) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, new NoTaskFragment())
                        .addToBackStack(null)
                        .commit();
            }
        }
        else fragmentContainer.setVisibility(View.GONE);    // Il FrameLayout di visualizzazione viene nascosto
    }

    private void sortTask(int mod) {
        switch (mod) {
            case 1: // Ordinamento alfabetico crescente
            Collections.sort(tasks, new Comparator<Task>() {
                public int compare(Task t1, Task t2) {
                    return t1.getContent().compareTo(t2.getContent());
                }
            });
            break;

            case 2: // Ordinamento per data crescente
            Collections.sort(tasks, new Comparator<Task>() {
                public int compare(Task t1, Task t2) {
                    return t1.getDueDate().compareTo(t2.getDueDate());
                }
            });
            break;

            case 3: // Ordinamento per priorità decrescente
            Collections.sort(tasks, new Comparator<Task>() {
                public int compare(Task t1, Task t2) {
                    return t1.getPriority() - t2.getPriority();
                }
            });
            break;
        }
        mAdapter.notifyDataSetChanged();
    }

    // Ottenimento dell'indice del progetto dato il suo ID
    private int getProjectIdx(int id) {
        for(int i = 0; i < projects.size(); i++){
            if(id == projects.get(i).getIdProject())
                return i;
        }
        return 0;
    }

    // Ottenimento dell'indice del progetto dato il suo nome
    private int getProjectIdx(String name) {
        for(int i = 0; i < projects.size(); i++){
            Log.d("Errore", "Nome = " + name + " e progetto = " + projects.get(i).getNome());
            if(name.equalsIgnoreCase(projects.get(i).getNome().toLowerCase()))
                return i;
        }
        return 0;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        // Aggiornamento flag di modifica
        hasChanged = hasChanged ? hasChanged : t.getDueDate() != c.getTime();
        t.setDueDate(c.getTime());  // Memorizzazione della data

        // Aggiornamento grafico del bottone di selezione
        if(btnDateTimePicker != null) btnDateTimePicker.setText(datePicked(c));
    }

    private String datePicked(Calendar c) {
        String datePicked;
        // Controllo della data e impostazione del nome relativo
        if(DateManipulation.isToday(t.getDueDate())) datePicked = "Oggi";
        else if(DateManipulation.isYesterday(t.getDueDate())) datePicked = "Ieri";
        else if(DateManipulation.isTomorrow(t.getDueDate())) datePicked = "Domani";
        else if(DateManipulation.getWeekDay(t.getDueDate()) != null) datePicked = DateManipulation.getWeekDay(t.getDueDate());
        else datePicked = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());

        return datePicked;
    }

    private void updateDrawer() {
        // Aggiornamento del Navigation Header del Navigation Drawer
        MutateDrawerMenu.mutateNavHeader(TaskListActivity.this, findViewById(R.id.icon_image),
                findViewById(R.id.icon_text), findViewById(R.id.username), u);

        // Aggiornamento del numero di task completate
        TextView fulfilledTasks = findViewById(R.id.fulfilled_tasks);
        fulfilledTasks.setText(u.getTask() + "/" +
                getSharedPreferences(u.getEmail(), Context.MODE_PRIVATE).getInt("DAILY_TASKS", 0));
    }

    private class CustomDrawer implements DrawerLayout.DrawerListener {

        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            onResume();
            // Se il Navigation Drawer è aperto
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                // Ottenimento referenze oggetti grafici
                final MenuItem navProjects = navigationView.getMenu().findItem(R.id.nav_projects);
                FrameLayout rootViewProjects = (FrameLayout) navProjects.getActionView();
                FrameLayout frmLytManageProjects = (FrameLayout) rootViewProjects.findViewById(R.id.frmLytManage);
                FrameLayout frmLytAddProjects = (FrameLayout) rootViewProjects.findViewById(R.id.frmLytAdd);

                // Impostazione del click per la gestione dei progetti
                frmLytManageProjects.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageButton flipArrow = rootViewProjects.findViewById(R.id.flipArrow);

                        // Animazione da basso verso l'alto
                        if (flipArrow.getTag().equals("down")) {
                            Animation rotate = AnimationUtils.loadAnimation(TaskListActivity.this, R.anim.rotate_0_180);
                            flipArrow.startAnimation(rotate);
                            flipArrow.setTag("up");

                            // Apertura del fragment di gestione dei progetti
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                    .addToBackStack(null)
                                    .replace(R.id.nav_projects_fragment_container, new NavProjectsFragment(tasks, mAdapter, projects, getNumTasksPerProject(), u, fragmentContainer)).commit();
                        }
                        else {
                            Animation rotate = AnimationUtils.loadAnimation(TaskListActivity.this, R.anim.rotate_180_360);
                            flipArrow.startAnimation(rotate);
                            flipArrow.setTag("down");

                            // Apertura di un fragment vuoto
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                    .addToBackStack(null)
                                    .replace(R.id.nav_projects_fragment_container, new Fragment()).commit();
                        }
                    }
                });

                // Impostazione del click per l'aggiunta di un nuoovo progetto
                frmLytAddProjects.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TaskListActivity.this, AddProjectActivity.class);
                        // Passaggio dei dati per l'AddProjectActivity
                        intent.putExtra("key_object", u);
                        intent.putExtra("mode", "Add project");
                        Log.d("Errore", "L'utente " + u.getNome());
                        startActivityForResult(intent, PROJECT_LIST_CHANGED);
                    }
                });
            }
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {
            updateDrawer();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Aggiornamento dei progetti
        if (requestCode == PROJECT_LIST_CHANGED) {
            getProjects();
            Log.d("Errore", "Ho caricato i progetti");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProjects();
        updateViewTasks(viewType.getText().toString().toLowerCase());
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Aggiornamento dell'utente e del Navigation Drawer
            u = db.getUser(u.getEmail(), u.getPassword());
            if(drawerLayout.isDrawerOpen(GravityCompat.START))
                updateDrawer();

            final MenuItem navProjects = navigationView.getMenu().findItem(R.id.nav_projects);
            FrameLayout rootViewProjects = (FrameLayout) navProjects.getActionView();
            ImageButton flipArrow = rootViewProjects.findViewById(R.id.flipArrow);

            // Aggiornamento della lista di progetti
            if (flipArrow.getTag().equals("up")) {
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.nav_projects_fragment_container, new NavProjectsFragment(tasks, mAdapter, projects, getNumTasksPerProject(), u, fragmentContainer)).commit();
            }
        }
    }

    class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);

            // Viene disabilitato lo SwipeRefresh se action mode è abilitato
            swipeRefreshLayout.setEnabled(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_complete:
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        // Cancellazione grafica delle task selezionate
                        deleteTasks();
                        mode.finish();
                        // Messaggio task completata
                        Snackbar snackBar;
                        if(tempDeleted.size() == 1)
                            snackBar = Snackbar.make(findViewById(R.id.fab), tempDeleted.size() + " task è stata completata.", Snackbar.LENGTH_LONG);
                        else
                            snackBar = Snackbar.make(findViewById(R.id.fab), tempDeleted.size() + " task sono state completate.", Snackbar.LENGTH_LONG);
                        snackBar.setAction("ANNULLA", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                restoreTasks();

                            }
                        })
                                // Quando la Snackbar non è più visualizzata a schermo
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    List<Integer> deleteTasks = new ArrayList<>(); // Task da eliminare fisicamente
                                    for(Task dt : tempDeleted) deleteTasks.add(dt.getIdTask());
                                    db.deleteTasks(deleteTasks);    // Cancellazione fisica
                                    selectedItemPositions.clear();  // Pulizia della lista attuale
                                    u.setTask(u.getTask() + tempDeleted.size());
                                    db.updateUser(u);               // Aggiornamento dell'utente
                                    tempDeleted.clear();            // Pulizia della lista attuale
                                }
                            }
                        })
                        .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                        .show();
                    }
                });
                return true;

                case R.id.action_delete:
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        // Cancellazione grafica delle task selezionate
                        deleteTasks();
                        mode.finish();
                        // Messaggio task eliminate
                        Snackbar snackBar;
                        if(tempDeleted.size() == 1)
                            snackBar = Snackbar.make(findViewById(R.id.fab), tempDeleted.size() + " task è stata eliminata.", Snackbar.LENGTH_LONG);
                        else
                            snackBar = Snackbar.make(findViewById(R.id.fab), tempDeleted.size() + " task sono state eliminate.", Snackbar.LENGTH_LONG);
                        snackBar.setAction("ANNULLA", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                restoreTasks();
                            }
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    List<Integer> deleteTasks = new ArrayList<>();  // Task da eliminare fisicamente
                                    for(Task dt : tempDeleted) deleteTasks.add(dt.getIdTask());
                                    db.deleteTasks(deleteTasks);    // Cancellazione fisica
                                    selectedItemPositions.clear();  // Pulizia della lista attuale
                                    tempDeleted.clear();            // Pulizia della lista attuale
                                }
                            }
                        })
                        .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                        .show();
                    }
                });
                return true;

                default:
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Reset della actionMode e della recyclerView
            mAdapter.clearSelections();
            swipeRefreshLayout.setEnabled(true);
            actionMode = null;
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.resetAnimationIndex();
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    // Cancellazione logica delle Task dalla RecyclerView
    private void deleteTasks() {
        // Indici selezionati
        mAdapter.resetAnimationIndex();
        selectedItemPositions = mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            // Aggiunta delle task da eliminare in una lista temporanea e rimozione delle stesse
            tempDeleted.add(tasks.get(selectedItemPositions.get(i)));
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();
        if(mAdapter.getItemCount() == 0) {
            // Viene visualizzato un messaggio per indicare l'assenza di task
            if(fragmentContainer.getVisibility() != View.VISIBLE) {
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, new NoTaskFragment())
                        .addToBackStack(null)
                        .commit();
            }
        } else fragmentContainer.setVisibility(View.GONE);  // Il FrameLayout di visualizzazione viene nascosto
    }

    // Ripristino delle task nella RecyclerView
    private void restoreTasks() {
        mAdapter.resetAnimationIndex();
        for (int i = 0; i < selectedItemPositions.size(); i++) {
            // Aggiunta delle task eliminate nella lista corrente, nella stessa posizione di prima
            tasks.add(selectedItemPositions.get(i), tempDeleted.get(selectedItemPositions.size() - 1 - i));
        }
        // Pulizia della lista temporanea e della selezione
        selectedItemPositions.clear();
        tempDeleted.clear();
        mAdapter.notifyDataSetChanged();
        fragmentContainer.setVisibility(View.GONE);     // Il FrameLayout di visualizzazione viene nascosto
    }

}