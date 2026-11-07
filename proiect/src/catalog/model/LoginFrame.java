package catalog.model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passField;
    private JButton loginButton;

    public LoginFrame() {
        setTitle("Login Catalog Online");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("  Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("  Parola:"));
        passField = new JPasswordField();
        add(passField);

        loginButton = new JButton("Login");
        add(new JLabel(""));
        add(loginButton);

        loginButton.addActionListener(this::handleLogin);
    }

    private void handleLogin(ActionEvent e) {
        String email = emailField.getText();
        String pass = new String(passField.getPassword());

        User user = Main.findUser(email, pass);

        if (user != null) {
            JOptionPane.showMessageDialog(this, "Login Reușit! Bine ai venit, " + user.getName());
            this.dispose();
            user.afiseazaMeniu();
        } else {
            JOptionPane.showMessageDialog(this, "Date incorecte!", "Eroare", JOptionPane.ERROR_MESSAGE);
        }
    }
}