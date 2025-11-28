import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
//import java.security.Timestamp;
import java.sql.Timestamp;


public class LoginFrame extends JFrame {
    private final ChatApp app;          // sigurohu që e ruan nga konstruktori ekzistues
    private String serverIP = "127.0.0.1";

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPassword;
    private JButton loginButton;
    private JLabel forgotLink, registerLink;
    private JToggleButton themeToggle;

    // palette
    // palette
    private boolean dark;
    private Color bg, card, fg, sub, accent, inputBg, inputBorder;

    // password echo
    private char defaultEchoChar;

    public LoginFrame(ChatApp app) {
        this.app = app;

        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { JOptionPane.showMessageDialog(this, "MySQL JDBC driver not found."); }

        this.dark = Boolean.TRUE.equals(UIManager.get("app.dark"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Login / Sign Up");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);

        applyPalette();
        setContentPane(buildRoot());
        wireEvents();
        setVisible(true);
    }

    private void applyPalette() {
        if (dark) {
            bg = new Color(20, 23, 28);
            card = new Color(28, 32, 38);
            fg = new Color(230, 233, 239);
            sub = new Color(160, 168, 180);
            accent = new Color(99, 179, 237);
            inputBg = new Color(36, 41, 48);
            inputBorder = new Color(60, 68, 80);
        } else {
            bg = new Color(245, 247, 250);
            card = Color.WHITE;
            fg = new Color(24, 28, 33);
            sub = new Color(104, 112, 118);
            accent = new Color(25, 118, 210);
            inputBg = new Color(252, 253, 255);
            inputBorder = new Color(208, 215, 222);
        }
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, ThemeManager.background(), 0, getHeight(), ThemeManager.background().darker()));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel cardPanel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.card());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(0, 0, 0, ThemeManager.isDark() ? 90 : 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(500, 300));

        GridBagConstraints R = new GridBagConstraints();
        R.insets = new Insets(8, 10, 8, 10);
        R.fill = GridBagConstraints.HORIZONTAL;
        R.weightx = 1;

        // header (only theme toggle)
        themeToggle = new JToggleButton(dark ? "☀︎" : "🌙");
        themeToggle.setSelected(dark);
        themeToggle.setFocusPainted(false);
        themeToggle.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        themeToggle.setOpaque(false);
        themeToggle.setContentAreaFilled(false);
        themeToggle.setForeground(sub);
        themeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeToggle.setToolTipText("Toggle theme");

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
        header.add(themeToggle, BorderLayout.EAST);

        R.gridx = 0; R.gridy = 0; R.gridwidth = 2;
        cardPanel.add(pad(header, 2, 8), R);

        // Username
        JLabel userLabel = label("Username:");
        R.gridwidth = 1; R.gridy = 1; R.gridx = 0; R.weightx = 0;
        cardPanel.add(userLabel, R);

        usernameField = inputField();
        R.gridx = 1; R.weightx = 1;
        cardPanel.add(usernameField, R);

        // Password
        JLabel passLabel = label("Password:");
        R.gridy = 2; R.gridx = 0; R.weightx = 0;
        cardPanel.add(passLabel, R);

        passwordField = passField();
        defaultEchoChar = passwordField.getEchoChar();
        R.gridx = 1; R.weightx = 1;
        cardPanel.add(passwordField, R);

        // Row: show password + forgot link (right)
        JPanel minorRow = new JPanel(new BorderLayout());
        minorRow.setOpaque(false);

        showPassword = new JCheckBox("Show password");
        styleMinor(showPassword);
        forgotLink = hyperlink("Forgot password?");
        minorRow.add(showPassword, BorderLayout.WEST);
        minorRow.add(forgotLink, BorderLayout.EAST);

        R.gridy = 3; R.gridx = 0; R.gridwidth = 2;
        cardPanel.add(pad(minorRow, 0, 2), R);

        // Primary Login button (single)
        loginButton = primaryButton("Login");
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(loginButton);

        R.gridy = 4; R.gridx = 0; R.gridwidth = 2;
        cardPanel.add(pad(btnWrap, 2, 6), R);

        // Footer: register hyperlink at the very end (bottom-right)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        registerLink = hyperlink("Sign up");
        footer.add(registerLink, BorderLayout.EAST);

        R.gridy = 5; R.gridx = 0; R.gridwidth = 2;
        cardPanel.add(pad(footer, 0, 8), R);

