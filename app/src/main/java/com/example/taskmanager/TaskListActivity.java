package com.example.taskmanager;

import android.app.DatePickerDialog;
import android.content.Context;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import activity.AddProject;
import adapter.AlertListAdapter;
import adapter.KeyboardUtils;
import adapter.ProjectListAdapter;
import adapter.TaskAdapter;
import fragment.DatePickerFragment;
import fragment.NavProjectsFragment;
import helper.DateManipulation;
import helper.DividerItemDecoration;
import helper.MutateDrawerMenu;
import model.ColorChoiceItem;
import model.Project;
import model.Task;
import model.User;

public class TaskListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        DatePickerDialog.OnDateSetListener, SwipeRefreshLayout.OnRefreshListener, TaskAdapter.TaskAdapterListener {
    private static final int PROJECT_LIST_CHANGED = 0xe110;
    private List<Task> tasks = new ArrayList<>();
    private List<Task> tempDeleted = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private List<Integer> selectedItemPositions;
    private DatabaseHelper db;
    private RecyclerView recyclerView;
    private TaskAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private User u;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    Button btnDateTimePicker;
    EditText editTextTextMultiLine;
    ImageButton btnPriority;
    TextView viewType;
    Task t;
    AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewType = findViewById(R.id.view_type);

        db = new DatabaseHelper(this);
        if(getIntent().hasExtra("key_object"))
            u = getIntent().getParcelableExtra("key_object");   // Recupero l'oggetto passato dalla precedente activity

