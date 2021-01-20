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
import java.util.Date;
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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UsersContract.CREATE_USERS);
        db.execSQL(TasksContract.CREATE_TASKS);
        db.execSQL(ProjectsContract.CREATE_PROJECTS);
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

    // Aggiunta di un nuovo utente
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

    // Aggiunta di una nuova task
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

        Log.d("Errore", "La task inserita è " + t.getContent());

        return getHighestID();
    }

    // Aggiunta di un nuovo progetto
    public int addProject(Project p) {
        // Ottenimento del repository dei dati in modalità di scrittura
        SQLiteDatabase db = this.getWritableDatabase();

        // Viene creata una nuova mappa dei valori, dove il nome della colonna è la chiave
        ContentValues values = new ContentValues();
        values.put(ProjectsContract.NOME, p.getNome());
        values.put(ProjectsContract.COLORE, p.getColore());
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

            return u;
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
                    case 0:
                    t = new Task();
                    t.setIdTask(cursor.getInt(i % NUM_FIELDS));
                    break;

                    case 1:
                    t.setContent(cursor.getString(i % NUM_FIELDS));
                    break;

                    case 2:
                    t.setPriority(cursor.getInt(i % NUM_FIELDS));
                    break;

                    case 3:
                    try {
                        t.setDueDate(new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(i % NUM_FIELDS)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;

                    case 4:
                    t.setIdProject(cursor.getInt(i % NUM_FIELDS));
                    break;

                    case 5:
                    t.setEmail(cursor.getString(i % NUM_FIELDS));
                    lstTask.add(t);
                    cursor.moveToNext();
                    break;
                }
            }
            return lstTask;
        }
    }

    public ArrayList<Project> getAllUserProjects(String email) {
        // Ottenimento del repository dei dati in modalità di lettura
        SQLiteDatabase db = this.getReadableDatabase();
        final int NUM_FIELDS = 4;
        ArrayList<Project> lstProject = new ArrayList<>();

        // Filtro SELECT per il result set WHERE EMAIL_UTENTE = email;
        String select = ProjectsContract.EMAIL_UTENTE + "=?";
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
                    case 0:
                        p = new Project();
                        p.setIdProject(cursor.getInt(i % NUM_FIELDS));
                        break;

                    case 1:
                        p.setNome(cursor.getString(i % NUM_FIELDS));
                        break;

                    case 2:
                        p.setColore(cursor.getInt(i % NUM_FIELDS));
                        break;

                    case 3:
                        p.setEmail(cursor.getString(i % NUM_FIELDS));
                        lstProject.add(p);
                        cursor.moveToNext();
                        break;
                }
            }
            return lstProject;
        }
    }

    public int deleteTasks(List<Integer> deletedTasks) {
        SQLiteDatabase db  = this.getWritableDatabase();

        // Filtro SELECT per il result set WHERE EMAIL_UTENTE = email;
        String select = TasksContract.ID_TASK + " IN ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = new String[deletedTasks.size()];
        for(int i = 0; i < deletedTasks.size(); i++)
            selectionArgs[i] = "" + deletedTasks.get(i);


        // Issue SQL statement.
        int deletedRows = db.delete(TasksContract.TABLE_NAME,
                select, selectionArgs);

        return deletedRows;
    }

    public int getHighestID() {
        final String MY_QUERY = "SELECT last_insert_rowid()";
        Cursor cur = this.getWritableDatabase().rawQuery(MY_QUERY, null);
        cur.moveToFirst();
        int ID = cur.getInt(0);
        cur.close();
        return ID;
    }
}