        GridBagConstraints G = new GridBagConstraints();
        root.add(cardPanel, G);
        return root;
    }

    // helpers
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(ThemeManager.subtext());
        return l;
    }

    private JTextField inputField() {
        JTextField f = new JTextField(20);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(ThemeManager.inputBg());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.inputBorder(), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        f.setForeground(ThemeManager.foreground());
        f.setCaretColor(ThemeManager.foreground());
        return f;
    }

    private JPasswordField passField() {
        JPasswordField f = new JPasswordField(20);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(ThemeManager.inputBg());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.inputBorder(), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        f.setForeground(ThemeManager.foreground());
        f.setCaretColor(ThemeManager.foreground());
        return f;
    }

    private static final Dimension BTN_SIZE = new Dimension(160, 36);
    private static final int BTN_RADIUS = 12;

    private JButton primaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color base = ThemeManager.accent();
                g2.setColor(isEnabled()
                        ? (getModel().isRollover() ? base.darker() : base)
                        : new Color(180,180,180));
                g2.fillRoundRect(0, 0, w, h, BTN_RADIUS, BTN_RADIUS);
                g2.dispose();
                super.paintComponent(g); // draws the text only (we keep opaque/contentArea off)
            }
        };
        b.setPreferredSize(BTN_SIZE);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }


    private void styleMinor(AbstractButton c) {
        c.setOpaque(false);
        c.setContentAreaFilled(false);
        c.setBorderPainted(false);
        c.setFocusPainted(false);
        c.setForeground(ThemeManager.subtext());
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JLabel hyperlink(String text) {
        JLabel link = new JLabel("<html><u>"+ text +"</u></html>");
        link.setForeground(ThemeManager.accent());
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        link.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                link.setText("<html><u><b>"+ text +"</b></u></html>");
                link.setForeground(ThemeManager.accent().darker());
            }
            @Override public void mouseExited(MouseEvent e) {
                link.setText("<html><u>"+ text +"</u></html>");
                link.setForeground(ThemeManager.accent());
            }
        });
        return link;
    }

    private JComponent pad(JComponent c, int vpad, int hpad) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(vpad, hpad, vpad, hpad));
        p.add(c);
        return p;
    }

    // events
    private void wireEvents() {
        // Enter -> login
        Action loginAction = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { attemptLogin(); }
        };
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        // show password (fixed)
        showPassword.addActionListener(e ->
                passwordField.setEchoChar(showPassword.isSelected() ? (char)0 : defaultEchoChar)
        );

        loginButton.addActionListener(e -> attemptLogin());

        // hyperlinks
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleForgotPassword(); }
        });
        registerLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openRegisterForm(); }
        });

