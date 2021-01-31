package com.example.taskmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import contract.ProjectsContract;
import contract.TasksContract;
import contract.UsersContract;
import model.Project;
import model.Task;
import model.User;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "UserTaskManager.db";

    // Costruttore generico
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UsersContract.CREATE_USERS);         // Creazione tabella Users
        db.execSQL(TasksContract.CREATE_TASKS);         // Creazione tabella Tasks
        db.execSQL(ProjectsContract.CREATE_PROJECTS);   // Creazione tabella Projects
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Questo database è solo una cache per i dati online, quindi la sua politica di
        // aggiornamento è semplicemente quella di scartare i dati e ricominciare da capo
        db.execSQL(UsersContract.DELETE_ENTRIES);
        db.execSQL(TasksContract.DELETE_ENTRIES);
        db.execSQL(ProjectsContract.DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // Aggiunta di un nuovo User
    public void addUser(User u) {
        // Ottenimento del repository dei dati in modalità di scrittura
        SQLiteDatabase db = this.getWritableDatabase();

        // Viene creata una nuova mappa dei valori, dove il nome della colonna è la chiave
        ContentValues values = new ContentValues();
        values.put(UsersContract.EMAIL_UTENTE, u.getEmail());
        values.put(UsersContract.NOME_UTENTE, u.getNome());
        values.put(UsersContract.TEL_UTENTE, u.getTelefono());
        values.put(UsersContract.PSW_UTENTE, u.getPassword());
        values.put(UsersContract.COLORE_UTENTE, u.getColore());
        values.put(UsersContract.TASK_COMPLETATE, u.getTask());
        values.put(UsersContract.LAST_ACCESS, new SimpleDateFormat("yyyy-MM-dd").format(u.getLastAccess()));

        // Viene inserita la nuova riga
        db.insert(UsersContract.TABLE_NAME, null, values);
        db.close();
    }

    // Aggiunta di una nuova Task
    public int addTask(Task t) {
        // Ottenimento del repository dei dati in modalità di scrittura
        SQLiteDatabase db = this.getWritableDatabase();

        // Viene creata una nuova mappa dei valori, dove il nome della colonna è la chiave
        ContentValues values = new ContentValues();
        values.put(TasksContract.CONTENUTO, t.getContent());
        values.put(TasksContract.PRIORITA, t.getPriority());
        values.put(TasksContract.DATA, new SimpleDateFormat("yyyy-MM-dd").format(t.getDueDate()));
        values.put(TasksContract.ID_PROGETTO, t.getIdProject());
        values.put(TasksContract.EMAIL_UTENTE, t.getEmail());

        // Viene inserita la nuova riga
        db.insert(TasksContract.TABLE_NAME, null, values);
        db.close();

        return getHighestID();
    }

    // Aggiunta di un nuovo Project
    public int addProject(Project p) {
        // Ottenimento del repository dei dati in modalità di scrittura
        SQLiteDatabase db = this.getWritableDatabase();

        // Viene creata una nuova mappa dei valori, dove il nome della colonna è la chiave
        ContentValues values = new ContentValues();
        values.put(ProjectsContract.NOME, p.getNome());
        values.put(ProjectsContract.COLORE, p.getColore());
        values.put(ProjectsContract.ID_PARENT, p.getIdParent());
        values.put(ProjectsContract.FAVORITE, p.isFavorite() ? 1 : 0);
        values.put(ProjectsContract.EMAIL_UTENTE, p.getEmail());

        // Viene inserita la nuova riga
        db.insert(ProjectsContract.TABLE_NAME, null, values);
        db.close();

        return getHighestID();
    }

    public User getUser(String email, String psw) {
        // Ottenimento del repository dei dati in modalità di lettura
        SQLiteDatabase db = this.getReadableDatabase();

        // Filtro SELECT per il result set WHERE EMAIL_UTENTE = email AND PSW_UTENTE = psw;
        String select = UsersContract.EMAIL_UTENTE + "=?" + " AND " + UsersContract.PSW_UTENTE + "=?";
        String[] selectArg = { email, psw };

        Log.d("Errore", "La e-mail di ricerca è " + email + " e la password di ricerca è " + psw);

        Cursor cursor = db.query(
                UsersContract.TABLE_NAME,       // La tabella da interrogare (FROM)
                null,                   // Le colonne da proiettare (null = *, ossia tutte) (SELECT)
                select,                         // Le colonne oggetto di filtraggio nella clausola WHERE
                selectArg,                      // I valori di filtraggio nella clausola WHERE
                null,                   // Non viene specificato alcun raggruppamento (GROUP BY)
                null,                    // Non viene specificato nessun filtro di raggruppamento (HAVING)
                null                    // Non viene specificato nessun ordinamento (ORDER BY)
        );

        if(!cursor.moveToFirst())   // Controllo della presenza di dati letti
            return null;
        else {
            User u = new User(cursor.getString(2),
                    cursor.getString(0), cursor.getString(3), cursor.getString(1), cursor.getInt(4),
                    cursor.getInt(5), null);

            try { u.setLastAccess(new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(6))); }
            catch (ParseException e) { e.printStackTrace(); }

            return u;   // Ritorno dello User presente nel Database
        }
    }

    public ArrayList<Task> getAllUserTasks(String email) {
        // Ottenimento del repository dei dati in modalità di lettura
        SQLiteDatabase db = this.getReadableDatabase();
        final int NUM_FIELDS = 6;
        ArrayList<Task> lstTask = new ArrayList<>();

        // Filtro SELECT per il result set WHERE EMAIL_UTENTE = email;
        String select = TasksContract.EMAIL_UTENTE + "=?";
        String[] selectArg = { email };

        Cursor cursor = db.query(
                TasksContract.TABLE_NAME,       // La tabella da interrogare (FROM)
                null,                   // Le colonne da proiettare (null = *, ossia tutte) (SELECT)
                select,                         // Le colonne oggetto di filtraggio nella clausola WHERE
                selectArg,                      // I valori di filtraggio nella clausola WHERE
                null,                   // Non viene specificato alcun raggruppamento (GROUP BY)
                null,                    // Non viene specificato nessun filtro di raggruppamento (HAVING)
                null                    // Non viene specificato nessun ordinamento (ORDER BY)
        );

        if(!cursor.moveToFirst())   // Controllo della presenza di dati letti
            return null;
        else {
            Task t = new Task();
            for(int i = 0; i < cursor.getCount() * NUM_FIELDS; i++) {
                switch (i % NUM_FIELDS) {
                    case 0:     // ID
                    t = new Task(); // Creazione di una nuova Task
                    t.setIdTask(cursor.getInt(i % NUM_FIELDS));
                    break;

                    case 1:     // Contenuto
                    t.setContent(cursor.getString(i % NUM_FIELDS));
                    break;

                    case 2:     // Priorità
                    t.setPriority(cursor.getInt(i % NUM_FIELDS));
                    break;

                    case 3:     // Due date
                    try {
                        t.setDueDate(new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(i % NUM_FIELDS)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;

                    case 4:     // Email
                    t.setEmail(cursor.getString(i % NUM_FIELDS));
                    break;

                    case 5:     // IDProject
                    t.setIdProject(cursor.getInt(i % NUM_FIELDS));
                    lstTask.add(t);     // Aggiunta della Task alla lista
                    cursor.moveToNext();
                    break;
                }
            }
            return lstTask; // Ritorno della lista di Task
        }
    }

    public ArrayList<Project> getAllUserProjects(String email) {
        // Ottenimento del repository dei dati in modalità di lettura
        SQLiteDatabase db = this.getReadableDatabase();
        final int NUM_FIELDS = 6;
        ArrayList<Project> lstProject = new ArrayList<>();

        // Filtro SELECT per il result set WHERE EMAIL_UTENTE = email;
        String select = ProjectsContract.EMAIL_UTENTE + "= ?";
        String[] selectArg = { email };

        Cursor cursor = db.query(
                ProjectsContract.TABLE_NAME,    // La tabella da interrogare (FROM)
                null,                   // Le colonne da proiettare (null = *, ossia tutte) (SELECT)
                select,                         // Le colonne oggetto di filtraggio nella clausola WHERE
                selectArg,                      // I valori di filtraggio nella clausola WHERE
                null,                   // Non viene specificato alcun raggruppamento (GROUP BY)
                null,                    // Non viene specificato nessun filtro di raggruppamento (HAVING)
                null                    // Non viene specificato nessun ordinamento (ORDER BY)
        );

        if(!cursor.moveToFirst())   // Controllo della presenza di dati letti
            return null;
        else {
            Project p = new Project();
            for(int i = 0; i < cursor.getCount() * NUM_FIELDS; i++) {
                switch (i % NUM_FIELDS) {
                    case 0:     // ID
                        p = new Project();  // Creazione nuovo progetto
                        p.setIdProject(cursor.getInt(i % NUM_FIELDS));
                        break;

                    case 1:     // Nome
                        p.setNome(cursor.getString(i % NUM_FIELDS));
                        break;

                    case 2:     // Colore
                        p.setColore(cursor.getInt(i % NUM_FIELDS));
                        break;

                    case 3:     // IDParent
                        p.setIdParent(cursor.getInt(i % NUM_FIELDS));
                        break;

                    case 4:     // Favorite
                        p.setFavorite(cursor.getInt(i % NUM_FIELDS) == 1);
                        break;

                    case 5:     // Email
                        p.setEmail(cursor.getString(i % NUM_FIELDS));
                        lstProject.add(p);  // Aggiunta del Project alla lista
                        cursor.moveToNext();
                        break;
                }
            }
            return lstProject;  // Ritorno della lista di progetti
        }
    }

    public void updateUser(User u) {
        SQLiteDatabase db  = this.getWritableDatabase();

        // Filtro SELECT per il result set WHERE EMAIL_UTENTE = u.getEmail();
        String select = UsersContract.EMAIL_UTENTE + " = ?";

        // Argomento della clausola WHERE
        String[] selectionArgs = new String[] { u.getEmail() };

        // Viene creata una nuova mappa dei valori, dove il nome della colonna è la chiave
        ContentValues values = new ContentValues();
        values.put(UsersContract.NOME_UTENTE, u.getNome());
        values.put(UsersContract.EMAIL_UTENTE, u.getEmail());
        values.put(UsersContract.TEL_UTENTE, u.getTelefono());
        values.put(UsersContract.PSW_UTENTE, u.getPassword());
        values.put(UsersContract.COLORE_UTENTE, u.getColore());
        values.put(UsersContract.TASK_COMPLETATE, u.getTask());
        values.put(UsersContract.LAST_ACCESS, new SimpleDateFormat("yyyy-MM-dd").format(u.getLastAccess()));

        // Esecuzione dell'aggiornamento
        db.update(UsersContract.TABLE_NAME, values, select, selectionArgs);
        db.close();
    }

    public void updateTask(Task t) {
        SQLiteDatabase db  = this.getWritableDatabase();

        // Filtro SELECT per il result set WHERE TASK_ID = p.taskID();
        String select = TasksContract.ID_TASK + " = ?";

        // Argomento della clausola WHERE
        String[] selectionArgs = new String[] { t.getIdTask() + "" };

        // Viene creata una nuova mappa dei valori, dove il nome della colonna è la chiave
        ContentValues values = new ContentValues();
        values.put(TasksContract.CONTENUTO, t.getContent());
        values.put(TasksContract.PRIORITA, t.getPriority());
        values.put(TasksContract.DATA, new SimpleDateFormat("yyyy-MM-dd").format(t.getDueDate()));
        values.put(TasksContract.EMAIL_UTENTE, t.getEmail());
        values.put(TasksContract.ID_PROGETTO, t.getIdProject());

        Log.d("Errore", "La task aggiornata ha questo id progetto = " + t.getIdProject());

        // Esecuzione dell'aggiornamento
        db.update(TasksContract.TABLE_NAME, values, select, selectionArgs);
        db.close();
    }

    public void updateProject(Project p) {
        SQLiteDatabase db  = this.getWritableDatabase();

        // Filtro SELECT per il result set WHERE ID_PROGETTO = p.getIdProject();
        String select = ProjectsContract.ID_PROGETTO + " = ?";

        // Argomento della clausola WHERE
        String[] selectionArgs = new String[] { p.getIdProject() + "" };

        // Viene creata una nuova mappa dei valori, dove il nome della colonna è la chiave
        ContentValues values = new ContentValues();
        values.put(ProjectsContract.NOME, p.getNome());
        values.put(ProjectsContract.COLORE, p.getColore());
        values.put(ProjectsContract.ID_PARENT, p.getIdParent());
        values.put(ProjectsContract.FAVORITE, p.isFavorite() ? 1 : 0);
        values.put(ProjectsContract.EMAIL_UTENTE, p.getEmail());

        // Esecuzione dell'aggiornamento
        db.update(ProjectsContract.TABLE_NAME, values, select, selectionArgs);
        db.close();
    }

    public int deleteTasks(List<Integer> deletedTasks) {
        SQLiteDatabase db  = this.getWritableDatabase();

        // Filtro SELECT per il result set WHERE ID_TASK IN lista;
        String select = TasksContract.ID_TASK + " IN ";

        // Argomento della clausola WHERE di tipo IN ("...")
        boolean after_first_string = false;
        StringBuffer sb = new StringBuffer("(");
        for(int s : deletedTasks) {
            if (after_first_string) {
                sb.append(",");
            }
            after_first_string = true;
            sb.append("'").append((s+"").replace("'","''")).append("'");
        }
        sb.append(")");

        // Esecuzione dell'eliminazione
        int deletedRows = db.delete(TasksContract.TABLE_NAME,
                select + sb.toString(), null);
        db.close();
        return deletedRows;
    }

    public int deleteProjects(List<Integer> deletedProjects) {
        SQLiteDatabase db  = this.getWritableDatabase();

        // Filtro SELECT per il result set WHERE ID_PROGETTO IN lista;
        String select = ProjectsContract.ID_PROGETTO + " IN ";

        // Argomento della clausola WHERE di tipo IN ("...")
        boolean after_first_string = false;
        StringBuffer sb = new StringBuffer("(");
        for(int s : deletedProjects) {
            if (after_first_string) {
                sb.append(",");
            }
            after_first_string = true;
            sb.append("'").append((s+"").replace("'","''")).append("'");
        }
        sb.append(")");

        // Esecuzione dell'eliminazione
        int deletedRows = db.delete(ProjectsContract.TABLE_NAME,
                select + sb.toString(), null);
        db.close();
        return deletedRows;
    }

    // Ritorna l'ultimo ID inserito con formula AUTOINCREMENT
    public int getHighestID() {
        final String MY_QUERY = "SELECT last_insert_rowid()";
        Cursor cur = this.getWritableDatabase().rawQuery(MY_QUERY, null);
        cur.moveToFirst();
        int ID = cur.getInt(0);
        Log.d("Errore", "L'ultimo id inserito è " + ID);
        cur.close();
        return ID;
    }
}
