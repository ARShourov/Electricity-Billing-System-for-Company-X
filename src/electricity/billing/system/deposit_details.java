package electricity.billing.system;

import net.proteanit.sql.DbUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

public class deposit_details extends Screen implements ActionListener {
    Theme.Cmb searchMeterCho,searchMonthCho;
    JTable table;
    JButton search,showAll,print,close;
    deposit_details(){
        super("Deposit Details", 980, 620, "Deposit Details", "Every bill and its payment status", 0);
        minSize(760, 480);
        content.setLayout(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(22, 26, 22, 26));

        searchMeterCho = Theme.combo();
        try{
            database c= new database();
            ResultSet resultSet = c.statement.executeQuery("select * from bill");
            while (resultSet.next()){
                searchMeterCho.addOption(resultSet.getString("meter_no"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        searchMonthCho = Theme.combo(Theme.MONTHS);

        search = Theme.button("Search", Theme.Kind.PRIMARY);
        search.addActionListener(this);
        showAll = Theme.button("Show all", Theme.Kind.SECONDARY);
        showAll.addActionListener(this);
        print = Theme.button("Print", Theme.Kind.SECONDARY);
        print.addActionListener(this);
        close = Theme.button("Close", Theme.Kind.GHOST);
        close.addActionListener(this);

        Theme.Card bar = Theme.card();
        bar.setLayout(new BorderLayout());
        bar.setBorder(new EmptyBorder(16, 24, 18, 24));
        bar.setPreferredSize(new Dimension(10, 108));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(Theme.stack("METER NUMBER", searchMeterCho, 190));
        left.add(Theme.stack("MONTH", searchMonthCho, 190));
        JPanel go = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        go.setOpaque(false);
        go.add(search);
        go.add(showAll);
        left.add(Theme.stack("", go, -1));
        bar.add(left, BorderLayout.WEST);
        JPanel more = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        more.setOpaque(false);
        more.add(print);
        more.add(close);
        JPanel moreWrap = new JPanel(new BorderLayout());
        moreWrap.setOpaque(false);
        moreWrap.add(Theme.stack("", more, -1), BorderLayout.EAST);
        bar.add(moreWrap, BorderLayout.CENTER);
        content.add(bar, BorderLayout.NORTH);

        table = new JTable();
        showAllRows();

        Theme.Card tableCard = Theme.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(16, 18, 18, 18));
        tableCard.add(Theme.tablePane(table), BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.CENTER);

        setVisible(true);
    }

    private void showAllRows() {
        try{
            database c =new database();
            ResultSet resultSet = c.statement.executeQuery("select * from bill");
            table.setModel(DbUtils.resultSetToTableModel(resultSet));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==search){
            String query_search = "select * from bill where meter_no = '"+searchMeterCho.value()+"' and month = '"+searchMonthCho.value()+"'" ;
            try{
                database c = new database();
                ResultSet resultSet = c.statement.executeQuery(query_search);
                table.setModel(DbUtils.resultSetToTableModel(resultSet));
            }catch (Exception E){
                E.printStackTrace();
            }
        } else if (e.getSource()==showAll) {
            showAllRows();
        } else if (e.getSource()==print)
        {
            try {
                table.print();
            } catch (Exception E) {
                E.printStackTrace();
            }

        }else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(deposit_details::new);
    }
}
