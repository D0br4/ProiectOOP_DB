package catalog.model;

import javax.swing.*;

public class Student extends User {
    private String id_grupa;
    public Student(int id, String nume, String email, String parola, String id_grupa) {
        super(id, nume, email, parola, "STUDENT");
        this.id_grupa = id_grupa;
    }

    @Override
    public void afiseazaMeniu() {
        new StudentProfileFrame(this, null, false, null).setVisible(true);
    }

    public String getGroup() {
        return id_grupa;
    }

    public void vizualizeazaNoteSiAbsente(String tip) {
        new GroupViewFrame(id_grupa, null,this, tip, null).setVisible(true);
    }
}