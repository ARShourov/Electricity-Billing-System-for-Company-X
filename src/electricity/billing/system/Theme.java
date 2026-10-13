package electricity.billing.system;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * One place for the whole look of the application.
 * Want a different colour scheme? Change the constants below - every screen follows.
 */
final class Theme {

    // ------------------------------------------------------------------ palette
    static final Color NAVY_DEEP   = new Color(0x0B1530);
    static final Color NAVY        = new Color(0x14284B);
    static final Color BLUE        = new Color(0x2563EB);
    static final Color BLUE_HOVER  = new Color(0x1D4ED8);
    static final Color BLUE_DOWN   = new Color(0x1E3A8A);
    static final Color BLUE_SOFT   = new Color(0xDBEAFE);
    static final Color CYAN        = new Color(0x22D3EE);
    static final Color AMBER       = new Color(0xF59E0B);
    static final Color BG          = new Color(0xF3F6FB);
    static final Color LINE        = new Color(0xE2E8F0);
    static final Color LINE_STRONG = new Color(0xCBD5E1);
    static final Color INK         = new Color(0x0F172A);
    static final Color MUTED       = new Color(0x64748B);
    static final Color GREEN       = new Color(0x15803D);
    static final Color GREEN_SOFT  = new Color(0xDCFCE7);
    static final Color RED         = new Color(0xB91C1C);
    static final Color RED_SOFT    = new Color(0xFEE2E2);
    static final Color AMBER_SOFT  = new Color(0xFEF3C7);
    static final Color BKASH       = new Color(0xE2136E);

    private Theme() { }

    // ------------------------------------------------------------------ fonts
    private static String family;

    static Font font(float size, int style) {
        if (family == null) {
            String forced = System.getProperty("billing.font");
            Set<String> have = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            String[] want = forced != null ? new String[]{forced}
                    : new String[]{"Segoe UI", "SF Pro Text", "Helvetica Neue", "Noto Sans", "Ubuntu", "DejaVu Sans"};
            family = Font.SANS_SERIF;
            for (String w : want) if (have.contains(w)) { family = w; break; }
        }
        return new Font(family, style, 1).deriveFont(size);
    }

    private static boolean installed = false;

    /** Global defaults (tooltips, tables, scroll bars ...). Safe to call many times. */
    static void install() {
        if (installed) return;
        installed = true;
        UIManager.put("ToolTip.font", font(12f, Font.PLAIN));
        UIManager.put("Label.font", font(13f, Font.PLAIN));
        UIManager.put("Table.font", font(13f, Font.PLAIN));
        UIManager.put("TextField.font", font(14f, Font.PLAIN));
    }

    static void aa(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ------------------------------------------------------------------ small helpers

    /** Position a component and add it to a null-layout parent. */
    static <T extends JComponent> T at(Container parent, T c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
        parent.add(c);
        return c;
    }

    static JLabel label(String text, float size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(size, style));
        l.setForeground(color);
        return l;
    }

    /** Small grey caption above a control. Returns the y just below the control. */
    static int labeled(Container parent, String caption, JComponent c, int x, int y, int w) {
        at(parent, label(caption, 12f, Font.BOLD, MUTED), x, y, w, 16);
        at(parent, c, x, y + 19, w, 38);
        return y + 19 + 38;
    }

