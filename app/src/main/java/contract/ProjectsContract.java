package contract;

import android.provider.BaseColumns;

import model.Project;
import model.User;

public class ProjectsContract implements BaseColumns {
    // Attributi della tabella Projects
    public static final String TABLE_NAME = "Projects";
    public static final String ID_PROGETTO = "ID_Progetto";
    public static final String NOME = "Nome";
    public static final String COLORE = "Colore";
    public static final String ID_PARENT = "Parent";
    public static final String FAVORITE = "Favorite";
    public static final String EMAIL_UTENTE = "Email";

    // In modo tale da evitare che accidentalmente venga istanziato un contract della classe,
    // il costruttore viene reso privato.
    private ProjectsContract() {}

    // SQL statement per la creazione della tabella Projects
    public static final String CREATE_PROJECTS =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID_PROGETTO + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOME + " VARCHAR(255) NOT NULL, " +
                    COLORE + " INT NOT NULL, " +
                    ID_PARENT + " INTEGER NOT NULL REFERENCES " + ProjectsContract.TABLE_NAME + "(" + ProjectsContract.ID_PROGETTO + "), " +
                    FAVORITE + " INTEGER DEFAULT FALSE, " +
                    EMAIL_UTENTE + " VARCHAR(255) REFERENCES " + UsersContract.TABLE_NAME + "(" + UsersContract.EMAIL_UTENTE + ") " +
                    ");";

    // SQL statement per l'eliminazione della tabella Projects
    public static final String DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
