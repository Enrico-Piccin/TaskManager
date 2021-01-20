package contract;

import android.provider.BaseColumns;

public class ProjectsContract implements BaseColumns {
    public static final String TABLE_NAME = "Projects";
    public static final String ID_PROGETTO = "ID_Progetto";
    public static final String NOME = "Nome";
    public static final String COLORE = "Colore";
    public static final String EMAIL_UTENTE = "Email";

    // In modo tale da evitare che accidentalmente venga istanziato un contract della classe,
    // il costruttore viene reso privato.
    private ProjectsContract() {}

    public static final String CREATE_PROJECTS =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID_PROGETTO + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOME + " VARCHAR(255) NOT NULL, " +
                    COLORE + " INT NOT NULL, " +
                    EMAIL_UTENTE + " VARCHAR(255) REFERENCES " + UsersContract.TABLE_NAME + "(" + UsersContract.EMAIL_UTENTE + ") " +
                    ");";

    public static final String DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
