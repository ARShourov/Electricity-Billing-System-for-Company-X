package electricity.billing.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

public class pay_bill extends Screen implements ActionListener {
    Theme.Cmb searchmonthcho;
    String meter;
    JButton pay,back;
    JLabel meterNumberText, nameText, unitText, totalBillText;
    Theme.Pill statusText;
    private boolean hasBill = false;

    pay_bill(String meter){
        super("Pay Bill", 760, 500, "Pay Bill", "Pick a month and pay with bKash", FIXED);
        this.meter=meter;

        Theme.Card card = Theme.card();
        Theme.at(content, card, 28, 24, 704, 370);

        int x1 = 36, x2 = 374, w = 310;

        detailCaption(card, "METER NUMBER", x1, 28);
        meterNumberText = Theme.at(card, Theme.label("", 18f, Font.BOLD, Theme.INK), x1, 50, w, 26);
        detailCaption(card, "CUSTOMER", x2, 28);
        nameText = Theme.at(card, Theme.label("", 18f, Font.BOLD, Theme.INK), x2, 50, w, 26);

        searchmonthcho = Theme.combo(Theme.MONTHS);
        Theme.labeled(card, "MONTH", searchmonthcho, x1, 108, w);

        detailCaption(card, "STATUS", x2, 108);
        statusText = Theme.pill("No bill", Theme.MUTED, new Color(0xE2E8F0));
        Theme.at(card, statusText, x2, 133, 110, 30);

        detailCaption(card, "UNITS CONSUMED", x1, 200);
        unitText = Theme.at(card, Theme.label("-", 22f, Font.BOLD, Theme.INK), x1, 222, w, 32);
        detailCaption(card, "TOTAL BILL", x2, 200);
        totalBillText = Theme.at(card, Theme.label("-", 22f, Font.BOLD, Theme.BLUE), x2, 222, w, 32);

        try{
            database c = new database();
            ResultSet resultSet = c.statement.executeQuery("select * from new_customer where meter_no = '"+meter+"'");
            while (resultSet.next()){
                meterNumberText.setText(meter);
                nameText.setText(resultSet.getString("name"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        searchmonthcho.addActionListener(e -> refresh());
        refresh();

        pay = Theme.button("Pay with bKash", Theme.Kind.BKASH);
        pay.addActionListener(this);
        Theme.at(card, pay, x1, 290, 190, 44);

        back = Theme.button("Back", Theme.Kind.SECONDARY);
        back.addActionListener(this);
        Theme.at(card, back, x1 + 204, 290, 110, 44);

        setVisible(true);
    }

    private void detailCaption(Container parent, String text, int x, int y) {
        Theme.at(parent, Theme.label(text, 12f, Font.BOLD, Theme.MUTED), x, y, 300, 16);
    }

    /** Show the bill of the selected month (or a clean "No bill" state). */
    private void refresh() {
        hasBill = false;
        unitText.setText("-");
        totalBillText.setText("-");
        statusText.set("No bill", Theme.MUTED, new Color(0xE2E8F0));
        try{
            database c = new database();
            ResultSet resultSet = c.statement.executeQuery("select * from bill where meter_no = '"+meter+"' and month = '"+searchmonthcho.value()+"'");
            while (resultSet.next()){
                hasBill = true;
                unitText.setText(resultSet.getString("unit"));
                totalBillText.setText(resultSet.getString("total_bill"));
                String status = resultSet.getString("status");
                if (status.equals("Paid")) statusText.set("Paid", Theme.GREEN, Theme.GREEN_SOFT);
                else statusText.set(status, Theme.RED, Theme.RED_SOFT);
            }
        }catch (Exception E){
            E.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==pay){
            if (!hasBill) {
                warn("There is no bill for " + searchmonthcho.value() + " yet.");
                return;
            }
            if (statusText.getText().equals("Paid")) {
                info("The bill for " + searchmonthcho.value() + " is already paid.");
                return;
            }
            try{
                database c = new database();
                c.statement.executeUpdate("update bill set status = 'Paid' where meter_no = '"+meter+"' and month = '"+searchmonthcho.value()+"'");
            }catch (Exception E){
                E.printStackTrace();
            }
            setVisible(false);
            new payment_bill(meter);
        }else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new pay_bill(""));
    }
}
