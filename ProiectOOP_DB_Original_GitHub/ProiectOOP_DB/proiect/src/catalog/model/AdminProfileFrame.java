package catalog.model;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;

public class AdminProfileFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> viewFilter;
    private JTextField txtCautare = new JTextField(20);
    private TableRowSorter<DefaultTableModel> sorter;

    private JButton btnMuta;
    private JButton btnSterge;
    private JButton btnAdauga;

    public AdminProfileFrame() {
        setTitle("Profil Administrator - Control Sistem");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Gestionare:"));
        viewFilter = new JComboBox<>(new String[]{"Studenti", "Profesori"});

        viewFilter.addActionListener(e -> {
            incarcaDate();
            boolean esteStudent = viewFilter.getSelectedItem().toString().equals("Studenti");
            btnAdauga.setEnabled(esteStudent);
            btnSterge.setEnabled(esteStudent);
            btnMuta.setEnabled(esteStudent);
        });
        topPanel.add(viewFilter);

        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(txtCautare);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Nume", "Prenume", "Info", "Tip"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        txtCautare.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicaFiltru(); }
            public void removeUpdate(DocumentEvent e) { aplicaFiltru(); }
            public void changedUpdate(DocumentEvent e) { aplicaFiltru(); }
            private void aplicaFiltru() {
                String text = txtCautare.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2));
                }
            }
        });

        JPanel sidePanel = new JPanel(new GridLayout(5, 1, 10, 10));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnMuta = new JButton("Mută Student");
        JButton btnParola = new JButton("Schimbă Parola");
        btnSterge = new JButton("Șterge Student");
        btnAdauga = new JButton("Adauga Student");
        JButton btnLogout = new JButton("Logout");

        btnMuta.addActionListener(e -> gestioneazaMutare());
        btnParola.addActionListener(e -> gestioneazaSchimbareParola());
        btnSterge.addActionListener(e -> gestioneazaStergere());

        btnAdauga.addActionListener(e -> {
            if (viewFilter.getSelectedItem().toString().equals("Studenti")) {
                String nume = JOptionPane.showInputDialog(this, "Nume Student:");
                if (nume == null || nume.trim().isEmpty()) return;

                String prenume = JOptionPane.showInputDialog(this, "Prenume Student:");
                if (prenume == null || prenume.trim().isEmpty()) return;

                String grupa = JOptionPane.showInputDialog(this, "Grupă (ex: 2111):");
                if (grupa == null || grupa.trim().isEmpty()) return;

                String emailUnic = genereazaEmailUnic(nume, prenume);
                String parola = "123";

                salveazaStudentInDB(nume, prenume, emailUnic, parola, grupa);
                incarcaDate();

                JOptionPane.showMessageDialog(this, "S-a generat automat email-ul: " + emailUnic);
            } else {
                JOptionPane.showMessageDialog(this, "Adăugarea este permisă doar pentru studenți!");
            }
        });

        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });

        sidePanel.add(btnMuta);
        sidePanel.add(btnParola);
        sidePanel.add(btnSterge);
        sidePanel.add(btnAdauga);
        sidePanel.add(btnLogout);
        add(sidePanel, BorderLayout.EAST);

        incarcaDate();
    }

    private void salveazaStudentInDB(String nume, String prenume, String email, String parola, String grupa) {
        String sql = "INSERT INTO public.studenti (nume, prenume, email, parola, id_grupa) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nume);
            ps.setString(2, prenume);
            ps.setString(3, email);
            ps.setString(4, parola);
            ps.setString(5, grupa);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student adăugat cu succes!");
        } catch (SQLException ex) {
            if (ex.getMessage().contains("already exists") || ex.getMessage().contains("duplicate key")) {
                JOptionPane.showMessageDialog(this, "Eroare: Email-ul " + email + " există deja!");
            } else {
                JOptionPane.showMessageDialog(this, "Eroare SQL: " + ex.getMessage());
            }
        }
    }

    private String genereazaEmailUnic(String nume, String prenume) {
        String baza = prenume.toLowerCase().replace(" ", "") + "." +
                nume.toLowerCase().replace(" ", "");
        String emailPropus = baza + "@student";
        int counter = 1;

        try (Connection conn = DBConnection.getConnection()) {
            while (true) {
                String sql = "SELECT COUNT(*) FROM public.studenti WHERE email = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, emailPropus);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        return emailPropus;
                    } else {
                        emailPropus = baza + counter + "@student";
                        counter++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return emailPropus;
        }
    }

    private void incarcaDate() {
        txtCautare.setText("");
        model.setRowCount(0);
        String selectie = viewFilter.getSelectedItem().toString();

        if (selectie.equals("Profesori")) {
            model.setColumnIdentifiers(new Object[]{"ID", "NUME", "PRENUME", "EMAIL", "PAROLA", "ROL"});
            String sql = "SELECT id, nume, prenume, email, parola, 'Profesor' as rol FROM profesori ORDER BY nume";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("nume"),
                            rs.getString("prenume"),
                            rs.getString("email"),
                            rs.getString("parola"),
                            rs.getString("rol")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            model.setColumnIdentifiers(new Object[]{"ID", "NUME", "PRENUME", "GRUPA", "EMAIL", "PAROLA", "ROL"});

            String sql = "SELECT id, nume, prenume, id_grupa, email, parola, 'Student' as rol FROM studenti ORDER BY nume";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("nume"),
                            rs.getString("prenume"),
                            rs.getString("id_grupa"),
                            rs.getString("email"),
                            rs.getString("parola"),
                            rs.getString("rol")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void gestioneazaSchimbareParola() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selectați un utilizator!"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);
        String selectie = viewFilter.getSelectedItem().toString();
        String tabel = selectie.equals("Studenti") ? "studenti" : "profesori";
        String nouaParola = JOptionPane.showInputDialog(this, "Introduceți noua parolă:");
        if (nouaParola != null && !nouaParola.trim().isEmpty()) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE " + tabel + " SET parola = ? WHERE id = ?")) {
                ps.setString(1, nouaParola); ps.setInt(2, id); ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Parola a fost actualizată!");
            } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Eroare: " + e.getMessage()); }
        }
    }

    private void gestioneazaMutare() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);
        String nouaGrupa = JOptionPane.showInputDialog(this, "ID Noua Grupă:");
        if (nouaGrupa != null && !nouaGrupa.trim().isEmpty()) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE studenti SET id_grupa = ? WHERE id = ?")) {
                ps.setString(1, nouaGrupa); ps.setInt(2, id); ps.executeUpdate();
                incarcaDate(); JOptionPane.showMessageDialog(this, "Student mutat.");
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void gestioneazaStergere() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selectați un student!"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);
        String numeComplet = model.getValueAt(modelRow, 1).toString() + " " + model.getValueAt(modelRow, 2).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Ștergeți definitiv studentul " + numeComplet + "?", "Confirmare", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    PreparedStatement ps1 = conn.prepareStatement("DELETE FROM public.catalog WHERE id_student = ?");
                    ps1.setInt(1, id); ps1.executeUpdate();
                    PreparedStatement ps2 = conn.prepareStatement("DELETE FROM public.studenti WHERE id = ?");
                    ps2.setInt(1, id); ps2.executeUpdate();
                    conn.commit(); incarcaDate();
                    JOptionPane.showMessageDialog(this, "Student șters.");
                } catch (SQLException ex) { conn.rollback(); throw ex; }
            } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Eroare: " + e.getMessage()); }
        }
    }
}