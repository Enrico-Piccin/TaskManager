package activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.taskmanager.DatabaseHelper;
import com.example.taskmanager.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import adapter.AlertListAdapter;
import adapter.ProjectListAdapter;
import model.Project;
import model.User;

public class AddProjectActivity extends AppCompatActivity {

    private List<Project> projects;          // Lista dei progetti correnti
    private final int NUM_MAX_LETTERS = 120; // Numero massimo di lettere inseribili come nome del progetto
    private boolean enableAdd = false;       // Flag di abilitazione del bottone di invio
    private String mode = "";                // Modalità operativa dell'acitivy (add/modify)
    int idParent = 0;                        // ID del progetto padre di editProject
    Project editProject = null;              // L'oggetto Progetto da modificare
    AlertDialog alert = null;
    User u;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Abilito la freccia in alto a sinistra per ritornare alla precedente activitv
        assert getSupportActionBar() != null;                   // Controllo NULL
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Mostro il back button

        db = new DatabaseHelper(this);
        if(getIntent().hasExtra("key_object"))
            u = getIntent().getParcelableExtra("key_object");   // Recupero l'oggetto passato dalla precedente activity

        if(getIntent().hasExtra("mode"))
            mode = getIntent().getStringExtra("mode");          // Recupero la modalità operativa passata dalla precedente activity

        if(getIntent().hasExtra("edit_project"))
            editProject = getIntent().getParcelableExtra("edit_project");   // Recupero l'oggetto passato dalla precedente activity

        // Abilitazione della Toolbar
        TextView activityMode = toolbar.findViewById(R.id.addProject);
        activityMode.setText(mode);

        // Inizializzazione ed istanza della EditText del nome del progetto
        EditText editText = findViewById(R.id.edit_text);
        TextView countLetters = findViewById(R.id.countLetters);
        editText.requestFocus();

        // Caricamento del nome del progetto da editare
        if(editProject != null) editText.setText(editProject.getNome());

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // Controllo dell'abilitazione del bottone Send
                countLetters.setText(editText.getText().toString().length() + "/" + NUM_MAX_LETTERS);
                enableAdd = editText.getText().toString().length() < NUM_MAX_LETTERS;

