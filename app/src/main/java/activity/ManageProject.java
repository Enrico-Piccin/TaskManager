package activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.taskmanager.DatabaseHelper;
import com.example.taskmanager.R;

import java.util.ArrayList;
import java.util.List;

import adapter.AlertListAdapter;
import adapter.ManageProjectAdapter;
import model.ColorChoiceItem;
import model.Project;
import model.User;

public class ManageProject extends AppCompatActivity {

    private User u;
    private DatabaseHelper db;
    private List<Project> projects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_project);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;   // null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // show back button

        db = new DatabaseHelper(this);
        if(getIntent().hasExtra("key_object"))
            u = getIntent().getParcelableExtra("key_object");   // Recupero l'oggetto passato dalla precedente activity

        getProjects();

        ListView listView = findViewById(R.id.listView);
        ManageProjectAdapter mAdapter = new ManageProjectAdapter(projects, ManageProject.this);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ColorChoiceItem colorChoiceItem = itemList.get(position);
                colorChoiceItem.setSelected(true);
                itemList.set(position, colorChoiceItem);

                colorPosition[0] = position;
                colorName.setText(colorNames[position]);
                colorValue.setColorFilter(colorValues[position], PorterDuff.Mode.SRC_ATOP);

                alert.dismiss();
            }
        });
        builder.setView(customView);

        alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.show();
    }
    }

    private void getProjects() {
        List<Project> retrievedProjects = db.getAllUserProjects(u.getEmail());
        if(retrievedProjects != null)
            projects.addAll(retrievedProjects);
    }
}