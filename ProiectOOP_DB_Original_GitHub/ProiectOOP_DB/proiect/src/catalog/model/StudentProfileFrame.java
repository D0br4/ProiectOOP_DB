package catalog.model;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class StudentProfileFrame extends JFrame {
    private Student student;
    private JFrame parentFrame;
    public StudentProfileFrame(Student student, Teacher teacher, boolean esteVizualizare, JFrame parentFrame) {
        this.student = student;
        this.parentFrame = parentFrame;

        setTitle("Profil Student - " + student.getName());
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(230, 230, 230));
        JTextField searchField = new JTextField(20);
        JButton btnCauta = new JButton("Caută");

        btnCauta.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                proceseazaCautareaStudent(query);
            }
        });

        topPanel.add(searchField);
        topPanel.add(btnCauta);
        add(topPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblNume = new JLabel(student.getName().toUpperCase());
        lblNume.setFont(new Font("Arial", Font.BOLD, 26));
        lblNume.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmail = new JLabel("email: " + student.getEmail());
        lblEmail.setFont(new Font("Consolas", Font.PLAIN, 16));
        lblEmail.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTel = new JLabel("telefon: 0744144944");
        lblTel.setFont(new Font("Arial", Font.PLAIN, 16));
        lblTel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblGrupa = new JLabel("Grupa: " + student.getGroup());
        lblGrupa.setFont(new Font("Arial", Font.BOLD, 20));
        lblGrupa.setForeground(new Color(0, 51, 153));
        lblGrupa.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(lblNume);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(lblEmail);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(lblTel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(lblGrupa);
        mainPanel.add(Box.createVerticalStrut(30));

        if (!esteVizualizare) {
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.setMaximumSize(new Dimension(350, 45));

            JButton btnNote = new JButton("NOTE");
            JButton btnAbsente = new JButton("ABSENȚE");

            btnNote.addActionListener(e -> {
                this.setVisible(false);
                new GroupViewFrame(student.getGroup(), null, student, "Nota", this).setVisible(true);
            });

            btnAbsente.addActionListener(e -> {
                this.setVisible(false);
                new GroupViewFrame(student.getGroup(), null, student, "Absenta", this).setVisible(true);
            });

            buttonPanel.add(btnNote);
            buttonPanel.add(btnAbsente);
            mainPanel.add(buttonPanel);
        }

        add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);

        JTextField infoField = new JTextField("Pentru probleme scrieți la user@admin");
        infoField.setEditable(false);
        infoField.setHorizontalAlignment(JTextField.CENTER);
        infoField.setFont(new Font("Arial", Font.ITALIC, 14));
        infoField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoField.setFocusable(false);
        infoField.setOpaque(false);

        bottomPanel.add(infoField, BorderLayout.CENTER);

        JButton btnBottom = new JButton(esteVizualizare ? "ÎNCHIDE" : "LOG OUT");
        btnBottom.setBackground(new Color(200, 50, 50));
        btnBottom.setForeground(Color.WHITE);
        btnBottom.setFont(new Font("Arial", Font.BOLD, 14));
        btnBottom.setPreferredSize(new Dimension(0, 40));

        btnBottom.addActionListener(e -> {
            this.dispose();
            if (esteVizualizare && parentFrame != null) {
                parentFrame.setVisible(true);
            } else if (!esteVizualizare) {
                new LoginFrame().setVisible(true);
            }
        });

        bottomPanel.add(btnBottom, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

    }

    private void proceseazaCautareaStudent(String query) {
        if (esteGrupaValida(query)) {
            new GroupViewFrame(query, null, student, "VizualizareGrupa", this).setVisible(true);
            return;
        }

        Student s = gasesteStudentInDB(query);
        if (s != null) {
            if (s.getId() != student.getId()) {
                new StudentProfileFrame(s, null, true, this).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Acesta este profilul dumneavoastră.");
            }
            return;
        }

        Teacher p = gasesteProfesorInDB(query);
        if (p != null) {
            new TeacherProfileFrame(p, true).setVisible(true);
            return;
        }

        JOptionPane.showMessageDialog(this, "Numele sau grupa '" + query + "' nu au fost găsite!");
    }

    private boolean esteGrupaValida(String query) {
        String sql = "SELECT 1 FROM public.grupe WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, query);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    private Student gasesteStudentInDB(String query) {
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

    private Teacher gasesteProfesorInDB(String query) {
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
}