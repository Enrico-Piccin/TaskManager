package contract;

import android.provider.BaseColumns;

public final class TasksContract implements BaseColumns {
    public static final String TABLE_NAME = "Tasks";
    public static final String ID_TASK = "ID_Task";
    public static final String CONTENUTO = "Contenuto";
    public static final String PRIORITA = "Priorita";
    public static final String DATA = "Data";
    public static final String EMAIL_UTENTE = "Email";
    public static final String ID_PROGETTO = "ID_Progetto";

    // In modo tale da evitare che accidentalmente venga istanziato un contract della classe,
    // il costruttore viene reso privato.
    private TasksContract() {}

    public static final String CREATE_TASKS =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID_TASK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CONTENUTO + " TEXT NOT NULL, " +
                    PRIORITA + " SMALLINT NOT NULL, " +
                    DATA + " DATE, " +
                    EMAIL_UTENTE + " VARCHAR(255) REFERENCES " + UsersContract.TABLE_NAME + "(" + UsersContract.EMAIL_UTENTE + "), " +
                    ID_PROGETTO + " SMALLINT REFERENCES " + ProjectsContract.TABLE_NAME + "(" + ProjectsContract.ID_PROGETTO + ") " +
                    ");";

    public static final String DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
