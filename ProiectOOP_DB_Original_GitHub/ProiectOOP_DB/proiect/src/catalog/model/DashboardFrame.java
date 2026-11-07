package catalog.model;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardFrame extends JFrame {
    private User user;

    public DashboardFrame(User user) {
        this.user = user;
        setTitle("Catalog - " + user.getName());
        setSize(600, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        headerPanel.setBackground(new Color(230, 230, 230));
        JTextField searchField = new JTextField(25);
        JButton btnSearch = new JButton("Caută");
        headerPanel.add(searchField);
        headerPanel.add(btnSearch);
        btnSearch.addActionListener(e -> proceseazaCautarea(searchField.getText().trim()));
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        if (user instanceof Teacher) {
            setupTeacherUI(mainPanel, (Teacher) user);
        } else if (user instanceof Student) {
            setupStudentUI(mainPanel, (Student) user);
        }

        add(mainPanel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        add(logoutButton, BorderLayout.SOUTH);
    }

    private void proceseazaCautarea(String query) {
        if (query.isEmpty()) return;

        if (esteGrupa(query)) {
            Teacher t = (user instanceof Teacher) ? (Teacher)user : null;
            new GroupViewFrame(query, t, null, "Grupă", this).setVisible(true);
            return;
        }

        Student s = gasesteStudent(query);
        if (s != null) {
            Teacher t = (user instanceof Teacher) ? (Teacher)user : null;
            new StudentProfileFrame(s, t, false, this).setVisible(true);
            return;
        }

        Teacher p = gasesteProfesor(query);
        if (p != null) {
            new TeacherProfileFrame(p, false).setVisible(true);
            return;
        }

        JOptionPane.showMessageDialog(this, "Numele sau grupa nu au fost găsite!");
    }

    private void setupTeacherUI(JPanel panel, Teacher teacher) {
        JLabel lblNumeHeader = new JLabel(teacher.getName().toUpperCase());
        lblNumeHeader.setFont(new Font("Arial", Font.BOLD, 26));
        lblNumeHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblEmail = new JLabel("email: " + teacher.getEmail());
        lblEmail.setFont(new Font("Consolas", Font.PLAIN, 16));
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblNumeHeader);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblEmail);
        panel.add(Box.createVerticalStrut(30));

        JLabel lblMateriTitle = new JLabel("Materii:");
        lblMateriTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblMateriTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblMateriTitle);

        List<String> materii = getMateriiProfesor(teacher.getId());
        for (String m : materii) {
            JLabel lblMat = new JLabel("- " + m);
            lblMat.setFont(new Font("Arial", Font.PLAIN, 15));
            lblMat.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(lblMat);
        }

        panel.add(Box.createVerticalGlue());

        JButton btnGrupe = new JButton("Grupele mele");
        btnGrupe.addActionListener(e -> afiseazaListaGrupe(teacher));
        panel.add(btnGrupe);
    }

    private void setupStudentUI(JPanel panel, Student student) {
        JLabel lblNume = new JLabel(student.getName().toUpperCase());
        lblNume.setFont(new Font("Arial", Font.BOLD, 26));
        lblNume.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblEmail = new JLabel("email: " + student.getEmail());
        lblEmail.setFont(new Font("Consolas", Font.PLAIN, 16));
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblNume);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblEmail);
        panel.add(Box.createVerticalStrut(40));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton btnNote = new JButton("NOTE");
        JButton btnAbs = new JButton("ABSENȚE");
        btnNote.addActionListener(e -> student.vizualizeazaNoteSiAbsente("Nota"));
        btnAbs.addActionListener(e -> student.vizualizeazaNoteSiAbsente("Absenta"));

        btnPanel.add(btnNote);
        btnPanel.add(btnAbs);
        panel.add(btnPanel);
    }

    private Teacher gasesteProfesor(String query) {
        String sql = "SELECT id, nume, prenume, email FROM public.profesori " +
                "WHERE (nume || ' ' || prenume) ILIKE ? OR (prenume || ' ' || nume) ILIKE ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String numeComplet = rs.getString("nume") + " " + rs.getString("prenume");
                return new Teacher(rs.getInt("id"), numeComplet, rs.getString("email"), "");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private boolean esteGrupa(String query) {
        String sql = "SELECT 1 FROM grupe WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, query);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    private Student gasesteStudent(String query) {
        String sql = "SELECT id, nume, prenume, email, id_grupa FROM public.studenti " +
                "WHERE (nume || ' ' || prenume) ILIKE ? OR (prenume || ' ' || nume) ILIKE ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String numeComplet = rs.getString("nume") + " " + rs.getString("prenume");
                return new Student(rs.getInt("id"), numeComplet, rs.getString("email"), "", rs.getString("id_grupa"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private List<String> getMateriiProfesor(int id) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nume_materie FROM materie WHERE id_profesor = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { lista.add(rs.getString("nume_materie")); }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private void afiseazaListaGrupe(Teacher teacher) {
        List<String> grupe = new ArrayList<>();
        String sql = "SELECT DISTINCT g.id FROM grupe g JOIN materie m ON g.an = m.an AND g.limba = m.limba WHERE m.id_profesor = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacher.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { grupe.add(rs.getString("id")); }
        } catch (SQLException e) { e.printStackTrace(); }

        if (grupe.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu aveți grupe alocate!");
            return;
        }

        String selectie = (String) JOptionPane.showInputDialog(this, "Alegeți grupa:", "Grupele mele",
                JOptionPane.PLAIN_MESSAGE, null, grupe.toArray(), grupe.get(0));

        if (selectie != null) {
            new GroupViewFrame(selectie, teacher, null, "Grupă", this).setVisible(true);
        }
    }
}