    package catalog.model;

    import javax.swing.*;
    import java.awt.*;
    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;

    public class TeacherProfileFrame extends JFrame {
        private Teacher teacher;
        public TeacherProfileFrame(Teacher teacher, boolean esteVizualizare) {
            this.teacher = teacher;
            setTitle("Profil Profesor - " + teacher.getName());
            setSize(500, 650);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout());

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            topPanel.setBackground(new Color(230, 230, 230));
            JTextField searchField = new JTextField(20);
            JButton btnCauta = new JButton("Caută");

            btnCauta.addActionListener(e -> {
                String query = searchField.getText().trim();
                if (!query.isEmpty()) {
                    proceseazaCautareaInProfil(query);
                }
            });

            topPanel.add(searchField);
            topPanel.add(btnCauta);
            add(topPanel, BorderLayout.NORTH);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
            mainPanel.setBackground(Color.WHITE);

            JLabel lblNume = new JLabel(teacher.getName().toUpperCase());
            lblNume.setFont(new Font("Arial", Font.BOLD, 26));
            lblNume.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblEmail = new JLabel("email: " + teacher.getEmail());
            lblEmail.setFont(new Font("Consolas", Font.PLAIN, 16));
            lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

            mainPanel.add(lblNume);
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(lblEmail);
            mainPanel.add(Box.createVerticalStrut(30));

            JLabel lblMateriiTitlu = new JLabel("Materii predate:");
            lblMateriiTitlu.setFont(new Font("Arial", Font.BOLD, 18));
            lblMateriiTitlu.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(lblMateriiTitlu);
            mainPanel.add(Box.createVerticalStrut(10));

            List<String> materii = getMateriiProfesor(teacher.getId());
            for (String m : materii) {
                JLabel lblM = new JLabel("- " + m);
                lblM.setFont(new Font("Arial", Font.PLAIN, 16));
                lblM.setAlignmentX(Component.LEFT_ALIGNMENT);
                mainPanel.add(lblM);
            }

            if (!esteVizualizare) {
                mainPanel.add(Box.createVerticalGlue());
                JButton btnGrupe = new JButton("GRUPELE MELE");
                btnGrupe.addActionListener(e -> afiseazaListaGrupeDirect());
                mainPanel.add(btnGrupe);
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

            JButton btnLogout = new JButton(esteVizualizare ? "ÎNCHIDE" : "LOG OUT");
            btnLogout.setBackground(new Color(200, 50, 50));
            btnLogout.setForeground(Color.WHITE);
            btnLogout.setPreferredSize(new Dimension(0, 40));
            btnLogout.addActionListener(e -> {
                this.dispose();
                if (!esteVizualizare) {
                    new LoginFrame().setVisible(true);
                }
            });

            bottomPanel.add(btnLogout, BorderLayout.SOUTH);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        private void proceseazaCautareaInProfil(String query) {
            if (esteGrupaValida(query)) {
                new GroupViewFrame(query, teacher, null, "Grupă", this).setVisible(true);
                return;
            }

            Student s = gasesteStudentInDB(query);
            if (s != null) {
                new StudentProfileFrame(s, teacher, true, this).setVisible(true);
                return;
            }

            Teacher p = gasesteProfesorInDB(query);
            if (p != null) {
                if (p.getId() != teacher.getId()) {
                    new TeacherProfileFrame(p, true).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Acesta este profilul dumneavoastră.");
                }
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

        private void afiseazaListaGrupeDirect() {
            List<String> grupe = new ArrayList<>();
            String sql = "SELECT DISTINCT g.id FROM grupe g " +
                    "JOIN materie m ON g.an = m.an AND g.limba = m.limba " +
                    "WHERE m.id_profesor = ?";
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
    }