        // Log.d("Errore", "L'utente " + u.getNome() + " si è loggato la prima volta: " + new SimpleDateFormat("yyyy-MM-dd").format(u.getLastAccess()));

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        drawerLayout.addDrawerListener(new CustomDrawer());
        MutateDrawerMenu.mutateDrawerMenu(TaskListActivity.this, navigationView.getMenu());

        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            updateDrawer();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TaskListActivity.this);
                View layoutView = TaskListActivity.this.getLayoutInflater().inflate(R.layout.add_task, null);
                t = new Task(0, null, 4, Calendar.getInstance().getTime(), 0, u.getEmail());

                ImageButton btnSend = layoutView.findViewById(R.id.send);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        t.setIdProject(2);
                        t.setContent(editTextTextMultiLine.getText().toString());
                        t.setIdTask(db.addTask(t));
                        tasks.add(t);
                        mAdapter.notifyDataSetChanged();
                        resetField();
                        t = new Task(0, null, 4, Calendar.getInstance().getTime(), 0, u.getEmail());
                    }
                });

                ImageView btnSendBg = layoutView.findViewById(R.id.send_bg);
                editTextTextMultiLine = layoutView.findViewById(R.id.editTextTextMultiLine);
                editTextTextMultiLine.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(s.length() != 0) {
                            btnSendBg.setColorFilter(TaskListActivity.this.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                            btnSend.setEnabled(true);
                        }
                        else {
                            btnSend.setEnabled(false);
                            btnSendBg.setColorFilter(TaskListActivity.this.getResources().getColor(R.color.dark_card), PorterDuff.Mode.SRC_ATOP);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) { }
                });

                btnDateTimePicker = layoutView.findViewById(R.id.btnDateTimePicker);
                btnDateTimePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Configuration configuration = TaskListActivity.this.getResources().getConfiguration();
                        Locale locale = Locale.ITALY;
                        configuration.setLocale(locale);
                        configuration.setLayoutDirection(locale);
                        TaskListActivity.this.createConfigurationContext(configuration);
                        Locale.setDefault(locale);

                        DialogFragment datePicker = new DatePickerFragment(t.getDueDate());
                        datePicker.show(getSupportFragmentManager(), "Seleziona data");
                    }
                });

                Button btnProjectSelector = layoutView.findViewById(R.id.btnProjectSelector);
                btnProjectSelector.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);
                        builder.setCancelable(true);
                        View customView = LayoutInflater.from(TaskListActivity.this).inflate(
                                R.layout.list_view_color, null, false);
                        ListView listView = customView.findViewById(R.id.listView);

                        ArrayList<ColorChoiceItem> itemList = new ArrayList<>();
                        for(int i = 0; i < projects.size(); i++)
                            if(projects.get(i).getColore() != 0)
                                itemList.add(new ColorChoiceItem(false, projects.get(i).getColore(), projects.get(i).getNome()));
                            else
                                itemList.add(new ColorChoiceItem(false, getResources().getColor(R.color.colorPrimary), projects.get(i).getNome()));

                        ProjectListAdapter mAdapter = new ProjectListAdapter(itemList, TaskListActivity.this);
                        listView.setAdapter(mAdapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ColorChoiceItem colorChoiceItem = itemList.get(position);
                                colorChoiceItem.setSelected(true);
                                itemList.set(position, colorChoiceItem);

                                t.setIdProject(projects.get(position).getIdProject());
                                btnProjectSelector.setText(projects.get(position).getNome());

                                alert.dismiss();
                            }
                        });
                        builder.setView(customView);
                        alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
                });

                btnPriority = layoutView.findViewById(R.id.priority);
                btnPriority.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                                t.setPriority(1);
                                alert.dismiss();
                                changeDrawableColor(btnPriority, R.color.red);
                            }
                        });

                        dialogView.findViewById(R.id.p2).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                t.setPriority(2);
                                alert.dismiss();
                                changeDrawableColor(btnPriority, R.color.colorAccent);
                            }
                        });

                        dialogView.findViewById(R.id.p3).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                t.setPriority(3);
                                alert.dismiss();
                                changeDrawableColor(btnPriority, R.color.timestamp);
                            }
                        });

                        dialogView.findViewById(R.id.p4).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                t.setPriority(4);
                                alert.dismiss();
                                changeDrawableColor(btnPriority, R.color.teal_700);
                            }
                        });
                        alert.show();
                    }
                });

                dialogBuilder.setView(layoutView);
                AlertDialog alertDialog = dialogBuilder.create();

                Window window = alertDialog.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
                wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
                wlp.gravity = Gravity.BOTTOM;
                wlp.windowAnimations = R.style.AddTask;
                window.setAttributes(wlp);

                KeyboardUtils.addKeyboardToggleListener(TaskListActivity.this, new KeyboardUtils.SoftKeyboardToggleListener()
                {
                    @Override
                    public void onToggleSoftKeyboard(boolean isVisible)
                    {
                        if(isVisible) {
                           Log.d("Errore", "La tastiera vale " + KeyboardUtils.getHeightDiff());
                           Log.d("Errore", "La altezza vale " + KeyboardUtils.getNavigationHeight(TaskListActivity.this));

                           wlp.y = KeyboardUtils.getHeightDiff() - KeyboardUtils.getNavigationHeight(TaskListActivity.this);
                       }else {
                           Log.d("Errore", "La tastiera NON è visibile!");
                           window.setGravity(Gravity.BOTTOM);
                           wlp.y = 0;
                       }
                       window.setAttributes(wlp);
                    }
                });
                alertDialog.show();
            }
        });

        // Messaggio di benvenuto
        Snackbar snackBar = Snackbar.make(fab, "Benvenuto " + u.getNome(), Snackbar.LENGTH_LONG);
            snackBar.setAction("ANNULLA", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackBar.dismiss();
                }
            })
            .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
            .show();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        mAdapter = new TaskAdapter(this, tasks, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        actionModeCallback = new ActionModeCallback();

        // show loader and fetch tasks
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        getTasks();
                        getProjects();
                    }
                }
        );
    }

    public void resetField() {
        editTextTextMultiLine.setText("");
        btnDateTimePicker.setText("Oggi");
        changeDrawableColor(btnPriority, R.color.white);
    }

    public void changeDrawableColor(ImageButton btnImg, int color) {
        Drawable buttonDrawable = btnImg.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);
        DrawableCompat.setTint(buttonDrawable, TaskListActivity.this.getResources().getColor(color));
        btnPriority.setBackground(buttonDrawable);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    private void getTasks() {
        List<Task> retrivedTasks = db.getAllUserTasks(u.getEmail());
        if(retrivedTasks != null)
            tasks.addAll(retrivedTasks);
        mAdapter.notifyDataSetChanged();
    }

    private void getProjects() {
        List<Project> retrievedProjects = db.getAllUserProjects(u.getEmail());
        if(retrievedProjects != null)
            projects.addAll(retrievedProjects);
        else {
            Project p = new Project(0, "Inbox", 0, u.getEmail());
            p.setIdProject(db.addProject(p));
            projects.add(p);
        }
    }

