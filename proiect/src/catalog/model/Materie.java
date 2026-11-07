package catalog.model;

public class Materie {
    private Long id;
    private String name;

    public Materie(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getNume() { return name; }
}