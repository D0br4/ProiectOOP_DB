package catalog.model;

import java.sql.*;
import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final List<Materie> MATERII_DB = new ArrayList<>();
    private static final List<Grupa> GRUPE_DB = new ArrayList<>();

    public static User findUser(String email, String pass) {
        try (Connection conn = DBConnection.getConnection()) {
            String sqlProf = "SELECT *, (nume || ' ' || prenume) as nume_complet FROM profesori WHERE email = ? AND parola = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlProf)) {
                pstmt.setString(1, email);
                pstmt.setString(2, pass);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Teacher(rs.getInt("id"), rs.getString("nume_complet"), email, pass);
                }
            }
            String sqlStud = "SELECT *, (nume || ' ' || prenume) as nume_complet FROM studenti WHERE email = ? AND parola = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlStud)) {
                pstmt.setString(1, email);
                pstmt.setString(2, pass);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Student(rs.getInt("id"), rs.getString("nume_complet"), email, pass, rs.getString("id_grupa"));
                }
            }
            String sqlAdmin = "SELECT * FROM admini WHERE email = ? AND parola = ?";
            try (PreparedStatement pstmtAdmin = conn.prepareStatement(sqlAdmin)) {
                pstmtAdmin.setString(1, email);
                pstmtAdmin.setString(2, pass);
                ResultSet rsAdmin = pstmtAdmin.executeQuery();

                if (rsAdmin.next()) {
                    System.out.println("Logare reușită: Admin găsit!");
                    return new Admin(
                            rsAdmin.getInt("id"),
                            rsAdmin.getString("nume"),
                            rsAdmin.getString("email"),
                            rsAdmin.getString("parola")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Eroare la conectarea sau interogarea bazei de date!");
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}