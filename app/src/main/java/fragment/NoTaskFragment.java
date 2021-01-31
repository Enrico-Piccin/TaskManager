package fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taskmanager.R;

public class NoTaskFragment extends Fragment {

    public NoTaskFragment() {
        // Viene richiesto un costruttore di default vuoto
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // L'inflate del layout personalizzato
        return inflater.inflate(R.layout.fragment_no_task, container, false);
    }
}