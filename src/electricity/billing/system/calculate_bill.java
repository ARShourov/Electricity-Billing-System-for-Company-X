package electricity.billing.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.ResultSet;

public class calculate_bill extends Screen implements ActionListener{
    JLabel nameText, addressText;
    JTextField unitText;
    Theme.Cmb meternumCho,monthCho;
    JButton submit, cancel;

    calculate_bill(){
        super("Calculate Bill", 700, 520, "Calculate Electricity Bill", "Create a monthly bill for a meter", FIXED);

        Theme.Card card = Theme.card();
        Theme.at(content, card, 28, 24, 644, 390);

        int x1 = 36, w = 572;

        meternumCho = Theme.combo();
        try {
            database c = new database();
            ResultSet resultSet = c.statement.executeQuery("select * from new_customer");
            while (resultSet.next()){
                meternumCho.addOption(resultSet.getString("meter_no"));
            }
        }catch (Exception E){
            E.printStackTrace();
        }
        Theme.labeled(card, "METER NUMBER", meternumCho, x1, 28, 270);

        // customer strip: shows who the selected meter belongs to
        Theme.at(card, Theme.label("CUSTOMER", 12f, Font.BOLD, Theme.MUTED), x1, 108, w, 16);
        JPanel strip = new JPanel(null) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.aa(g);
                g.setColor(new Color(0xF1F5F9));
                g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g.dispose();
            }
        };
        strip.setOpaque(false);
        Theme.at(card, strip, x1, 127, w, 58);

        nameText = Theme.at(strip, Theme.label("", 15f, Font.BOLD, Theme.INK), 16, 8, w - 32, 22);
        addressText = Theme.at(strip, Theme.label("", 13f, Font.PLAIN, Theme.MUTED), 16, 30, w - 32, 20);

        loadCustomer();
        meternumCho.addActionListener(e -> loadCustomer());

        unitText = Theme.field();
        Theme.labeled(card, "UNITS CONSUMED", unitText, x1, 208, 270);

        monthCho = Theme.combo(Theme.MONTHS);
        Theme.labeled(card, "MONTH", monthCho, x1 + 302, 208, 270);

        submit = Theme.button("Create bill", Theme.Kind.PRIMARY);
        submit.addActionListener(this);
        Theme.at(card, submit, x1, 308, 160, 44);

        cancel = Theme.button("Cancel", Theme.Kind.SECONDARY);
        cancel.addActionListener(this);
        Theme.at(card, cancel, x1 + 174, 308, 110, 44);

        setVisible(true);
    }

    private void loadCustomer() {
        nameText.setText("");
        addressText.setText("");
        try {
            database c= new database();
            ResultSet resultSet = c.statement.executeQuery("select * from new_customer where meter_no = '"+meternumCho.value()+"' ");
            while (resultSet.next()){
                nameText.setText(resultSet.getString("name"));
                addressText.setText(resultSet.getString("address"));
            }
        }catch (Exception E){
            E.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submit){
            String smeterNo = meternumCho.value();
            String sunit = unitText.getText().trim();
            String smonth = monthCho.value();

            int totalBill =0;
            int units;
            try {
                units = Integer.parseInt(sunit);
            } catch (NumberFormatException nfe) {
                warn("Enter the units consumed as a whole number, for example 120.");
                return;
            }
            String query_tax = "select * from tax";
            try{
                database c = new database();
                ResultSet resultSet = c.statement.executeQuery(query_tax);
                while (resultSet.next()){
                    totalBill += units * Integer.parseInt(resultSet.getString("cost_per_unit"));
                    totalBill += Integer.parseInt(resultSet.getString("meter_rent"));
                    totalBill += Integer.parseInt(resultSet.getString("service_charge"));
                    totalBill += Integer.parseInt(resultSet.getString("swacch_bharat"));
                    totalBill += Integer.parseInt(resultSet.getString("fixed_tax"));

                }
            }catch (Exception E){
                E.printStackTrace();
            }
            String query_total_bill = "insert into bill values('"+smeterNo+"', '"+smonth+"','"+sunit+"', '"+totalBill+"','Not Paid')";
            try{
                database c = new database();
                c.statement.executeUpdate(query_total_bill);

                info("Bill created. Total: " + totalBill);
                setVisible(false);
            }catch (Exception E){
                E.printStackTrace();
                error("Could not save the bill: " + E.getMessage());
            }

        }else {
            setVisible(false);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(calculate_bill::new);
    }

}
