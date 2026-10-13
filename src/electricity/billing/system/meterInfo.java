package electricity.billing.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class meterInfo extends Screen implements ActionListener {

    Theme.Cmb meterLocCho, meterTypCho,phaseCodeCho,billtypCho;
    JButton submit;
    String meternumber;

    meterInfo(String meternumber){
        super("Meter Information", 760, 520, "Meter Information", "Step 2 of 2  \u00B7  Meter details", FIXED);
        this.meternumber=meternumber;

        Theme.Card card = Theme.card();
        Theme.at(content, card, 28, 24, 704, 390);

        int x1 = 36, x2 = 374, w = 310;

        JTextField meterNumberText = Theme.field();
        meterNumberText.setText(meternumber);
        meterNumberText.setEditable(false);
        Theme.labeled(card, "METER NUMBER", meterNumberText, x1, 28, w);

        meterLocCho = Theme.combo("Outside", "Inside");
        Theme.labeled(card, "METER LOCATION", meterLocCho, x2, 28, w);

        meterTypCho = Theme.combo("Electric Meter", "Solar Meter", "Smart Meter");
        Theme.labeled(card, "METER TYPE", meterTypCho, x1, 108, w);

        phaseCodeCho = Theme.combo("011", "022", "033", "044", "055", "066", "077", "088", "099");
        Theme.labeled(card, "PHASE CODE", phaseCodeCho, x2, 108, w);

        billtypCho = Theme.combo("Normal", "Industrial");
        Theme.labeled(card, "BILL TYPE", billtypCho, x1, 188, w);

        JTextField days = Theme.field();
        days.setText("30 days");
        days.setEditable(false);
        Theme.labeled(card, "BILLING PERIOD", days, x2, 188, w);

        JPanel note = new JPanel(null) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.aa(g);
                g.setColor(Theme.BLUE_SOFT);
                g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g.dispose();
            }
        };
        note.setOpaque(false);
        Theme.at(card, note, x1, 270, 648, 44);
        Theme.at(note, Theme.label("Note: by default, bills are calculated for 30 days only.", 13f, Font.PLAIN, new Color(0x1E40AF)),
                16, 12, 600, 20);

        submit = Theme.button("Submit", Theme.Kind.PRIMARY);
        submit.addActionListener(this);
        Theme.at(card, submit, x1, 336, 150, 44);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==submit){
            String smeterNum = meternumber;
            String smeterLoc = meterLocCho.value();
            String smeterTyp = meterTypCho.value();
            String sphaseCode = phaseCodeCho.value();
            String sbillTyp = billtypCho.value();
            String sday ="30";

            String query_meterInfo = "insert into meter_info values('"+smeterNum+"','"+smeterLoc+"','"+smeterTyp+"','"+sphaseCode+"','"+sbillTyp+"','"+sday+"')";
            try{
                database c= new database();
                c.statement.executeUpdate(query_meterInfo);

                info("Meter information submitted successfully.");
                setVisible(false);
            }catch (Exception E){
                E.printStackTrace();
                error("Could not save the meter information: " + E.getMessage());
            }
        }else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new meterInfo(""));
    }
}