                // Colorazione del countLetters a seconda del numero di caratteri
                if(!enableAdd)
                  countLetters.setTextColor(getResources().getColor(R.color.red));
                else
                    countLetters.setTextColor(getResources().getColor(R.color.teal_700));
            }
        });

        // Impostazione grafica della selezione del colore per il progetto
        ConstraintLayout constraintLayoutColor = findViewById(R.id.constraintLayoutColor);
        TextView colorName = findViewById(R.id.textViewColorName);
        ImageView colorValue = findViewById(R.id.imgProjectColor);

        // Impostazione grafica della selezione del padre del progetto
        ConstraintLayout constraintLayoutParent = findViewById(R.id.constraintLayoutParent);
        TextView parentName = findViewById(R.id.textViewParentName);
        ConstraintLayout constraintLayoutFavorite = findViewById(R.id.constraintLayoutFavorite);

        int[] colorValues = AddProjectActivity.this.getResources().getIntArray(R.array.mdcolor_500);            // Recupero array di colori
        String[] colorNames = AddProjectActivity.this.getResources().getStringArray(R.array.projectColorsName); // Recupero nome dei colori
        int[] colorPosition = {new Random().nextInt(colorValues.length)};   // Generazione di un indice di array casuale

        // Aggiornamento della grafica relativa al colore del progetto
        colorName.setText(colorNames[editProject != null ? getProjectColorPosition(editProject.getColore(), colorValues) : colorPosition[0]]);
        colorValue.setColorFilter(colorValues[editProject != null ? getProjectColorPosition(editProject.getColore(), colorValues) : colorPosition[0]], PorterDuff.Mode.SRC_ATOP);

        // Gestione dell'evento di selezione del colore
        constraintLayoutColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creazione di un AlertDialog con layout personalizzato
                AlertDialog.Builder builder = new AlertDialog.Builder(AddProjectActivity.this);
                builder.setCancelable(true);
                View customView = LayoutInflater.from(AddProjectActivity.this).inflate(
                        R.layout.list_view_color, null, false);
                ListView listView = customView.findViewById(R.id.listView);

                // Caricamento di un'array di colori
                ArrayList<Project> projects = new ArrayList<>();
                for(int i = 0; i < colorValues.length; i++)
                    projects.add(new Project(0, colorNames[i], colorValues[i], 0, false, u.getEmail()));

                AlertListAdapter mAdapter = new AlertListAdapter(projects, AddProjectActivity.this);
                listView.setAdapter(mAdapter);
                // Gestione dell'evento di click su uno degli item della lista
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Project p = projects.get(position);
                        projects.set(position, p);

                        // Memorizzazione della posizione, del colore e del nome scelti
                        colorPosition[0] = position;
                        colorName.setText(colorNames[position]);
                        colorValue.setColorFilter(colorValues[position], PorterDuff.Mode.SRC_ATOP);

                        alert.dismiss();    // Dismiss del dialog
                    }
                });
                builder.setView(customView);

                alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();   // Visualizzazione del dialog
            }
        });

        // Gestione dell'evento di selezione del padre del progetto
        constraintLayoutParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ottenimento di tutti i progetti correnti
                projects = db.getAllUserProjects(u.getEmail());

                // Creazione di un AlertDialog con layout personalizzato
                AlertDialog.Builder builder = new AlertDialog.Builder(AddProjectActivity.this);
                builder.setCancelable(true);
                View customView = LayoutInflater.from(AddProjectActivity.this).inflate(
                        R.layout.list_view_color, null, false);
                ListView listView = customView.findViewById(R.id.listView);

                // Caricamento di una lista di progetti
                ArrayList<Project> itemList = new ArrayList<>();
                for(int i = 1; i < projects.size(); i++) itemList.add(projects.get(i));

                // Impostazione del progetto padre del progetto da modificare
                if(editProject != null && editProject.getIdParent() != 0) {
                    parentName.setText(getProjectName(editProject.getIdParent()));
                    idParent = editProject.getIdParent();
                }

                ProjectListAdapter mAdapter = new ProjectListAdapter(itemList, new ArrayList<>(Collections.nCopies(projects.size(), 0)), AddProjectActivity.this, R.color.dark_card);
                listView.setAdapter(mAdapter);
                // Gestione dell'evento di click su uno degli item della lista
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Project p = itemList.get(position);
                        projects.set(position, p);

                        // Memorizzazione del progetto selezionato (con nome e id)
                        parentName.setText(p.getNome());
                        idParent = p.getIdProject();

                        alert.dismiss();    // Dismiss del dialog
                    }
                });
                builder.setView(customView);
                alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();   // Visualizzazione del dialog
            }
        });


        // Checkbox per indicare la preferenza del progetto
        CheckBox checkBoxFavorite = constraintLayoutFavorite.findViewById(R.id.checkBox);
        if(editProject != null) checkBoxFavorite.setChecked(editProject.isFavorite());

        // Gestione dell'evento di selezione della preferenza del progetto
        constraintLayoutFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBoxFavorite.setChecked(!checkBoxFavorite.isChecked()); // Cambio di stato
            }
        });

        ImageButton btnSend = toolbar.findViewById(R.id.btnSend);
        // Gestione dell'invio del progetto
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creazione dell'oggetto progetto con i dati inseriti
                Project p = new Project(editProject != null ? editProject.getIdProject() : 0, editText.getText().toString(), colorValues[colorPosition[0]], idParent, checkBoxFavorite.isChecked(), u.getEmail());
                if(mode.equalsIgnoreCase("add project"))
                    db.addProject(p);    // Aggiunta del progetto se in add mode
                else
                    db.updateProject(p); // Aggiornamento del progetto se in edit mode
                finish();
            }
        });
    }

    // Restituisce la posizione di un colore all'interno dell'array specificato
    private int getProjectColorPosition(int colore, int[] colorValues) {
        for(int i = 0; i < colorValues.length; i++) {
            if(colore == colorValues[i]) return i;
        }
        return 0;
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

    // Restituisce il nome di un progetto dato il suo id
    private String getProjectName(int id) {
        for(int i = 0; i < projects.size(); i++){
            if(id == projects.get(i).getIdProject())
                return projects.get(i).getNome();
        }
        return projects.get(0).getNome();
    }
}