package activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
import com.example.taskmanager.TaskListActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Random;

import adapter.AlertListAdapter;
import model.ColorChoiceItem;
import model.Project;
import model.User;

public class AddProject extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private final int NUM_MAX_LETTERS = 120;
    private boolean enableAdd = false;
    AlertDialog alert = null;
    User u;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;   // null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // show back button

        db = new DatabaseHelper(this);
        if(getIntent().hasExtra("key_object"))
            u = getIntent().getParcelableExtra("key_object");   // Recupero l'oggetto passato dalla precedente activity

        EditText editText = findViewById(R.id.edit_text);
        TextView countLetters = findViewById(R.id.countLetters);
        editText.requestFocus();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                countLetters.setText(editText.getText().toString().length() + "/" + NUM_MAX_LETTERS);
                enableAdd = editText.getText().toString().length() < NUM_MAX_LETTERS;

                if(!enableAdd)
                  countLetters.setTextColor(getResources().getColor(R.color.red));
                else
                    countLetters.setTextColor(getResources().getColor(R.color.teal_700));
            }
        });

        ConstraintLayout constraintLayoutColor = findViewById(R.id.constraintLayoutColor);
        TextView colorName = findViewById(R.id.textViewColorName);
        ImageView colorValue = findViewById(R.id.imgProjectColor);

        ConstraintLayout constraintLayoutParent = findViewById(R.id.constraintLayoutParent);
        ConstraintLayout constraintLayoutFavorite = findViewById(R.id.constraintLayoutFavorite);

        int[] colorValues = AddProject.this.getResources().getIntArray(R.array.mdcolor_500);
        String[] colorNames = AddProject.this.getResources().getStringArray(R.array.projectColorsName);
        int[] colorPosition = {new Random().nextInt(colorValues.length)};

        colorName.setText(colorNames[colorPosition[0]]);
        colorValue.setColorFilter(colorValues[colorPosition[0]], PorterDuff.Mode.SRC_ATOP);

        constraintLayoutColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddProject.this);
                builder.setCancelable(true);
                View customView = LayoutInflater.from(AddProject.this).inflate(
                        R.layout.list_view_color, null, false);
                ListView listView = customView.findViewById(R.id.listView);

                ArrayList<ColorChoiceItem> itemList = new ArrayList<>();
                for(int i = 0; i < colorValues.length; i++)
                    itemList.add(new ColorChoiceItem(false, colorValues[i], colorNames[i]));

                AlertListAdapter mAdapter = new AlertListAdapter(itemList, AddProject.this);
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
        });

        constraintLayoutFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBoxFavorite = constraintLayoutFavorite.findViewById(R.id.checkBox);
                checkBoxFavorite.setChecked(!checkBoxFavorite.isChecked());
            }
        });

        ImageButton btnSend = toolbar.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Project p = new Project(0, editText.getText().toString(), colorValues[colorPosition[0]], u.getEmail());
                db.addProject(p);
                finish();
            }
        });
    }

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
}