package catalog.model;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Teacher extends User {
    public Teacher(int id, String name, String email, String password) {
        super(id, name, email, password, "PROFESOR");
    }

    @Override
    public void afiseazaMeniu() {
        new TeacherProfileFrame(this, false).setVisible(true);
    }

    public List<String> getMaterii() {
        List<String> materii = new ArrayList<>();
        String sql = "SELECT nume_materie FROM materie WHERE id_profesor = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                materii.add(rs.getString("nume_materie"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materii;
    }

    public void vizualizeazaSituatieStudent() {
        String query = JOptionPane.showInputDialog(null, "Introduceți numele studentului sau codul grupei:");
        if (query == null || query.trim().isEmpty())
            return;
    }
}