package electricity.billing.system;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

public class update_information extends Screen implements ActionListener {
    JTextField nametext, meterText;
    JTextField addressText,cityText,stateText,emailText,phoneText;
    String meter;
    JButton update,cancel;

    update_information(String meter){
        super("Update Information", 760, 540, "Update Information", "Keep your contact details up to date", FIXED);
        this.meter=meter;

        Theme.Card card = Theme.card();
        Theme.at(content, card, 28, 24, 704, 410);

        int x1 = 36, x2 = 374, w = 310;

        nametext = Theme.field();
        nametext.setEditable(false);
        Theme.labeled(card, "NAME", nametext, x1, 28, w);

        meterText = Theme.field();
        meterText.setEditable(false);
        Theme.labeled(card, "METER NUMBER", meterText, x2, 28, w);

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

        try{
            database c = new database();
            ResultSet resultSet = c.statement.executeQuery("select * from new_customer where meter_no = '"+meter+"'");
            if (resultSet.next()){
                nametext.setText(resultSet.getString("name"));
                meterText.setText(resultSet.getString("meter_no"));
                addressText.setText(resultSet.getString("address"));
                cityText.setText(resultSet.getString("city"));
                stateText.setText(resultSet.getString("state"));
                emailText.setText(resultSet.getString("email"));
                phoneText.setText(resultSet.getString("phone_no"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        update = Theme.button("Save changes", Theme.Kind.PRIMARY);
        update.addActionListener(this);
        Theme.at(card, update, x1, 346, 170, 44);

        cancel = Theme.button("Cancel", Theme.Kind.SECONDARY);
        cancel.addActionListener(this);
        Theme.at(card, cancel, x1 + 184, 346, 110, 44);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==update){
            String saddress = addressText.getText();
            String scity = cityText.getText();
            String sstate = stateText.getText();
            String semail = emailText.getText();
            String sphone = phoneText.getText();

            try{
                database c = new database();
                c.statement.executeUpdate("update new_customer set address ='"+saddress+"', city = '"+scity+"', state = '"+sstate+"', email = '"+semail+"', phone_no ='"+sphone+"' where meter_no = '"+meter+"'");

                info("Your information was updated successfully.");
                setVisible(false);
            }catch (Exception E){
                E.printStackTrace();
                error("Could not update the information: " + E.getMessage());
            }
        }else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new update_information(""));
    }
}