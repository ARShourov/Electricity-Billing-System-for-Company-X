package electricity.billing.system;

import javax.swing.*;
import java.awt.*;

public class Splash extends Screen {
    Splash(){
        super("Electricity Billing System for Company X", 640, 380, null, null, UNDECORATED);

        JPanel art = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.aa(g);
                int w = getWidth(), h = getHeight();
                g.setPaint(new GradientPaint(0, 0, Theme.NAVY_DEEP, w, h, Theme.BLUE));
                g.fillRect(0, 0, w, h);
                g.setColor(new Color(255, 255, 255, 14));
                g.fillOval(-120, h - 220, 360, 360);
                g.fillOval(w - 200, -120, 320, 320);
                g.setColor(new Color(255, 255, 255, 9));
                g.fillOval(w - 330, h - 150, 260, 260);

                Theme.paintLogo(g, w / 2 - 38, 70, 76);

                g.setColor(Color.WHITE);
                g.setFont(Theme.font(30f, Font.BOLD));
                String t = "Electricity Billing System for Company X";
                g.drawString(t, (w - g.getFontMetrics().stringWidth(t)) / 2, 192);
                g.setColor(new Color(0xBFD3F5));
                g.setFont(Theme.font(14f, Font.PLAIN));
                String s = "Smart metering  \u00B7  Simple bills  \u00B7  Easy payments";
                g.drawString(s, (w - g.getFontMetrics().stringWidth(s)) / 2, 222);
                g.dispose();
            }
        };
        art.setBounds(0, 0, 640, 380);

        Theme.Progress bar = new Theme.Progress();
        bar.setBounds(120, 292, 400, 8);
        art.add(bar);

        JLabel loading = Theme.label("Loading...", 12f, Font.PLAIN, new Color(0xBFD3F5));
        loading.setHorizontalAlignment(SwingConstants.CENTER);
        loading.setBounds(120, 308, 400, 18);
        art.add(loading);

        content.add(art);
        setVisible(true);

        final int[] tick = {0};
        final Timer timer = new Timer(30, null);
        timer.addActionListener(e -> {
            tick[0]++;
            bar.setValue(tick[0]);
            if (tick[0] >= 100) {
                timer.stop();
                dispose();
                new Login();
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Splash::new);
    }
}
