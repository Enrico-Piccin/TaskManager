package fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.taskmanager.R;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment {
    private Date initDate = null;   // Data iniziale di visualizzazione del DatePicker

    // Costruttore di default
    public DatePickerFragment() {}

    // Costruttore generico
    public DatePickerFragment(Date initDate) {
        this.initDate = initDate;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        if(initDate != null) c.setTime(initDate);   // Impostazione della data iniziale
        // Ritorno di un'istanza del del DatePickerDialog
        return new DatePickerDialog(getActivity(), R.style.DatePicker, (DatePickerDialog.OnDateSetListener) getActivity(), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }
}
