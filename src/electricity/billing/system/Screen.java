package electricity.billing.system;

import javax.swing.*;
import java.awt.*;

/**
 * Base class of every window: themed page, optional gradient header, popups.
 * Width/height are the size of the window CONTENT (without the title bar).
 */
abstract class Screen extends JFrame {
    static final int FIXED = 1, UNDECORATED = 2, QUIT_ON_CLOSE = 4;

    /** A modal popup. show() blocks until close() is called. */
    interface Popup {
        void show();
        void close();
    }

    final JPanel page = new JPanel(new BorderLayout());
    final JPanel content = new JPanel(null);

    Screen(String title, int w, int h, String heading, String subtitle, int flags) {
        super(title);
        Theme.install();
        if ((flags & UNDECORATED) != 0) setUndecorated(true);
        if ((flags & FIXED) != 0) setResizable(false);
        if ((flags & QUIT_ON_CLOSE) != 0) setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        page.setBackground(Theme.BG);
        content.setBackground(Theme.BG);
        if (heading != null) page.add(new Theme.Header(heading, subtitle), BorderLayout.NORTH);
        page.add(content, BorderLayout.CENTER);
        page.setPreferredSize(new Dimension(w, h));
        setContentPane(page);
        setIconImage(Theme.appIcon());
        pack();
        setLocationRelativeTo(null);
    }

    void info(String message)  { notice(message, Theme.Tone.INFO); }
    void warn(String message)  { notice(message, Theme.Tone.WARN); }
    void error(String message) { notice(message, Theme.Tone.ERROR); }

    void notice(String message, Theme.Tone tone) {
        final Popup[] ref = new Popup[1];
        JPanel body = Theme.noticePanel(message, tone, () -> ref[0].close());
        ref[0] = popup("Electricity Billing System for Company X", body);
        ref[0].show();
    }

    Popup popup(String title, JComponent body) {
        final JDialog d = new JDialog(this, title, true);
        d.setContentPane(body);
        d.setResizable(false);
        d.pack();
        d.setLocationRelativeTo(this);
        return new Popup() {
            @Override public void show() { d.setVisible(true); }
            @Override public void close() { d.dispose(); }
        };
    }

    void defaultButton(JButton b) { getRootPane().setDefaultButton(b); }

    void exitApp() { System.exit(0); }

    void minSize(int w, int h) { setMinimumSize(new Dimension(w, h)); }

    void maximize() { setExtendedState(JFrame.MAXIMIZED_BOTH); }
}