package catalog.model;

public abstract class User {
    private int id;
    private String nume;
    private String email;
    private String parola;
    private String rol;

    public User(int id, String nume, String email, String parola, String rol) {
        this.id = id;
        this.nume = nume;
        this.email = email;
        this.parola = parola;
        this.rol = rol;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return nume;
    }

    public int getId() {
        return id;
    }

    public String getRole() {
        return rol;
    }

    public abstract void afiseazaMeniu();
}