    static ImageIcon logoIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        aa(g);
        paintLogo(g, 0, 0, size);
        g.dispose();
        return new ImageIcon(img);
    }

    static Image appIcon() {
        return logoIcon(64).getImage();
    }

    /** Amber rounded square with a white lightning bolt. */
    static void paintLogo(Graphics2D g, int x, int y, int s) {
        g.setPaint(new GradientPaint(x, y, new Color(0xFBBF24), x, y + s, new Color(0xF97316)));
        g.fill(new RoundRectangle2D.Float(x, y, s, s, s * 0.28f, s * 0.28f));
        Path2D bolt = new Path2D.Float();
        bolt.moveTo(x + s * 0.58, y + s * 0.14);
        bolt.lineTo(x + s * 0.28, y + s * 0.55);
        bolt.lineTo(x + s * 0.47, y + s * 0.55);
        bolt.lineTo(x + s * 0.40, y + s * 0.86);
        bolt.lineTo(x + s * 0.72, y + s * 0.42);
        bolt.lineTo(x + s * 0.53, y + s * 0.42);
        bolt.closePath();
        g.setColor(Color.WHITE);
        g.fill(bolt);
    }

    private static final Map<String, ImageIcon> ICONS = new HashMap<>();

    /** Load a classpath image and scale it smoothly (several halving steps = no jagged edges). */
    static ImageIcon icon(String resource, int size) {
        String key = resource + "@" + size;
        ImageIcon cached = ICONS.get(key);
        if (cached != null) return cached;
        ImageIcon result = new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
        try {
            URL url = ClassLoader.getSystemResource(resource);
            if (url != null) {
                BufferedImage src = ImageIO.read(url);
                int w = src.getWidth(), h = src.getHeight();
                int tw = size, th = Math.max(1, (int) Math.round(size * (double) h / w));
                BufferedImage cur = src;
                while (cur.getWidth() / 2 >= tw) {
                    cur = resample(cur, cur.getWidth() / 2, cur.getHeight() / 2);
                }
                cur = resample(cur, tw, th);
                result = new ImageIcon(cur);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ICONS.put(key, result);
        return result;
    }

    private static BufferedImage resample(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }


    /** Caption above a control, as one panel (for flow/border layouts). w <= 0 keeps the control's own width. */
    static JPanel stack(String caption, JComponent c, int w) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel cap = label(caption.isEmpty() ? " " : caption, 12f, Font.BOLD, MUTED);
        cap.setAlignmentX(Component.LEFT_ALIGNMENT);
        cap.setBorder(new EmptyBorder(0, 0, 3, 0));
        int cw = w > 0 ? w : c.getPreferredSize().width;
        c.setPreferredSize(new Dimension(cw, 38));
        c.setMaximumSize(new Dimension(cw, 38));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(cap);
        p.add(c);
        return p;
    }

    // ------------------------------------------------------------------ buttons

    enum Kind { PRIMARY, SECONDARY, DANGER, GHOST, BKASH }

    static final class Btn extends JButton {
        private final Kind kind;
        private boolean hover, down;

        Btn(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setFont(font(13f, Font.BOLD));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(9, 18, 9, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            switch (kind) {
                case PRIMARY: case DANGER: case BKASH: setForeground(Color.WHITE); break;
                case SECONDARY: setForeground(INK); break;
                default: setForeground(BLUE); break;
            }
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; down = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { down = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { down = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            Color fill, line = null;
            switch (kind) {
                case PRIMARY: fill = down ? BLUE_DOWN : hover ? BLUE_HOVER : BLUE; break;
                case DANGER:  fill = down ? new Color(0x7F1D1D) : hover ? new Color(0x991B1B) : RED; break;
                case BKASH:   fill = down ? new Color(0x9D0F4E) : hover ? new Color(0xBF0F5E) : BKASH; break;
                case SECONDARY:
                    fill = down ? new Color(0xE2E8F0) : hover ? new Color(0xF1F5F9) : Color.WHITE;
                    line = LINE_STRONG; break;
                default:
                    fill = down ? new Color(0xBFDBFE) : hover ? BLUE_SOFT : new Color(0, 0, 0, 0); break;
            }
            if (!isEnabled()) { fill = new Color(0xCBD5E1); line = null; }
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            if (line != null) {
                g.setColor(line);
                g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static Btn button(String text, Kind kind) {
        return new Btn(text, kind);
    }

    // ------------------------------------------------------------------ text fields

    private static void paintFieldBg(JTextComponent c, Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        aa(g);
        boolean editable = c.isEditable() && c.isEnabled();
        g.setColor(editable ? Color.WHITE : new Color(0xF1F5F9));
        g.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 12, 12));
        boolean focus = c.hasFocus();
        g.setColor(focus ? BLUE : LINE_STRONG);
        g.setStroke(new BasicStroke(focus ? 2f : 1f));
        float o = focus ? 1f : 0.5f;
        g.draw(new RoundRectangle2D.Float(o, o, c.getWidth() - 2 * o, c.getHeight() - 2 * o, 12, 12));
        g.dispose();
    }

    private static void styleText(JTextComponent c) {
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(6, 12, 6, 12));
        c.setFont(font(14f, Font.PLAIN));
        c.setForeground(INK);
        c.setCaretColor(BLUE);
        c.setSelectionColor(new Color(0xBFDBFE));
        c.setSelectedTextColor(INK);
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { c.repaint(); }
            @Override public void focusLost(FocusEvent e) { c.repaint(); }
        });
    }

    static final class Fld extends JTextField {
        Fld() { styleText(this); }
        @Override protected void paintComponent(Graphics g) { paintFieldBg(this, g); super.paintComponent(g); }
    }

    static final class PwdFld extends JPasswordField {
        PwdFld() { styleText(this); }
        @Override protected void paintComponent(Graphics g) { paintFieldBg(this, g); super.paintComponent(g); }
    }

    static Fld field() { return new Fld(); }

    static PwdFld password() { return new PwdFld(); }

    // ------------------------------------------------------------------ drop-down

    static final class Cmb extends JComboBox<String> {
        Cmb(String... items) {
            super(items);
            setUI(new CmbUI());
            setOpaque(false);
            setFont(font(14f, Font.PLAIN));
            setForeground(INK);
            setBorder(new EmptyBorder(0, 8, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean selected, boolean focus) {
                    JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, selected, false);
                    l.setFont(font(14f, Font.PLAIN));
                    l.setBorder(new EmptyBorder(7, 10, 7, 10));
                    l.setForeground(INK);
                    if (index == -1) {           // the closed box: let our rounded background show through
                        l.setOpaque(false);
                        l.setBorder(new EmptyBorder(0, 2, 0, 0));
                    } else {
                        l.setOpaque(true);
                        l.setBackground(selected ? BLUE_SOFT : Color.WHITE);
                    }
                    return l;
                }
            });
        }

        String value() {
            Object o = getSelectedItem();
            return o == null ? "" : o.toString();
        }

        void select(String s) { setSelectedItem(s); }

        void addOption(String s) { addItem(s); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            g.setColor(isEnabled() ? Color.WHITE : new Color(0xF1F5F9));
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            g.setColor(LINE_STRONG);
            g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static Cmb combo(String... items) { return new Cmb(items); }

    static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    private static final class CmbUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    aa(g);
                    int cx = getWidth() / 2, cy = getHeight() / 2;
                    g.setColor(MUTED);
                    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g.dispose();
                }
            };
            b.setBorder(new EmptyBorder(0, 0, 0, 0));
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setOpaque(false);
            return b;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) { }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup p = new BasicComboPopup(comboBox);
            p.setBorder(BorderFactory.createLineBorder(LINE_STRONG));
            return p;
        }
    }

    // ------------------------------------------------------------------ panels

    /** White rounded card with a soft shadow. Children start about 6px inside the edge. */
    static class Card extends JPanel {
        Card() { super(null); setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            for (int i = 0; i < 6; i++) {
                g.setColor(new Color(15, 23, 42, 7));
                g.fill(new RoundRectangle2D.Float(i, i + 2, getWidth() - 2 * i, getHeight() - 2 * i - 2, 22 - i, 22 - i));
            }
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(6, 5, getWidth() - 12, getHeight() - 12, 18, 18));
            g.dispose();
        }
    }

    static Card card() { return new Card(); }

    /** Gradient banner at the top of a window. */
    static final class Header extends JPanel {
        private final String title, subtitle;

        Header(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle == null ? "" : subtitle;
            setPreferredSize(new Dimension(10, 84));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, NAVY_DEEP, w, h, BLUE));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 14));
            g.fillOval(w - 170, -90, 260, 260);
            g.setColor(new Color(255, 255, 255, 10));
            g.fillOval(w - 290, 20, 190, 190);
            paintLogo(g, 28, (h - 44) / 2, 44);
            g.setColor(Color.WHITE);
            g.setFont(font(21f, Font.BOLD));
            int ty = subtitle.isEmpty() ? h / 2 + 8 : h / 2 - 3;
            g.drawString(title, 86, ty);
            if (!subtitle.isEmpty()) {
                g.setColor(new Color(0xBFD3F5));
                g.setFont(font(13f, Font.PLAIN));
                g.drawString(subtitle, 86, h / 2 + 18);
            }
            g.dispose();
        }
    }

    /** Left-hand brand panel used by Login and Signup. */
    static final class Brand extends JPanel {
        private final String line1, line2;
        private final String[] points;

        Brand(String line1, String line2, String... points) {
            this.line1 = line1;
            this.line2 = line2;
            this.points = points;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, NAVY_DEEP, w, h, BLUE));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 14));
            g.fillOval(-80, h - 190, 280, 280);
            g.setColor(new Color(255, 255, 255, 10));
            g.fillOval(w - 120, -70, 220, 220);
            paintLogo(g, 36, 40, 52);
            g.setColor(Color.WHITE);
            g.setFont(font(26f, Font.BOLD));
            g.drawString(line1, 36, 150);
            g.drawString(line2, 36, 183);
            g.setFont(font(13f, Font.PLAIN));
            int y = 232;
            for (String p : points) {
                g.setColor(CYAN);
                g.fillOval(36, y - 9, 8, 8);
                g.setColor(new Color(0xD6E4FA));
                g.drawString(p, 54, y);
                y += 30;
            }
            g.dispose();
        }
    }

    /** Rounded text pill (status, meter number ...). */
    static final class Pill extends JLabel {
        private Color bg;

        Pill(String text, Color fg, Color bg) {
            super(text, SwingConstants.CENTER);
            this.bg = bg;
            setForeground(fg);
            setFont(font(12f, Font.BOLD));
            setOpaque(false);
            setBorder(new EmptyBorder(3, 12, 3, 12));
        }

        void set(String text, Color fg, Color bg) {
            setText(text);
            setForeground(fg);
            this.bg = bg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            g.setColor(bg);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static Pill pill(String text, Color fg, Color bg) { return new Pill(text, fg, bg); }

    /** Add a pill to a null-layout parent, as wide as its text needs. */
    static Pill atPill(Container parent, Pill p, int x, int y) {
        return at(parent, p, x, y, p.getPreferredSize().width + 6, 26);
    }

    /** Splash screen progress bar (0..100). */
    static final class Progress extends JComponent {
        private int value;

        void setValue(int v) { value = Math.max(0, Math.min(100, v)); repaint(); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            g.setColor(new Color(255, 255, 255, 40));
            g.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
            int fw = Math.round(w * value / 100f);
            if (fw > 0) {
                g.setPaint(new GradientPaint(0, 0, CYAN, w, 0, AMBER));
                g.fill(new RoundRectangle2D.Float(0, 0, Math.max(fw, h), h, h, h));
            }
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ tables

    static void styleTable(JTable t) {
        t.setRowHeight(38);
        t.setFont(font(13f, Font.PLAIN));
        t.setForeground(INK);
        t.setBackground(Color.WHITE);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(LINE);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(BLUE_SOFT);
        t.setSelectionForeground(INK);
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.addPropertyChangeListener("model", e -> fitColumns(t));
        fitColumns(t);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(10, 42));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                           boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, col);
                l.setText(String.valueOf(value).replace('_', ' ').toUpperCase());
                l.setFont(font(11.5f, Font.BOLD));
                l.setForeground(MUTED);
                l.setBackground(new Color(0xF8FAFC));
                l.setOpaque(true);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, LINE_STRONG), new EmptyBorder(0, 14, 0, 8)));
                return l;
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                           boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, sel, false, row, col);
                l.setBorder(new EmptyBorder(0, 14, 0, 8));
                l.setFont(font(13f, Font.PLAIN));
                String s = value == null ? "" : value.toString();
                l.setForeground(INK);
                if (!sel) l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                if (s.equals("Paid")) { l.setForeground(GREEN); l.setFont(font(13f, Font.BOLD)); }
                if (s.equals("Not Paid")) { l.setForeground(RED); l.setFont(font(13f, Font.BOLD)); }
                return l;
            }
        });
    }

    static JScrollPane tablePane(JTable t) {
        styleTable(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setColumnHeaderView(t.getTableHeader());
        sp.setBorder(BorderFactory.createLineBorder(LINE));
        sp.getViewport().setBackground(Color.WHITE);
        slim(sp);
        return sp;
    }

    /** Thin, modern scroll bars. */
    static void slim(JScrollPane sp) {
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new SlimScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(10, 10));
    }

    /** Give every column the width its content needs (re-run whenever the table gets a new model). */
    private static void fitColumns(JTable t) {
        if (t.getColumnCount() == 0) return;
        FontMetrics fm = t.getFontMetrics(t.getFont());
        FontMetrics hm = t.getFontMetrics(font(11.5f, Font.BOLD));
        for (int c = 0; c < t.getColumnCount(); c++) {
            String head = String.valueOf(t.getColumnName(c)).replace('_', ' ').toUpperCase();
            int w = hm.stringWidth(head) + 40;
            for (int r = 0; r < Math.min(t.getRowCount(), 60); r++) {
                Object v = t.getValueAt(r, c);
                w = Math.max(w, fm.stringWidth(v == null ? "" : v.toString()) + 40);
            }
            t.getColumnModel().getColumn(c).setPreferredWidth(Math.min(w, 420));
        }
    }

    private static final class SlimScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = LINE_STRONG; trackColor = Color.WHITE; }

        @Override protected JButton createDecreaseButton(int o) { return zero(); }

        @Override protected JButton createIncreaseButton(int o) { return zero(); }

        private JButton zero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }

        @Override
        protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            g.setColor(isThumbRollover() ? MUTED : LINE_STRONG);
            g.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8));
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ notice popup content

    enum Tone { INFO, WARN, ERROR }

    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    /** Body of the success / warning / error popup. onOk runs when the user presses OK. */
    static JPanel noticePanel(String message, Tone tone, Runnable onOk) {
        final Color c = tone == Tone.ERROR ? new Color(0xDC2626) : tone == Tone.WARN ? AMBER : new Color(0x16A34A);
        JPanel p = new JPanel(new BorderLayout(18, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(26, 26, 18, 26));

        JComponent icon = new JComponent() {
            { setPreferredSize(new Dimension(46, 46)); }

            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                aa(g);
                g.setColor(c);
                g.fillOval(0, 0, 46, 46);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (tone == Tone.INFO) {
                    g.drawLine(13, 24, 20, 31);
                    g.drawLine(20, 31, 33, 16);
                } else if (tone == Tone.WARN) {
                    g.drawLine(23, 12, 23, 26);
                    g.drawLine(23, 33, 23, 33);
                } else {
                    g.drawLine(15, 15, 31, 31);
                    g.drawLine(31, 15, 15, 31);
                }
                g.dispose();
            }
        };
        JPanel west = new JPanel(new BorderLayout());
        west.setOpaque(false);
        west.add(icon, BorderLayout.NORTH);
        p.add(west, BorderLayout.WEST);

        String title = tone == Tone.INFO ? "Success" : tone == Tone.WARN ? "Please check" : "Something went wrong";
        JLabel text = new JLabel("<html><div style='width:250px'><b>" + title + "</b><br>"
                + "<span style='color:#475569'>" + html(message) + "</span></div></html>");
        text.setFont(font(14f, Font.PLAIN));
        text.setForeground(INK);
        p.add(text, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(18, 0, 0, 0));
        Btn ok = button("OK", Kind.PRIMARY);
        ok.addActionListener(e -> onOk.run());
        south.add(ok);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }
}
