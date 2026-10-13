package electricity.billing.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

public class Login extends Screen implements ActionListener {
    JTextField userText;
    JPasswordField passwordText;
    Theme.Cmb loginChoice;

    JButton loginButton,cancelButton,signupButton;

    Login(){
        super("Login", 860, 520, null, null, FIXED | QUIT_ON_CLOSE);
        content.setBackground(Color.WHITE);

        Theme.Brand brand = new Theme.Brand("Power your", "billing, simply.",
                "Create customers and meters in seconds",
                "Generate bills with one click",
                "Track every payment in one place");
        Theme.at(content, brand, 0, 0, 340, 520);

        int x = 400, w = 400;
        Theme.at(content, Theme.label("Welcome back", 27f, Font.BOLD, Theme.INK), x, 56, w, 34);
        Theme.at(content, Theme.label("Sign in to your account to continue", 14f, Font.PLAIN, Theme.MUTED), x, 92, w, 20);

        userText = Theme.field();
        Theme.labeled(content, "USERNAME", userText, x, 142, w);

        passwordText = Theme.password();
        Theme.labeled(content, "PASSWORD", passwordText, x, 218, w);

        loginChoice = Theme.combo("Admin", "Customer");
        Theme.labeled(content, "LOGGING IN AS", loginChoice, x, 294, w);

        loginButton = Theme.button("Login", Theme.Kind.PRIMARY);
        loginButton.addActionListener(this);
        Theme.at(content, loginButton, x, 384, w, 44);
        defaultButton(loginButton);

        signupButton = Theme.button("Create an account", Theme.Kind.GHOST);
        signupButton.addActionListener(this);
        Theme.at(content, signupButton, x, 446, 190, 36);

        cancelButton = Theme.button("Cancel", Theme.Kind.GHOST);
        cancelButton.addActionListener(this);
        cancelButton.setForeground(Theme.MUTED);
        Theme.at(content, cancelButton, x + w - 90, 446, 90, 36);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==loginButton){
             String susername = userText.getText();
             String spassword = new String(passwordText.getPassword());
             String suser = loginChoice.value();

             try{
                 database d = new database();
                 String queryy = "select * from Signup where username = '"+susername+"' and password = '"+spassword+"' and usertype ='"+suser+"'";
                 ResultSet resultSet = d.statement.executeQuery(queryy);

                 if (resultSet.next()){
                     String meter = resultSet.getString("meter_no");
                     setVisible(false);
                     new main_class(suser,meter);
                 }else {
                     error("Invalid login. Check your username, password and role.");
                 }

             }catch (Exception E){
                 E.printStackTrace();
                 error("Login failed: " + E.getMessage());
             }
        } else if (e.getSource()==cancelButton) {
            setVisible(false);
            exitApp();
        } else if (e.getSource()==signupButton) {
            setVisible(false);
            new Signup();
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}