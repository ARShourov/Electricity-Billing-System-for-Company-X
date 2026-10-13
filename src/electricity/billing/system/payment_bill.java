package electricity.billing.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class payment_bill extends Screen implements ActionListener {
    JButton back;
    String meter;
    JTextField bkashNumber;

    payment_bill(String meter){
        super("Payment", 460, 240, null, null, FIXED);
        this.meter = meter;

        // The window itself shows nothing except the Back button.
        back = Theme.button("Back", Theme.Kind.SECONDARY);
        back.addActionListener(this);
        Theme.at(content, back, 460 - 24 - 100, 24, 100, 40);

        setVisible(true);

        showBkashPopup();
    }

    // bKash-style popup. This is only the screen: no real money moves, because that needs
    // bKash merchant API credentials. Hook the real payment call into the Confirm button below.
    private void showBkashPopup(){
        final Popup[] ref = new Popup[1];

        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(400, 330));

        JPanel header = new JPanel(null) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.aa(g);
                g.setPaint(new GradientPaint(0, 0, new Color(0xE2136E), getWidth(), getHeight(), new Color(0xB5105A)));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(255, 255, 255, 22));
                g.fillOval(getWidth() - 110, -60, 170, 170);
                g.setColor(Color.WHITE);
                g.setFont(Theme.font(32f, Font.BOLD));
                g.drawString("bKash", 26, 46);
                g.setFont(Theme.font(13f, Font.PLAIN));
                g.setColor(new Color(0xFFD3E8));
                g.drawString("Secure mobile payment", 28, 68);
                g.dispose();
            }
        };
        Theme.at(p, header, 0, 0, 400, 84);

        Theme.atPill(p, Theme.pill("Meter No: " + meter, new Color(0x9D0F4E), new Color(0xFCE7F3)), 26, 102);

        bkashNumber = Theme.field();
        Theme.labeled(p, "YOUR BKASH ACCOUNT NUMBER", bkashNumber, 26, 142, 348);

        JLabel hint = Theme.at(p, Theme.label("Example: 01712345678", 12f, Font.PLAIN, Theme.MUTED), 28, 204, 340, 18);

        Theme.Btn confirm = Theme.button("Confirm", Theme.Kind.BKASH);
        Theme.at(p, confirm, 26, 244, 168, 44);
        Theme.Btn close = Theme.button("Close", Theme.Kind.SECONDARY);
        Theme.at(p, close, 206, 244, 168, 44);

        confirm.addActionListener(ev -> {
            String number = bkashNumber.getText().trim().replaceFirst("^\\+?88", "");   // allow +8801... / 8801...
            if (!number.matches("01[3-9][0-9]{8}")) {
                hint.setText("Enter a valid 11-digit bKash number, e.g. 01712345678");
                hint.setForeground(Theme.RED);
                return;
            }
            ref[0].close();
            info("bKash payment submitted from " + number + ".");
        });
        close.addActionListener(ev -> ref[0].close());

        ref[0] = popup("bKash Payment", p);
        ref[0].show();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        new pay_bill(meter);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new payment_bill(""));
    }
}
