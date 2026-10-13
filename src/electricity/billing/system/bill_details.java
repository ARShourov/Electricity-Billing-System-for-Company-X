package electricity.billing.system;

import net.proteanit.sql.DbUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.ResultSet;

public class bill_details extends Screen {
    String meter;
    JTable table;
    JButton close;

    bill_details(String meter){
        super("Bill Details", 760, 560, "Bill Details", "All bills for meter " + meter, 0);
        this.meter =meter;
        minSize(600, 420);
        content.setLayout(new BorderLayout(0, 14));
        content.setBorder(new EmptyBorder(22, 26, 22, 26));

        table = new JTable();

        try{
            database c = new database();
            String query_bill = "select * from bill where meter_no = '"+meter+"'";
            ResultSet resultSet = c.statement.executeQuery(query_bill);
            table.setModel(DbUtils.resultSetToTableModel(resultSet));
        }catch (Exception e){
            e.printStackTrace();
        }

        Theme.Card tableCard = Theme.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(16, 18, 18, 18));
        tableCard.add(Theme.tablePane(table), BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.CENTER);

        close = Theme.button("Close", Theme.Kind.SECONDARY);
        close.addActionListener(e -> setVisible(false));
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        foot.setOpaque(false);
        foot.add(close);
        content.add(foot, BorderLayout.SOUTH);

        setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new bill_details(""));
    }
}
