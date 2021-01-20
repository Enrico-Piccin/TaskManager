package model;


import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import java.util.Date;

// Classe interna che definisce il contenuto della tabella Utenti
public class User implements Parcelable {
    private String nome, email, telefono, password;
    private int colore, task;
    private Date lastAccess;

    public User() {}

    public User(String nome, String email, String telefono, String password, int colore, int task, Date lastAccess) {
        this.nome = nome;
        this.email = email;
        this.telefono = telefono;
        this.password = password;
        this.colore = colore;
        this.task = task;
        this.lastAccess = lastAccess;
    }

    public String getNome() {
        return nome;
    }

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

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

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
        dest.writeString(this.lastAccess.toLocaleString());
    }

    // Metodo usato per rigenerare l'oggetto. Tutti i Parcelables devono avere un CREATORE che implementi questi due metodi
    public static final Parcelable.Creator<User>
            CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in.readString(), in.readString(), in.readString(), in.readString(), in.readInt(), in.readInt(), new Date(in.readString()));
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}