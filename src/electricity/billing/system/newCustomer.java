package electricity.billing.system;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class newCustomer extends Screen implements ActionListener {
    JTextField meternumText, nameText, addressText, cityText, stateText ,emailText,phoneText;
    JButton next, cancel;

    newCustomer(){
        super("New Customer", 760, 580, "New Customer", "Step 1 of 2  \u00B7  Customer details", FIXED);

        Theme.Card card = Theme.card();
        Theme.at(content, card, 28, 24, 704, 440);

        int x1 = 36, x2 = 374, w = 310;

        nameText = Theme.field();
        Theme.labeled(card, "CUSTOMER NAME", nameText, x1, 28, w);

        meternumText = Theme.field();
        meternumText.setEditable(false);
        Theme.labeled(card, "METER NUMBER (AUTO)", meternumText, x2, 28, w);

        Random ran = new Random();
        long number = ran.nextLong() % 1000000;
        meternumText.setText(""+ Math.abs(number) );

        addressText = Theme.field();
        Theme.labeled(card, "ADDRESS", addressText, x1, 108, w);

        cityText = Theme.field();
        Theme.labeled(card, "CITY", cityText, x2, 108, w);

        stateText = Theme.field();
        Theme.labeled(card, "STATE", stateText, x1, 188, w);

        emailText = Theme.field();
        Theme.labeled(card, "EMAIL", emailText, x2, 188, w);

        phoneText = Theme.field();
        Theme.labeled(card, "PHONE", phoneText, x1, 268, w);

        next = Theme.button("Next: meter details", Theme.Kind.PRIMARY);
        next.addActionListener(this);
        Theme.at(card, next, x1, 366, 190, 44);

        cancel = Theme.button("Cancel", Theme.Kind.SECONDARY);
        cancel.addActionListener(this);
        Theme.at(card, cancel, x1 + 204, 366, 110, 44);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==next){
            String sname = nameText.getText();
            String smeter = meternumText.getText();
            String saddress = addressText.getText();
            String scity = cityText.getText();
            String sstate = stateText.getText();
            String eemail = emailText.getText();
            String sphone = phoneText.getText();


            String query_customer = "insert into new_customer values('"+sname+"','"+smeter+"','"+saddress+"','"+scity+"','"+sstate+"','"+eemail+"','"+sphone+"')";
            String query_signup = "insert into Signup values('"+smeter+"','','"+sname+"','','')";

            try{
                database c = new database();
                c.statement.executeUpdate(query_customer);
                c.statement.executeUpdate(query_signup);

                info("Customer details added successfully.");
                setVisible(false);
                new meterInfo(smeter);
            }catch (Exception E){
                E.printStackTrace();
                error("Could not save the customer: " + E.getMessage());
            }
        }else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
         SwingUtilities.invokeLater(newCustomer::new);
    }
}