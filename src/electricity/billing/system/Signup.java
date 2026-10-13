package electricity.billing.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;

public class Signup extends Screen implements ActionListener {
    Theme.Cmb loginASCho;
    JTextField meterText,EmployerText,userNameText,nameText;
    JPasswordField passwordText;
    JButton create,back;
    private final JLabel meterNo, employer;

    Signup(){
        super("Signup Page", 900, 500, null, null, FIXED | QUIT_ON_CLOSE);
        content.setBackground(Color.WHITE);

        Theme.at(content, new Theme.Brand("Join the", "grid today.",
                "Admins manage customers and bills",
                "Customers view and pay their bills",
                "Your data stays on your computer"), 0, 0, 340, 500);

        int x = 390, colW = 230, gap = 20;
        Theme.at(content, Theme.label("Create your account", 27f, Font.BOLD, Theme.INK), x, 44, 480, 34);
        Theme.at(content, Theme.label("Pick a role and fill in your details", 14f, Font.PLAIN, Theme.MUTED), x, 80, 480, 20);

        loginASCho = Theme.combo("Admin", "Customer");
        Theme.labeled(content, "CREATE ACCOUNT AS", loginASCho, x, 122, colW);

        // The two fields below share the same spot; the role decides which one shows.
        employer = Theme.at(content, Theme.label("EMPLOYER ID", 12f, Font.BOLD, Theme.MUTED), x + colW + gap, 122, colW, 16);
        EmployerText = Theme.at(content, Theme.field(), x + colW + gap, 141, colW, 38);
        meterNo = Theme.at(content, Theme.label("METER NUMBER", 12f, Font.BOLD, Theme.MUTED), x + colW + gap, 122, colW, 16);
        meterText = Theme.at(content, Theme.field(), x + colW + gap, 141, colW, 38);
        meterNo.setVisible(false);
        meterText.setVisible(false);

        userNameText = Theme.field();
        Theme.labeled(content, "USERNAME", userNameText, x, 200, colW);

        nameText = Theme.field();
        Theme.labeled(content, "NAME", nameText, x + colW + gap, 200, colW);

        passwordText = Theme.password();
        Theme.labeled(content, "PASSWORD", passwordText, x, 278, colW);

        meterText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lookupName();
            }
        });

        loginASCho.addActionListener(e -> {
            String user = loginASCho.value();
            boolean customer = user.equals("Customer");
            employer.setVisible(!customer);
            EmployerText.setVisible(!customer);
            meterNo.setVisible(customer);
            meterText.setVisible(customer);
            nameText.setEditable(!customer);
        });

        create = Theme.button("Create account", Theme.Kind.PRIMARY);
        create.addActionListener(this);
        Theme.at(content, create, x, 388, 230, 44);
        defaultButton(create);

        back = Theme.button("Back to login", Theme.Kind.SECONDARY);
        back.addActionListener(this);
        Theme.at(content, back, x + colW + gap, 388, 230, 44);

        setVisible(true);
    }

    /** Customer accounts are created by the admin first; typing the meter number fills in the name. */
    void lookupName() {
        try{
            database c = new database();
            ResultSet resultSet = c.statement.executeQuery("select * from Signup  where meter_no = '"+meterText.getText()+"'");
            if (resultSet.next()){
                nameText.setText(resultSet.getString("name"));
            }

        }catch (Exception E){
            E.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()== create){
            String sloginAs = loginASCho.value();
            String susername = userNameText.getText();
            String sname = nameText.getText();
            String spassword = new String(passwordText.getPassword());
            String smeter = meterText.getText();
            try{
                database c = new database();
                String query= null;
                if (sloginAs.equals("Admin")) {
                    query = "insert into Signup value('" + smeter + "', '" + susername + "', '" + sname + "','" + spassword + "','" + sloginAs + "')";
                }else {
                    query = "update Signup set username = '"+susername+"', password = '"+spassword+"', usertype = '"+sloginAs+"' where meter_no = '"+smeter+"'";
                }
                 c.statement.executeUpdate(query);

                info("Account created. You can log in now.");
                setVisible(false);
                new Login();

            }catch (Exception E){
                E.printStackTrace();
                error("Could not create the account: " + E.getMessage());
            }
        } else if (e.getSource()==back) {
            setVisible(false);
            new Login();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Signup::new);
    }
}