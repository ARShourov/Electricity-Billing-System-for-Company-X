package electricity.billing.system;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.time.Year;

public class generate_bill extends Screen implements ActionListener {
    Theme.Cmb searchmonthcho;
    String meter;
    JTextArea area;
    JButton bill, close;

    generate_bill(String meter){
        super("Generate Bill", 640, 740, "Generate Bill", "Meter " + meter, FIXED);
        this.meter=meter;
        content.setLayout(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(22, 26, 22, 26));

        searchmonthcho = Theme.combo(Theme.MONTHS);
        bill = Theme.button("Generate bill", Theme.Kind.PRIMARY);
        bill.addActionListener(this);
        close = Theme.button("Close", Theme.Kind.SECONDARY);
        close.addActionListener(e -> setVisible(false));

        Theme.Card bar = Theme.card();
        bar.setLayout(new BorderLayout());
        bar.setBorder(new EmptyBorder(16, 24, 18, 24));
        bar.setPreferredSize(new Dimension(10, 106));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(Theme.stack("MONTH", searchmonthcho, 200));
        JPanel go = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        go.setOpaque(false);
        go.add(bill);
        left.add(Theme.stack("", go, -1));
        bar.add(left, BorderLayout.WEST);
        JPanel cl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        cl.setOpaque(false);
        cl.add(close);
        JPanel clWrap = new JPanel(new BorderLayout());
        clWrap.setOpaque(false);
        clWrap.add(Theme.stack("", cl, -1), BorderLayout.EAST);
        bar.add(clWrap, BorderLayout.CENTER);
        content.add(bar, BorderLayout.NORTH);

        area= new JTextArea();
        area.setText("\n\n   Pick a month and click \"Generate bill\"\n   to see the full bill here.");
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setForeground(Theme.MUTED);
        area.setEditable(false);
        area.setMargin(new Insets(18, 22, 18, 22));
        JScrollPane pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        Theme.slim(pane);

        Theme.Card paper = Theme.card();
        paper.setLayout(new BorderLayout());
        paper.setBorder(new EmptyBorder(16, 18, 18, 18));
        paper.add(pane, BorderLayout.CENTER);
        content.add(paper, BorderLayout.CENTER);

        setVisible(true);
    }

    private static String row(String key, String value) {
        return String.format("  %-26s %s%n", key, value);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        try{
            database c = new database();
            String smonth =  searchmonthcho.value();
            area.setForeground(Theme.INK);
            String rule = "  " + "-".repeat(46) + "\n";
            area.setText("\n                POWER LIMITED\n"
                    + "        Electricity bill - " + smonth + " " + Year.now().getValue() + "\n" + rule);

            // the amount first, so it is visible without scrolling
            ResultSet resultSet = c.statement.executeQuery("select * from bill where meter_no = '"+meter+"' and month = '"+searchmonthcho.value()+"'");
            if (resultSet.next()) {
                area.append("\n  BILL SUMMARY\n");
                area.append(row("Month", resultSet.getString("month")));
                area.append(row("Units consumed", resultSet.getString("unit")));
                area.append(row("Total charges", resultSet.getString("total_bill")));
                area.append(row("TOTAL PAYABLE", resultSet.getString("total_bill")));
                area.append(row("Status", resultSet.getString("status")));
            } else {
                area.append("\n  No bill has been created for " + smonth + " yet.\n");
            }

            resultSet = c.statement.executeQuery("select * from new_customer where meter_no ='"+meter+"'");
            if (resultSet.next()){
                area.append("\n" + rule + "\n  CUSTOMER\n");
                area.append(row("Name", resultSet.getString("name")));
                area.append(row("Meter number", resultSet.getString("meter_no")));
                area.append(row("Address", resultSet.getString("address")));
                area.append(row("City", resultSet.getString("city")));
                area.append(row("State", resultSet.getString("state")));
                area.append(row("Email", resultSet.getString("email")));
                area.append(row("Phone", resultSet.getString("phone_no")));

            }

            resultSet = c.statement.executeQuery("select * from meter_info where meter_number ='"+meter+"'");
            if (resultSet.next()){
                area.append("\n  METER\n");
                area.append(row("Location", resultSet.getString("meter_location")));
                area.append(row("Type", resultSet.getString("meter_type")));
                area.append(row("Phase code", resultSet.getString("phase_code")));
                area.append(row("Bill type", resultSet.getString("bill_type")));
                area.append(row("Billing days", resultSet.getString("Days")));


            }
            resultSet = c.statement.executeQuery("select * from tax");
            if (resultSet.next()){
                area.append("\n  TARIFF\n");
                area.append(row("Cost per unit", resultSet.getString("cost_per_unit")));
                area.append(row("Meter rent", resultSet.getString("meter_rent")));
                area.append(row("Service charge", resultSet.getString("service_charge")));
                area.append(row("Service tax", resultSet.getString("service_tax")));
                area.append(row("Fixed tax", resultSet.getString("fixed_tax")));

            }
            area.setCaretPosition(0);

        }catch (Exception E ){
            E.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new generate_bill(""));
    }
}
