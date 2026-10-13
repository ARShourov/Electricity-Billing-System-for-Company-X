package electricity.billing.system;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class main_class extends Screen implements ActionListener {
    String acctype;
    String meter_pass;

    main_class(String acctype, String meter_pass){
        super("Electricity Billing System for Company X", 1180, 720, null, null, QUIT_ON_CLOSE);
        this.meter_pass = meter_pass;
        this.acctype = acctype;
        minSize(1000, 640);
        maximize();
        boolean admin = acctype.equals("Admin");

        // what each role can do: {command, icon, one-line description}
        String[][] actions = admin
                ? new String[][]{
                    {"New Customer", "newcustomer.png", "Register a customer and their meter"},
                    {"Customer Details", "customerDetails.png", "Browse and search every customer"},
                    {"Deposit Details", "depositdetails.png", "See all bills and their payment status"},
                    {"Calculate Bill", "calculatorbills.png", "Create a monthly bill for a meter"}}
                : new String[][]{
                    {"Generate Bill", "bill.png", "View your bill for any month"},
                    {"Pay Bill", "pay.png", "Pay a bill with bKash"},
                    {"Bill Details", "detail.png", "All your bills in one table"},
                    {"View Information", "information.png", "See your account details"},
                    {"Update Information", "refresh.png", "Change address, email or phone"}};

        content.setLayout(new BorderLayout());
        content.add(buildSidebar(admin, actions), BorderLayout.WEST);
        content.add(buildMain(admin, actions), BorderLayout.CENTER);

        setVisible(true);
    }

    // ------------------------------------------------------------------ sidebar

    private JPanel buildSidebar(boolean admin, String[][] actions) {
        JPanel side = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setPaint(new GradientPaint(0, 0, Theme.NAVY_DEEP, 0, getHeight(), Theme.NAVY));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(250, 10));

        JPanel head = new JPanel(null) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.aa(g);
                Theme.paintLogo(g, 22, 24, 40);
                g.setColor(Color.WHITE);
                g.setFont(Theme.font(17f, Font.BOLD));
                g.drawString("Electricity", 74, 40);
                g.setColor(new Color(0x9DB6E0));
                g.setFont(Theme.font(12f, Font.PLAIN));
                g.drawString("Billing System", 74, 58);
                g.dispose();
            }
        };
        head.setOpaque(false);
        head.setPreferredSize(new Dimension(250, 118));
        String role = admin ? "Administrator" : "Customer  \u00B7  Meter " + meter_pass;
        Theme.Pill pill = Theme.pill(role, Color.WHITE, new Color(255, 255, 255, 36));
        Theme.atPill(head, pill, 22, 76);
        side.add(head, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.add(sectionLabel(admin ? "MANAGE" : "MY ACCOUNT"));
        for (String[] a : actions) nav.add(nav(a[0], a[0], a[1]));
        nav.add(Box.createVerticalStrut(14));
        nav.add(sectionLabel("UTILITIES"));
        nav.add(nav("Notepad", "Notepad", "notepad.png"));
        nav.add(nav("Calculator", "Calculator", "calculator.png"));
        side.add(nav, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(0, 0, 18, 0));
        foot.add(nav("Logout", "Exit", "exit.png"), BorderLayout.CENTER);
        side.add(foot, BorderLayout.SOUTH);
        return side;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = Theme.label(text, 11f, Font.BOLD, new Color(0x7F97C2));
        l.setBorder(new EmptyBorder(8, 24, 6, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return l;
    }

    private JButton nav(String text, String command, String iconFile) {
        JButton b = new JButton(text) {
            private boolean hover;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g0) {
                if (hover) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Theme.aa(g);
                    g.setColor(new Color(255, 255, 255, 30));
                    g.fill(new RoundRectangle2D.Float(12, 2, getWidth() - 24, getHeight() - 4, 12, 12));
                    g.dispose();
                }
                super.paintComponent(g0);
            }
        };
        b.setActionCommand(command);
        b.setIcon(Theme.icon("icon/" + iconFile, 24));
        b.setIconTextGap(14);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(Theme.font(14f, Font.PLAIN));
        b.setForeground(new Color(0xE6EEFB));
        b.setBorder(new EmptyBorder(0, 26, 0, 12));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setPreferredSize(new Dimension(250, 44));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.addActionListener(this);
        return b;
    }

    // ------------------------------------------------------------------ main area

    private JPanel buildMain(boolean admin, String[][] actions) {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);
        main.setBorder(new EmptyBorder(26, 32, 26, 32));

        // greeting
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel hello = Theme.label("Welcome back, " + displayName(admin), 26f, Font.BOLD, Theme.INK);
        JLabel date = Theme.label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)),
                13f, Font.PLAIN, Theme.MUTED);
        hello.setAlignmentX(Component.LEFT_ALIGNMENT);
        date.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(hello);
        top.add(Box.createVerticalStrut(4));
        top.add(date);
        main.add(top, BorderLayout.NORTH);

        List<String[]> bills = loadBills(admin);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.BOTH;

        // summary cards
        JPanel stats = new JPanel(new GridLayout(1, 0, 18, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(10, 104));
        stats.setMinimumSize(new Dimension(10, 104));
        int unpaid = 0, due = 0;
        for (String[] b : bills) {
            if (b[4].equals("Not Paid")) { unpaid++; due += num(b[3]); }
        }
        if (admin) {
            stats.add(new Stat("Customers", String.valueOf(countCustomers()), "newcustomer.png", Theme.BLUE));
            stats.add(new Stat("Bills issued", String.valueOf(bills.size()), "detail.png", new Color(0x7C3AED)));
            stats.add(new Stat("Unpaid bills", String.valueOf(unpaid), "pay.png", Theme.RED));
            stats.add(new Stat("Amount due", String.valueOf(due), "budget.png", new Color(0xD97706)));
        } else {
            stats.add(new Stat("Bills issued", String.valueOf(bills.size()), "detail.png", Theme.BLUE));
            stats.add(new Stat("Unpaid bills", String.valueOf(unpaid), "pay.png", Theme.RED));
            stats.add(new Stat("Amount due", String.valueOf(due), "budget.png", new Color(0xD97706)));
        }
        gc.gridy = 0;
        gc.weighty = 0;
        gc.insets = new Insets(0, 0, 22, 0);
        body.add(stats, gc);

        // quick actions
        JPanel tilesWrap = new JPanel(new BorderLayout());
        tilesWrap.setOpaque(false);
        JLabel qa = Theme.label("Quick actions", 16f, Font.BOLD, Theme.INK);
        qa.setBorder(new EmptyBorder(0, 2, 10, 0));
        tilesWrap.add(qa, BorderLayout.NORTH);
        JPanel tiles = new JPanel(new GridLayout(1, actions.length, 18, 0));
        tiles.setOpaque(false);
        tiles.setPreferredSize(new Dimension(10, 176));
        tiles.setMinimumSize(new Dimension(10, 176));
        for (String[] a : actions) tiles.add(new Tile(a[0], a[2], a[1]));
        tilesWrap.add(tiles, BorderLayout.CENTER);
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 22, 0);
        body.add(tilesWrap, gc);

        // recent bills
        Theme.Card recent = Theme.card();
        recent.setLayout(new BorderLayout(0, 10));
        recent.setBorder(new EmptyBorder(18, 24, 20, 24));
        recent.setPreferredSize(new Dimension(10, 260));
        recent.setMinimumSize(new Dimension(10, 170));
        recent.add(Theme.label("Recent bills", 16f, Font.BOLD, Theme.INK), BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(new Object[]{"meter_no", "month", "unit", "total_bill", "status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        int shown = 0;
        for (int i = bills.size() - 1; i >= 0 && shown < 6; i--, shown++) model.addRow(bills.get(i));
        JTable table = new JTable(model);
        recent.add(Theme.tablePane(table), BorderLayout.CENTER);
        gc.gridy = 2;
        gc.weighty = 1;
        gc.insets = new Insets(0, 0, 0, 0);
        body.add(recent, gc);

        main.add(body, BorderLayout.CENTER);
        return main;
    }

    private String displayName(boolean admin) {
        if (admin) return "Admin";
        try {
            database c = new database();
            ResultSet rs = c.statement.executeQuery("select * from new_customer where meter_no = '" + meter_pass + "'");
            if (rs.next()) return rs.getString("name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Customer";
    }

    private int countCustomers() {
        int n = 0;
        try {
            database c = new database();
            ResultSet rs = c.statement.executeQuery("select * from new_customer");
            while (rs.next()) n++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return n;
    }

    /** Rows of the bill table as {meter_no, month, unit, total_bill, status}; admins see all, customers their own. */
    private List<String[]> loadBills(boolean admin) {
        List<String[]> rows = new ArrayList<>();
        try {
            database c = new database();
            String q = admin ? "select * from bill" : "select * from bill where meter_no = '" + meter_pass + "'";
            ResultSet rs = c.statement.executeQuery(q);
            while (rs.next()) {
                rows.add(new String[]{rs.getString("meter_no"), rs.getString("month"), rs.getString("unit"),
                        rs.getString("total_bill"), rs.getString("status")});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static int num(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    // ------------------------------------------------------------------ dashboard widgets

    /** A number card: caption, big value, coloured icon chip. */
    private static final class Stat extends Theme.Card {
        private final String caption, value;
        private final Color accent;
        private final ImageIcon icon;

        Stat(String caption, String value, String iconFile, Color accent) {
            this.caption = caption;
            this.value = value;
            this.accent = accent;
            this.icon = Theme.icon("icon/" + iconFile, 30);
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Theme.aa(g);
            g.setColor(accent);
            g.fill(new RoundRectangle2D.Float(6, 26, 4, getHeight() - 52, 4, 4));
            g.setColor(Theme.MUTED);
            g.setFont(Theme.font(12.5f, Font.PLAIN));
            g.drawString(caption, 28, 38);
            g.setColor(Theme.INK);
            g.setFont(Theme.font(30f, Font.BOLD));
            g.drawString(value, 28, 76);
            g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 26));
            g.fill(new RoundRectangle2D.Float(getWidth() - 64, 26, 44, 44, 14, 14));
            icon.paintIcon(this, g, getWidth() - 57, 33);
            g.dispose();
        }
    }

    /** Big clickable card for a quick action. */
    private final class Tile extends JButton {
        private final String title, desc;
        private final ImageIcon icon;
        private boolean hover;

        Tile(String title, String desc, String iconFile) {
            this.title = title;
            this.desc = desc;
            this.icon = Theme.icon("icon/" + iconFile, 52);
            setActionCommand(title);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(main_class.this);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.aa(g);
            int w = getWidth(), h = getHeight();
            for (int i = 0; i < 5; i++) {
                g.setColor(new Color(15, 23, 42, hover ? 9 : 6));
                g.fill(new RoundRectangle2D.Float(i, i + 2, w - 2 * i, h - 2 * i - 2, 22 - i, 22 - i));
            }
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(5, 4, w - 10, h - 10, 18, 18));
            if (hover) {
                g.setColor(Theme.BLUE);
                g.setStroke(new BasicStroke(2f));
                g.draw(new RoundRectangle2D.Float(6, 5, w - 12, h - 12, 17, 17));
            }
            icon.paintIcon(this, g, 24, 22);
            g.setColor(Theme.INK);
            String shown = title.replace(" Information", " Info");
            float size = 15.5f;
            g.setFont(Theme.font(size, Font.BOLD));
            while (size > 11f && g.getFontMetrics().stringWidth(shown) > w - 40) {
                size -= 0.5f;
                g.setFont(Theme.font(size, Font.BOLD));
            }
            g.drawString(shown, 24, 104);
            g.setColor(Theme.MUTED);
            g.setFont(Theme.font(12.5f, Font.PLAIN));
            FontMetrics fm = g.getFontMetrics();
            int y = 126, maxW = w - 48;
            StringBuilder line = new StringBuilder();
            for (String word : desc.split(" ")) {
                String tryLine = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(tryLine) > maxW && line.length() > 0) {
                    g.drawString(line.toString(), 24, y);
                    y += 18;
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(tryLine);
                }
            }
            g.drawString(line.toString(), 24, y);
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ actions

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = e.getActionCommand();
        if (msg.equals("New Customer")) {
            new newCustomer();
        } else if (msg.equals("Customer Details")) {
          new customer_details();
        } else if (msg.equals("Deposit Details")) {
            new deposit_details();
        } else if (msg.equals("Calculate Bill")) {
            new calculate_bill();
        } else if (msg.equals("View Information")) {
            new view_information(meter_pass);
        } else if (msg.equals("Update Information")) {
            new update_information(meter_pass);
        } else if (msg.equals("Bill Details")) {
            new bill_details(meter_pass);
        } else if (msg.equals("Calculator")) {
            try{
                new ProcessBuilder("calc.exe").start();
            }catch (Exception E){
                E.printStackTrace();
                warn("The calculator can only be opened on Windows.");
            }
        } else if (msg.equals("Notepad")) {
            try{
                new ProcessBuilder("notepad.exe").start();
            }catch (Exception E){
                E.printStackTrace();
                warn("Notepad can only be opened on Windows.");
            }
        } else if (msg.equals("Exit")) {
            setVisible(false);
            new Login();
        } else if (msg.equals("Pay Bill")) {
            new pay_bill(meter_pass);
        } else if (msg.equals("Generate Bill")) {
            new generate_bill(meter_pass);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new main_class("Admin",""));
    }
}