package fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.taskmanager.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import activity.AddProject;
import adapter.AlertListAdapter;
import adapter.ProjectListAdapter;
import model.ColorChoiceItem;
import model.Project;

public class NavProjectsFragment extends Fragment {
    List<Project> projects = new ArrayList<>();

    public NavProjectsFragment() { }

    public NavProjectsFragment(List<Project> projects) {
        this.projects = projects;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_drawer, container, false);

        ListView listView = view.findViewById(R.id.listView);

        ArrayList<ColorChoiceItem> itemList = new ArrayList<>();
        for(int i = 0; i < projects.size(); i++) {
            if(projects.get(i).getColore() != 0)
                itemList.add(new ColorChoiceItem(false, projects.get(i).getColore(), projects.get(i).getNome()));
            Log.d("Errore", "Colore: " + projects.get(i).getColore() + " e Progetto: " + projects.get(i).getNome());
        }

        ProjectListAdapter mAdapter = new ProjectListAdapter(itemList, view.getContext());
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ColorChoiceItem colorChoiceItem = itemList.get(position);
                colorChoiceItem.setSelected(true);
                itemList.set(position, colorChoiceItem);

                Toast.makeText(view.getContext(), "Ho premuto " +  itemList.get(position).getColorName(), Toast.LENGTH_LONG).show();
            }
        });
        mAdapter.notifyDataSetChanged();
        RelativeLayout relativeLayout = view.findViewById(R.id.relativeLayoutContainer);

        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) ((projects.size() + 2) * 40 * scale + 0.5f);

        RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, pixels);
        rel_btn.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        relativeLayout.setLayoutParams(rel_btn);
        relativeLayout.setScrollbarFadingEnabled(true);


        ConstraintLayout add = view.findViewById(R.id.constraintLayoutAdd);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddProject.class);
                startActivity(intent);
            }
        });

        ConstraintLayout manage = view.findViewById(R.id.constraintLayoutColor);
        manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    public void replaceFragment(Fragment newFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
