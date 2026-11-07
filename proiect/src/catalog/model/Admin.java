package catalog.model;

import javax.swing.JOptionPane;
import java.sql.*;

public class Admin extends User {
    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password, "ADMIN");
    }

    @Override
    public void afiseazaMeniu() {
        new AdminProfileFrame().setVisible(true);
    }

    public void gestiuneUtilizatori() {
        String[] options = {"Deschide Panou Administrare", "Adaugă Utilizator Nou", "Anulează"};
        int choice = JOptionPane.showOptionDialog(null, "Ce doriți să faceți?",
                "Gestiune Utilizatori", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            afiseazaMeniu();
        } else if (choice == 1) {
            interfataAdaugareRapida();
        }
    }

    public void interfataAdaugareRapida() {
        String nume = JOptionPane.showInputDialog("Nume:");
        if (nume == null) return;
        String prenume = JOptionPane.showInputDialog("Prenume:");
        if (prenume == null) return;
        String email = JOptionPane.showInputDialog("Email:");
        if (email == null) return;
        String parola = JOptionPane.showInputDialog("Parola:");
        if (parola == null) return;

        String[] roles = {"Student", "Profesor"};
        int roleChoice = JOptionPane.showOptionDialog(null, "Alegeți rolul:", "Rol",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);

        if (roleChoice == 0) {
            String grupa = JOptionPane.showInputDialog("ID Grupa (ex: 2211):");
            if (grupa != null) adaugaStudentInBD(nume, prenume, email, parola, grupa);
        } else if (roleChoice == 1) {
            String[] limbi = {"Romana", "Engleza", "Romana/Engleza"};
            String limba = (String) JOptionPane.showInputDialog(null, "Limba de predare:",
                    "Limba", JOptionPane.QUESTION_MESSAGE, null, limbi, limbi[0]);

            if (limba != null) adaugaProfesorInBD(nume, prenume, email, parola, limba);
        }
    }

    public void adaugaStudentInBD(String nume, String prenume, String email, String parola, String idGrupa) {
        String sqlVerificare = "SELECT COUNT(*) FROM studenti WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(sqlVerificare)) {

            psCheck.setString(1, email);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null, "Eroare: Studentul cu email-ul " + email + " există deja!");
                return;
            }

            String sqlInsert = "INSERT INTO studenti (nume, prenume, email, parola, id_grupa) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                psInsert.setString(1, nume);
                psInsert.setString(2, prenume);
                psInsert.setString(3, email);
                psInsert.setString(4, parola);
                psInsert.setString(5, idGrupa);

                psInsert.executeUpdate();
                JOptionPane.showMessageDialog(null, "Student adăugat cu succes!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void adaugaProfesorInBD(String nume, String prenume, String email, String parola, String limba) {
        String sql = "INSERT INTO profesori (nume, prenume, email, parola, limba) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nume);
            ps.setString(2, prenume);
            ps.setString(3, email);
            ps.setString(4, parola);
            ps.setString(5, limba);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Profesor adăugat!");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}