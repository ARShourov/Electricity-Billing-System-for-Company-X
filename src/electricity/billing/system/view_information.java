package electricity.billing.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

public class view_information extends Screen implements ActionListener {
    String view;
    JButton cancel;

    view_information(String view){
        super("View Information", 760, 520, "My Information", "Your registered customer details", FIXED);
        this.view = view;

        Theme.Card card = Theme.card();
        Theme.at(content, card, 28, 24, 704, 392);

        String name = "", meterNo = "", address = "", city = "", state = "", email = "", phone = "";
        try{
            database c = new database();
            ResultSet resultSet = c.statement.executeQuery("select * from new_customer where meter_no = '"+view+"'");
            if (resultSet.next()){
                name = resultSet.getString("name");
                meterNo = resultSet.getString("meter_no");
                address = resultSet.getString("address");
                city = resultSet.getString("city");
                state = resultSet.getString("state");
                email = resultSet.getString("email");
                phone = resultSet.getString("phone_no");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        final String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        JComponent avatar = new JComponent() {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.aa(g);
                g.setPaint(new GradientPaint(0, 0, Theme.BLUE, getWidth(), getHeight(), new Color(0x7C3AED)));
                g.fillOval(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.setFont(Theme.font(32f, Font.BOLD));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g.dispose();
            }
        };
        Theme.at(card, avatar, 36, 30, 76, 76);
        Theme.at(card, Theme.label(name, 24f, Font.BOLD, Theme.INK), 132, 38, 520, 32);
        Theme.atPill(card, Theme.pill("Meter " + meterNo, Theme.BLUE, Theme.BLUE_SOFT), 132, 76);

        JSeparator line = new JSeparator();
        line.setForeground(Theme.LINE);
        Theme.at(card, line, 36, 128, 632, 2);

        int x1 = 36, x2 = 374;
        detail(card, "ADDRESS", address, x1, 150);
        detail(card, "CITY", city, x2, 150);
        detail(card, "STATE", state, x1, 220);
        detail(card, "EMAIL", email, x2, 220);
        detail(card, "PHONE", phone, x1, 290);

        cancel = Theme.button("Close", Theme.Kind.SECONDARY);
        cancel.addActionListener(this);
        Theme.at(card, cancel, 558, 322, 110, 40);

        setVisible(true);
    }

    private void detail(Container parent, String caption, String value, int x, int y) {
        Theme.at(parent, Theme.label(caption, 12f, Font.BOLD, Theme.MUTED), x, y, 300, 16);
        Theme.at(parent, Theme.label(value, 16f, Font.PLAIN, Theme.INK), x, y + 22, 310, 24);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==cancel){
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new view_information(""));

    }
}
