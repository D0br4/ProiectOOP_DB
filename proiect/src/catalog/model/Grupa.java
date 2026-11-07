package catalog.model;

public class Grupa {
    private int id;
    private String nume;
    private int anStudiu;

    public Grupa(int id, String nume, int anStudiu) {
        this.id = id;
        this.nume = nume;
        this.anStudiu = anStudiu;
    }

    public String getNume() {
        return nume;
    }
    public int getAnStudiu() {
        return anStudiu;
    }
}