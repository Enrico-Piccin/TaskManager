package model;

import java.util.Date;

public class Task {
    private int idTask;     // ID univoco della task
    private String content; // Contenuto della task
    private int priority;   // Priorità della task
    private Date dueDate;   // Data di scadenza della task
    private int idProject;  // ID del progetto di appartenenza
    private String email;   // Email dell'utente a cui è associata la task

    // Costruttore di default
    public Task() { }

    // Costruttore generico
    public Task(int idTask, String content, int priority, Date dueDate, int idProject, String email) {
        this.idTask = idTask;
        this.content = content;
        this.priority = priority;
        this.dueDate = dueDate;
        this.idProject = idProject;
        this.email = email;
    }

    // Getters & Setters
    public int getIdTask() {
        return idTask;
    }

    public void setIdTask(int idTask) {
        this.idTask = idTask;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}