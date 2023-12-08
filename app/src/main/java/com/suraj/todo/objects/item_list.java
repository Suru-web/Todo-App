package com.suraj.todo.objects;

public class item_list {
    String task;
    int date;
    int month;
    int year;
    boolean completed;
    String docID;
    String categ;

    public item_list() {

    }

    public item_list(String task, int date, int month, int year, boolean completed, String docID, String categ) {
        this.task = task;
        this.date = date;
        this.month = month;
        this.year = year;
        this.completed = completed;
        this.docID = docID;
        this.categ = categ;
    }

    public String getCateg() {
        return categ;
    }

    public void setCateg(String categ) {
        this.categ = categ;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
