package contract;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import java.util.Date;

public final class UsersContract implements BaseColumns{
    public static final String TABLE_NAME = "Utenti";
    public static final String NOME_UTENTE = "Nome";
    public static final String EMAIL_UTENTE = "Email";
    public static final String TEL_UTENTE = "Telefono";
    public static final String PSW_UTENTE = "Password";
    public static final String COLORE_UTENTE = "Colore";
    public static final String TASK_COMPLETATE = "Task";
    public static final String LAST_ACCESS = "UltimoAccesso";

    // In modo tale da evitare che accidentalmente venga istanziato un contract della classe,
    // il costruttore viene reso privato.
    private UsersContract() {}

    public static final String CREATE_USERS =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    EMAIL_UTENTE + " VARCHAR(255) PRIMARY KEY," +
                    PSW_UTENTE + " VARBINARY(255) NOT NULL, " +
                    NOME_UTENTE + " VARCHAR(255), " +
                    TEL_UTENTE + " CHAR(10), " +
                    COLORE_UTENTE + " INTEGER, " +
                    TASK_COMPLETATE + " INTEGER DEFAULT 0, " +
                    LAST_ACCESS + " DATE NOT NULL" +
            ");";

    public static final String DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
