package model;


import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class User implements Parcelable {
    // Attributi specifici del singolo utente
    private String nome, email, telefono, password;
    private int colore, task;
    private Date lastAccess;

    // Costruttore di default
    public User() {}

    // Costruttore generico
    public User(String nome, String email, String telefono, String password, int colore, int task, Date lastAccess) {
        this.nome = nome;
        this.email = email;
        this.telefono = telefono;
        this.password = password;
        this.colore = colore;
        this.task = task;
        this.lastAccess = lastAccess;
    }

    // Getters & Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getPassword() {
        return password;
    }

    public int getColore() {
        return colore;
    }

    public int getTask() {
        return task;
    }

    public void setTask(int task) { this.task = task; }

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    // Implementazione dei metodi dell'interfaccia Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    // Vengono scritti i dati dell'oggetto sul Parcel passato in input
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.nome);
        dest.writeString(this.email);
        dest.writeString(this.telefono);
        dest.writeString(this.password);
        dest.writeInt(this.colore);
        dest.writeInt(this.task);
        dest.writeString(new SimpleDateFormat("yyyy-MM-dd").format(this.lastAccess));
    }

    // Metodo usato per rigenerare l'oggetto. Tutti i Parcelables devono avere un CREATORE che implementi questi due metodi
    public static final Parcelable.Creator<User>
            CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            try {
                return new User(in.readString(), in.readString(), in.readString(), in.readString(), in.readInt(), in.readInt(), new SimpleDateFormat("yyyy-MM-dd").parse(in.readString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return new User(in.readString(), in.readString(), in.readString(), in.readString(), in.readInt(), in.readInt(), Calendar.getInstance().getTime());
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}