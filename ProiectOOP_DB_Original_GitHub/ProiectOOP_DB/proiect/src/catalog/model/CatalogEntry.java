package catalog.model;

import java.time.LocalDate;

public class CatalogEntry{
    private Long id;
    private Long idStudent;
    private Long idMaterie;
    private Long idProfesor;
    private String tipInregistrare;
    private int valoare;
    private LocalDate data;

    public CatalogEntry(Long id, Long idStudent, Long idMaterie, Long idProfesor,
                        String tipInregistrare, int valoare, LocalDate data) {
        this.id = id;
        this.idStudent = idStudent;
        this.idMaterie = idMaterie;
        this.idProfesor = idProfesor;
        this.tipInregistrare = tipInregistrare;
        this.valoare = valoare;
        this.data = data;
    }

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public Long getIdStudent() { return idStudent; }
    public Long getIdMaterie() { return idMaterie; }
    public Long getIdProfesor() { return idProfesor; }
    public String getTipInregistrare() { return tipInregistrare; }
    public int getValoare() { return valoare; }
    public LocalDate getData() { return data; }

    public void setValoare(int valoare) { this.valoare = valoare; }
    public void setIdProfesor(Long idProfesor) { this.idProfesor = idProfesor; }
}