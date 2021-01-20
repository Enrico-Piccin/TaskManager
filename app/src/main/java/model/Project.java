package model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Project implements Parcelable {
    private int idProject;
    private String nome;
    private int colore;
    private int idParent;
    private boolean favorite;
    private String email;

    public Project() { }

    public Project(int idProject, String nome, int colore, int idParent, boolean favorite, String email) {
        this.idProject = idProject;
        this.nome = nome;
        this.colore = colore;
        this.idParent = idParent;
        this.favorite = favorite;
        this.email = email;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getColore() {
        return colore;
    }

    public void setColore(int colore) {
        this.colore = colore;
    }

    public int getIdParent() {
        return idParent;
    }

    public void setIdParent(int idParent) {
        this.idParent = idParent;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.idProject);
        dest.writeString(this.nome);
        dest.writeInt(this.colore);
        dest.writeInt(this.idParent);
        dest.writeInt(this.favorite ? 1 : 0);
        dest.writeString(this.email);
    }

    // Metodo usato per rigenerare l'oggetto. Tutti i Parcelables devono avere un CREATORE che implementi questi due metodi
    public static final Parcelable.Creator<Project>
            CREATOR = new Parcelable.Creator<Project>() {
        public Project createFromParcel(Parcel in) {
            return new Project(in.readInt(), in.readString(), in.readInt(), in.readInt(), in.readInt() != 0, in.readString());
        }

        public Project[] newArray(int size) {
            return new Project[size];
        }
    };
}