/*    private void getInbox() {
        swipeRefreshLayout.setRefreshing(true);

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<List<Message>> call = apiService.getInbox();
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                // clear the inbox
                messages.clear();

                // add all the messages
                // messages.addAll(response.body());

                // TODO - avoid looping
                // the loop was performed to add colors to each message
                for (Message message : response.body()) {
                    // generate a random color
                    message.setColor(getRandomMaterialColor("400"));
                    messages.add(message);
                }

                mAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to fetch json: " + t.getMessage(), Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
*/

    /**
     * chooses a random color from array.xml
     */
/*    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            Toast.makeText(getApplicationContext(), "Search...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        // swipe refresh is performed, fetch the messages again
        // getInbox();
    }

    @Override
    public void onIconClicked(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }
/*
    @Override
    public void onIconImportantClicked(int position) {
        // Star icon is clicked,
        // mark the message as important
        Task task = messages.get(position);
        message.setImportant(!message.isImportant());
        messages.set(position, message);
        mAdapter.notifyDataSetChanged();
    }
*/
    @Override
    public void onMessageRowClicked(int position) {
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        if (mAdapter.getSelectedItemCount() > 0) {
            enableActionMode(position);
        } else {
            // read the message which removes bold from the row
            Task task = tasks.get(position);
            // task.setRead(true);
            tasks.set(position, task);
            mAdapter.notifyDataSetChanged();

            Toast.makeText(getApplicationContext(), "Selezionata: " + task.getContent(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRowLongClicked(int position) {
        // long press is performed, enable action mode
        enableActionMode(position);
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d("Errore", "L'item selezionato è " + item.getItemId());

        item.setChecked(false);
        viewType.setText(item.getTitle());

        switch (item.getItemId()) {
            case R.id.nav_inbox:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new NavProjectsFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_today:
                break;

            case R.id.nav_upcoming:
                break;

            case R.id.nav_projects:
                break;

            case R.id.nav_filters:
                break;

            case R.id.nav_setting:
                break;
        }

        Log.d("Errore", "Ciaooo ");
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        t.setDueDate(c.getTime());

        String datePicked;
        if(DateManipulation.isToday(t.getDueDate())) datePicked = "Oggi";
        else if(DateManipulation.isYesterday(t.getDueDate())) datePicked = "Ieri";
        else if(DateManipulation.isTomorrow(t.getDueDate())) datePicked = "Domani";
        else if(DateManipulation.getWeekDay(t.getDueDate()) != null) datePicked = DateManipulation.getWeekDay(t.getDueDate());
        else datePicked = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());

        if(btnDateTimePicker != null) btnDateTimePicker.setText(datePicked);
    }

    private void updateDrawer() {
        MutateDrawerMenu.mutateNavHeader(TaskListActivity.this, findViewById(R.id.icon_image),
                findViewById(R.id.icon_text), findViewById(R.id.username), u);

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
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {

                final MenuItem navProjects = navigationView.getMenu().findItem(R.id.nav_projects);
                final MenuItem navFilters = navigationView.getMenu().findItem(R.id.nav_filters);

                FrameLayout rootViewProjects = (FrameLayout) navProjects.getActionView();
                FrameLayout rootViewFilters = (FrameLayout) navFilters.getActionView();

                FrameLayout frmLytManageProjects = (FrameLayout) rootViewProjects.findViewById(R.id.frmLytManage);
                FrameLayout frmLytAddProjects = (FrameLayout) rootViewProjects.findViewById(R.id.frmLytAdd);
                FrameLayout frmLytManageFilters = (FrameLayout) rootViewFilters.findViewById(R.id.frmLytManage);
                FrameLayout frmLytAddFilters = (FrameLayout) rootViewFilters.findViewById(R.id.frmLytAdd);

                frmLytManageProjects.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageButton flipArrow = rootViewProjects.findViewById(R.id.flipArrow);

                        if (flipArrow.getTag().equals("down")) {
                            Animation rotate = AnimationUtils.loadAnimation(TaskListActivity.this, R.anim.rotate_0_180);
                            flipArrow.startAnimation(rotate);
                            flipArrow.setTag("up");

                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                    .addToBackStack(null)
                                    .replace(R.id.nav_projects_fragment_container, new NavProjectsFragment(projects)).commit();
                        }
                        else {
                            Animation rotate = AnimationUtils.loadAnimation(TaskListActivity.this, R.anim.rotate_180_360);
                            flipArrow.startAnimation(rotate);
                            flipArrow.setTag("down");

                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                    .addToBackStack(null)
                                    .replace(R.id.nav_projects_fragment_container, new Fragment()).commit();
                        }
                    }
                });

                frmLytAddProjects.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TaskListActivity.this, AddProject.class);
                        intent.putExtra("key_object", u);
                        startActivityForResult(intent, PROJECT_LIST_CHANGED);
                        if(drawerLayout.isDrawerOpen(GravityCompat.START))
                            drawerLayout.closeDrawer(GravityCompat.START);
                    }
                });

                frmLytManageFilters.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageButton flipArrow = rootViewFilters.findViewById(R.id.flipArrow);

                        if (flipArrow.getTag().equals("down")) {
                            Animation rotate = AnimationUtils.loadAnimation(TaskListActivity.this, R.anim.rotate_0_180);
                            flipArrow.startAnimation(rotate);
                            flipArrow.setTag("up");

                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                    .addToBackStack(null)
                                    .replace(R.id.nav_projects_fragment_container, new NavProjectsFragment()).commit();
                        }
                        else {
                            Animation rotate = AnimationUtils.loadAnimation(TaskListActivity.this, R.anim.rotate_180_360);
                            flipArrow.startAnimation(rotate);
                            flipArrow.setTag("down");

                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                    .addToBackStack(null)
                                    .replace(R.id.nav_projects_fragment_container, new Fragment()).commit();
                        }
                    }
                });

                frmLytAddFilters.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TaskListActivity.this, AddProject.class);
                        startActivity(intent);
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

        if (requestCode == PROJECT_LIST_CHANGED) {
            getProjects();
            Log.d("Errore", "Ho caricato i progetti");
        }
    }


    class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);

            // disable swipe refresh if action mode is enabled
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
                case R.id.action_delete:
                    // delete all the selected messages
                    deleteTasks();
                    mode.finish();
                    // Messaggio di benvenuto
                    Snackbar snackBar = Snackbar.make(findViewById(R.id.fab), tempDeleted.size() + " task sono state eliminate.", Snackbar.LENGTH_LONG);
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
                                List<Integer> deleteTasks = new ArrayList<>();
                                for(Task dt : tasks) deleteTasks.add(dt.getIdTask());
                                db.deleteTasks(deleteTasks);
                                selectedItemPositions.clear();
                                tempDeleted.clear();
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

    // deleting tasks from recycler view
    private void deleteTasks() {
        mAdapter.resetAnimationIndex();
        selectedItemPositions = mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            tempDeleted.add(tasks.get(selectedItemPositions.get(i)));
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }

    // restoring tasks in recycler view
    private void restoreTasks() {
        mAdapter.resetAnimationIndex();
        for (int i = 0; i < selectedItemPositions.size(); i++) {
            tasks.add(selectedItemPositions.get(i), tempDeleted.get(selectedItemPositions.size() - 1 - i));
        }
        selectedItemPositions.clear();
        tempDeleted.clear();
        mAdapter.notifyDataSetChanged();
    }

}