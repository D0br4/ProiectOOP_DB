package catalog.model;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupViewFrame extends JFrame {
    private String groupName;
    private Teacher teacher;
    private Student student;
    private JTable table;
    private DefaultTableModel model;
    private String tipVizualizare;
    private JFrame parentFrame;

    private JLabel lblMedia = new JLabel();
    private JLabel lblStatus = new JLabel();

    public GroupViewFrame(String groupName, Teacher teacher, Student student, String tipVizualizare, JFrame parentFrame) {
        this.groupName = groupName;
        this.teacher = teacher;
        this.student = student;
        this.tipVizualizare = tipVizualizare;
        this.parentFrame = parentFrame;

        setTitle("Situație " + tipVizualizare + " - " + (student != null ? student.getName() : groupName));
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupTableColumns();
        add(new JScrollPane(table), BorderLayout.CENTER);

        if (teacher != null) {
            setupActionPanel();
        } else if (student != null) {
            setupStudentFooter();
        }

        loadData();
    }

    private void setupTableColumns() {
        String[] columns;
        if (tipVizualizare.equals("VizualizareGrupa")) {
            columns = new String[]{"ID Student", "Nume", "Prenume", "Status"};
        } else if (teacher == null && student != null) {
            if (tipVizualizare.equals("Absenta")) {
                columns = new String[]{"Materia", "Absențe (Date)", "Nr. Total Absențe"};
            } else {
                columns = new String[]{"Materia", "Nota", "Credite"};
            }
        } else {
            columns = new String[]{"ID Student", "Nume", "Prenume", "Note / Dată Completă", "Absențe / Dată Completă"};
        }

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(50);
    }

    private void setupStudentFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        footer.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        lblMedia.setFont(new Font("Arial", Font.BOLD, 16));
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.add(lblMedia);
        infoPanel.add(lblStatus);

        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> {
            this.dispose();
            if (parentFrame != null) {
                parentFrame.setVisible(true);
            }
        });

        footer.add(infoPanel);
        footer.add(btnBack);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadData() {
        model.setRowCount(0);
        String sql;
        if (tipVizualizare.equals("VizualizareGrupa")) {
            sql = "SELECT id, nume, prenume FROM studenti WHERE id_grupa = ? ORDER BY nume ASC";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, groupName);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("nume"),
                            rs.getString("prenume"),
                            "Student"
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        } else if (teacher != null) {
            sql = "SELECT s.id, s.nume, s.prenume, " +
                    "STRING_AGG(CASE WHEN c.tip_inregistrare = 'Nota' AND c.id_profesor = ? " +
                    "THEN c.valoare || ' (' || TO_CHAR(c.data_inregistrare, 'DD.MM.YYYY') || ')' END, ', ') as note_info, " +
                    "STRING_AGG(CASE WHEN c.tip_inregistrare = 'Absenta' AND c.id_profesor = ? " +
                    "THEN TO_CHAR(c.data_inregistrare, 'DD.MM.YYYY') END, ', ') as abs_info " +
                    "FROM studenti s LEFT JOIN catalog c ON s.id = c.id_student " +
                    "WHERE s.id_grupa = ? GROUP BY s.id, s.nume, s.prenume ORDER BY s.nume ASC";
            executaIncarcareProfesor(sql);
        } else if (tipVizualizare.equals("Nota")) {
            sql = "SELECT m.nume_materie, c.valoare, m.credite " +
                    "FROM materie m " +
                    "JOIN studenti s ON m.an = CAST(SUBSTRING(s.id_grupa, 3, 1) AS INTEGER) " +
                    "               AND m.limba = CAST(SUBSTRING(s.id_grupa, 2, 1) AS INTEGER) " +
                    "LEFT JOIN catalog c ON m.id_materie = c.id_materie AND c.id_student = s.id AND c.tip_inregistrare = 'Nota' " +
                    "WHERE s.id = ? " +
                    "ORDER BY m.nume_materie";
            executaIncarcareNoteStudent(sql);
        } else {
            sql = "SELECT m.nume_materie, m.an, " +
                    "STRING_AGG(TO_CHAR(c.data_inregistrare, 'DD.MM.YYYY'), ', ') as date_abs, " +
                    "COUNT(c.id) as total " +
                    "FROM materie m " +
                    "JOIN studenti s ON m.an = CAST(SUBSTRING(s.id_grupa, 3, 1) AS INTEGER) " +
                    "               AND m.limba = CAST(SUBSTRING(s.id_grupa, 2, 1) AS INTEGER) " +
                    "LEFT JOIN catalog c ON m.id_materie = c.id_materie AND c.id_student = s.id AND c.tip_inregistrare = 'Absenta' " +
                    "WHERE s.id = ? " +
                    "GROUP BY m.nume_materie, m.an " +
                    "ORDER BY m.nume_materie";
            executaIncarcareAbsenteStudent(sql);
        }
    }

    private void executaIncarcareProfesor(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacher.getId());
            pstmt.setInt(2, teacher.getId());
            pstmt.setString(3, groupName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("nume"), rs.getString("prenume"),
                        "<html>" + (rs.getString("note_info") == null ? "-" : rs.getString("note_info")) + "</html>",
                        "<html><font color='red'>" + (rs.getString("abs_info") == null ? "-" : rs.getString("abs_info")) + "</font></html>"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void executaIncarcareNoteStudent(String sql) {
        double sumaProduseNotaCredite = 0;
        int totalCrediteAn = 0;
        int materiiCuNotaTrecuta = 0;
        int totalMateriiAn = 0;
        int totalCrediteRestante = 0;
        boolean areNoteLipsa = false;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, student.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                totalMateriiAn++;
                String notaStr = rs.getString("valoare");
                int credite = rs.getInt("credite");
                totalCrediteAn += credite;

                if (notaStr != null && !notaStr.isEmpty()) {
                    double nota = Double.parseDouble(notaStr);
                    materiiCuNotaTrecuta++;
                    if (nota >= 5) sumaProduseNotaCredite += (nota * credite);
                    else {
                        sumaProduseNotaCredite += (0 * credite);
                        totalCrediteRestante += credite;
                    }
                } else {
                    sumaProduseNotaCredite += (0 * credite);
                    areNoteLipsa = true;
                }

                model.addRow(new Object[]{
                        rs.getString("nume_materie"),
                        (notaStr == null ? "-" : notaStr),
                        credite
                });
            }

            if (totalMateriiAn > 0) {
                double mediePonderata = sumaProduseNotaCredite / totalCrediteAn;
                if (areNoteLipsa) {
                    int lipsa = totalMateriiAn - materiiCuNotaTrecuta;
                    lblMedia.setText("<html><font color='orange'>Media: " + String.format("%.2f", mediePonderata) + " (Incompletă - lipsesc " + lipsa + " note)</font></html>");
                    lblStatus.setText("<html>Status: <font color='gray'>SITUAȚIE NEÎNCHEIATĂ</font></html>");
                } else {
                    lblMedia.setText(String.format("Media Ponderată Finală: %.2f", mediePonderata));
                    if (totalCrediteRestante >= 15) lblStatus.setText("<html>Status: <font color='red'>AN REPETAT (" + totalCrediteRestante + " credite restante)</font></html>");
                    else if (totalCrediteRestante > 0) lblStatus.setText("<html>Status: <font color='blue'>PROMOVAT CU RESTANȚE (" + totalCrediteRestante + " credite restante)</font></html>");
                    else lblStatus.setText("<html>Status: <font color='green'>PROMOVAT COMPLET</font></html>");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void executaIncarcareAbsenteStudent(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, student.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String numeMaterieCuAn = rs.getString("nume_materie") + " (An " + rs.getInt("an") + ")";
                model.addRow(new Object[]{
                        numeMaterieCuAn,
                        rs.getString("date_abs") == null ? "-" : rs.getString("date_abs"),
                        rs.getInt("total")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setupActionPanel() {
        JPanel actionPanel = new JPanel();
        JButton btnNota = new JButton("Adaugă Notă");
        JButton btnAbsenta = new JButton("Adaugă Absență");
        JButton btnEditSterge = new JButton("Editează/Șterge");
        boolean esteTitular = (gasesteIdMaterieCorect() != -1);
        if (!esteTitular) {
            btnNota.setEnabled(false);
            btnAbsenta.setEnabled(false);
            btnEditSterge.setEnabled(false);
            JLabel lblAvertisment = new JLabel("<html><font color='red'>Vizualizare doar citire (Nu predați aici)</font></html>");
            actionPanel.add(lblAvertisment);
        }

        btnNota.addActionListener(e -> deschideDialogActiune("Nota"));
        btnAbsenta.addActionListener(e -> deschideDialogActiune("Absenta"));
        btnEditSterge.addActionListener(e -> gestioneazaInregistrare());

        actionPanel.add(btnNota);
        actionPanel.add(btnAbsenta);
        actionPanel.add(btnEditSterge);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void gestioneazaInregistrare() {
        if (teacher == null) return;
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectați un student!");
            return;
        }
        int idStudent = (int)table.getValueAt(row, 0);
        int choice = JOptionPane.showOptionDialog(this, "Ce doriți să gestionați?", "Opțiuni",
                0, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Notă", "Absență", "Anulează"}, null);
        if (choice == 0) editeazaSauStergeNota();
        else if (choice == 1) alegeSiStergeAbsenta(idStudent);
    }

    private void alegeSiStergeAbsenta(int idStudent) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, data_inregistrare FROM catalog WHERE id_student=? AND id_profesor=? AND tip_inregistrare='Absenta'")) {
            ps.setInt(1, idStudent);
            ps.setInt(2, teacher.getId());
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                listModel.addElement("Data: " + rs.getDate("data_inregistrare"));
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        if (listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu s-au găsit absențe puse de dvs. pentru acest student.");
            return;
        }
        JList<String> jl = new JList<>(listModel);
        if (JOptionPane.showConfirmDialog(this, new JScrollPane(jl), "Selectați absența de șters", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            executaStergereSQL(ids.get(jl.getSelectedIndex()));
        }
    }

    private void executaStergereSQL(int idInregistrare) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM catalog WHERE id = ? AND id_profesor = ?")) {
            ps.setInt(1, idInregistrare);
            ps.setInt(2, teacher.getId());
            ps.executeUpdate();
            loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void editeazaSauStergeNota() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int idStudent = (int) table.getValueAt(row, 0);
        int idMat = gasesteIdMaterieCorect();

        Object[] options = {"Editează", "Șterge Nota", "Anulează"};
        int choice = JOptionPane.showOptionDialog(this,
                "Doriți să modificați nota sau să o ștergeți definitiv?",
                "Gestionare Notă",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[2]);

        if (choice == 1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Sigur ștergeți nota?", "Confirmare", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                executaStergereNotaSQL(idStudent, idMat);
            }
        } else if (choice == 0) {
            String noua = JOptionPane.showInputDialog("Noua notă:");
            if (noua != null && !noua.trim().isEmpty()) {
                executaUpdateNotaSQL(Integer.parseInt(noua), idStudent, idMat);
            }
        }
    }

    private void executaStergereNotaSQL(int idStudent, int idMat) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM catalog WHERE id_student=? AND id_materie=? AND id_profesor=? AND tip_inregistrare='Nota'")) {
            ps.setInt(1, idStudent);
            ps.setInt(2, idMat);
            ps.setInt(3, teacher.getId());
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Nota a fost ștearsă!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void executaUpdateNotaSQL(int nota, int idStudent, int idMat) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE catalog SET valoare=? WHERE id_student=? AND id_materie=? AND id_profesor=? AND tip_inregistrare='Nota'")) {
            ps.setInt(1, nota);
            ps.setInt(2, idStudent);
            ps.setInt(3, idMat);
            ps.setInt(4, teacher.getId());
            ps.executeUpdate();
            loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int gasesteIdMaterieCorect() {
        if (groupName == null || groupName.length() < 2) return -1;
        int limbaGrupa = Character.getNumericValue(groupName.charAt(1));
        String sql = "SELECT id_materie FROM materie WHERE id_profesor = ? AND limba = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacher.getId());
            ps.setInt(2, limbaGrupa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_materie");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private void deschideDialogActiune(String tip) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectați un student!");
            return;
        }
        String val = tip.equals("Nota") ? JOptionPane.showInputDialog("Nota:") : "0";
        if (val == null) return;
        String data = JOptionPane.showInputDialog("Data (AAAA-LL-ZZ):", java.time.LocalDate.now());
        if (data == null) return;
        salveazaInCatalog((int)table.getValueAt(row, 0), tip, Integer.parseInt(val), data);
    }

    private void salveazaInCatalog(int idStudent, String tip, int valoare, String data) {
        int idMat = gasesteIdMaterieCorect();
        if (idMat == -1) {
            JOptionPane.showMessageDialog(this, "Eroare: Nu sunteți titular la materia corespunzătoare limbii acestei grupe!");
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO catalog (id_student, id_materie, id_profesor, tip_inregistrare, valoare, data_inregistrare) VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, idStudent);
            ps.setInt(2, idMat);
            ps.setInt(3, teacher.getId());
            ps.setString(4, tip);
            ps.setInt(5, valoare);
            ps.setDate(6, java.sql.Date.valueOf(data));

            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Date salvate!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Eroare la salvare" + e.getMessage());
        }
    }

    class MultiLineRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setVerticalAlignment(SwingConstants.TOP);
            label.setForeground(Color.BLACK);

            if (value != null) {
                String fullText = value.toString().replace("<html>", "").replace("</html>", "").trim();
                if (teacher != null && column == 3 && !fullText.equals("-")) {
                    try {
                        String notaPart = fullText.split(" ")[0];
                        double nota = Double.parseDouble(notaPart);
                        if (nota < 5) {
                            label.setForeground(Color.RED);
                            label.setFont(label.getFont().deriveFont(Font.BOLD));
                        }
                    } catch (Exception e) {}
                }
                if (student != null && column == 1 && !fullText.equals("-")) {
                    try {
                        double nota = Double.parseDouble(fullText);
                        if (nota < 5) {
                            label.setForeground(Color.RED);
                            label.setFont(label.getFont().deriveFont(Font.BOLD));
                        }
                    } catch (Exception e) {}
                }
            }
            return label;
        }
    }
}