// handler
        themeToggle.addActionListener(e -> {
            boolean d = themeToggle.isSelected();
            ThemeManager.setDarkMode(d);               // <-- central
            themeToggle.setText(d ? "☀︎" : "🌙");
            SwingUtilities.updateComponentTreeUI(this);
            repaint();                                  // no pack(), no size jump
        });


    }

    private void handleForgotPassword() {
        String user = LoginManager.showForgotPasswordDialog(this);
        if (user == null || user.isBlank()) return;

        boolean ok = LoginManager.showResetCodeInputDialog(this, "123456");
        if (!ok) return;

        String newPass = LoginManager.showPasswordResetForm(this);
        if (newPass == null) return;

        boolean updated = UserManager.updatePassword(user, newPass);
        LoginManager.showSystemNotification(this,
                updated ? "Password reset successfully." : "Failed to reset password.");
    }

    // === HELPER: thirr auth në server (me timeout) ===
    private boolean authViaServer(String ip, String user, String pass) {
        String host = (ip == null || ip.isBlank() || "0.0.0.0".equals(ip) || "localhost".equalsIgnoreCase(ip))
                ? "127.0.0.1" : ip.trim();
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, 8000), 4000); // 4s timeout
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
            PrintWriter out   = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"), true);
            out.println("AUTH|" + user + "|" + pass);
            String resp = in.readLine();
            return "AUTH_OK".equals(resp);
        } catch (IOException ioe) {
            return false;
        }
    }

    // === PAS AUTH: hap chat-in (pa JDBC në klient) ===
    private void openChat(String user) {
        UserData userData = new UserData(
                user,                        // username
                "default.png",               // avatarPath
                "user",                      // role
                "online",                    // status
                new Timestamp(System.currentTimeMillis()), // joinTime
                "",                          // roomCode (serveri do ta dërgojë më vonë)
                "",                          // firstName
                ""                           // lastName
        );
        // nëse të duhet IP e serverit diku:
       // try { userData.setIpAddress(serverIP); } catch (Throwable ignore) {}

      //  app.continueAfterLogin(userData, /*isAdmin*/ false);
        app.continueAfterLogin(userData, /*isAdmin*/ false, serverIP);
    }

    // === LOGIN pa bllokuar UI (SwingWorker) ===
    private void attemptLogin() {
        String user = usernameField.getText().trim();
        String pw   = new String(passwordField.getPassword());

        String ip = JOptionPane.showInputDialog(this, "Server IP (empty = this PC):", serverIP);
        if (ip == null) return;
        serverIP = (ip.isBlank() || "0.0.0.0".equals(ip) || "localhost".equalsIgnoreCase(ip)) ? "127.0.0.1" : ip.trim();

        // disable butonin përkohësisht
        loginButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        boolean ok = authViaServer(serverIP, user, pw);

        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "Login failed (server or credentials).",
                    "Login", JOptionPane.WARNING_MESSAGE);
        } else {
            openChat(user);
            // mbyll login frame
            setVisible(false);
            dispose();
        }

        // rikthe UI
        loginButton.setEnabled(true);
        setCursor(Cursor.getDefaultCursor());
    }

    private void openRegisterForm() {
        JDialog d = new JDialog(this, "Register", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(ThemeManager.card());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.EAST;

        // Labels
        JLabel fn = label("First Name:");
        JLabel ln = label("Last Name:");
        JLabel un = label("Username:");
        JLabel pw = label("Password:");
        JLabel sc = label("Secret Code (for reset):");

        // Fields
        JTextField nameField       = inputField();
        JTextField surnameField    = inputField();
        JTextField regUsernameField= inputField();
        JPasswordField regPasswordField = passField();
        JTextField secretCode      = secretCodeField();

        // Row 0: First Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        d.add(fn, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        d.add(nameField, gbc);

        // Row 1: Last Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        d.add(ln, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        d.add(surnameField, gbc);

        // Row 2: Username
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        d.add(un, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        d.add(regUsernameField, gbc);

        // Row 3: Password
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        d.add(pw, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        d.add(regPasswordField, gbc);

        // Row 4: Secret Code
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        d.add(sc, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        d.add(secretCode, gbc);

        // Row 5: Create button
        JButton create = primaryButton("Create Account");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        d.add(create, gbc);

        // Action
        create.addActionListener(e -> {
            String name    = safe(nameField.getText());
            String surname = safe(surnameField.getText());
            String regUser = safe(regUsernameField.getText());
            String regPass = new String(regPasswordField.getPassword());
            String scode   = safe(secretCode.getText());

            if (name.isEmpty() || surname.isEmpty() || regUser.isEmpty() || regPass.isEmpty() || scode.isEmpty()) {
                JOptionPane.showMessageDialog(d, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Store secret code (hash inside UserManager)
            boolean created = UserManager.register(regUser, regPass, name, surname, scode);
            if (!created) {
                JOptionPane.showMessageDialog(d, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(d, "User registered successfully.");
                usernameField.setText(regUser); // prefill login
                d.dispose();
            }
        });

        // Theming + layout polish
        themeDialog(d);
        d.pack();                       // size to fit new row
        d.setMinimumSize(new Dimension(420, 360));
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private JTextField secretCodeField() {
        JTextField field = new JTextField();
        field.setColumns(15);  // width
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private void themeDialog(JDialog d) {
        boolean darkNow = Boolean.TRUE.equals(UIManager.get("app.dark"));
        Color cardNow = darkNow ? new Color(28,32,38) : Color.WHITE;
        Color fgNow   = darkNow ? new Color(230,233,239) : new Color(24,28,33);

        d.getContentPane().setBackground(cardNow);
        for (Component c : d.getContentPane().getComponents()) {
            if (c instanceof JLabel) ((JLabel)c).setForeground(fgNow);
            if (c instanceof JTextField) {
                c.setBackground(darkNow ? new Color(36,41,48) : new Color(252,253,255));
                c.setForeground(fgNow);
            }
            if (c instanceof JPasswordField) {
                c.setBackground(darkNow ? new Color(36,41,48) : new Color(252,253,255));
                c.setForeground(fgNow);
            }
        }
    }

}
