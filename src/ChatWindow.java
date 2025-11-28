import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.*;

public class ChatWindow extends JFrame {
    private JTabbedPane tabbedPane;
    private JTextField inputField;
    private JButton sendButton;
    private JPanel sidebar;
    private UserData user;
    private ChatClient client;
    private JButton settingsButton;
    private JLabel roomCodeLabel;
    private JScrollPane groupScroll;
    private String groupName;
    private JLabel usernameLabel;
    private JPanel chatArea;

    // at top of ChatWindow
    private JPanel avatarPanel;
    private String userStatus = "online"; // use whatever you track

    private Map<String, ChatBubblePanel> chatPanels = new HashMap<>();
    private final Map<String, ImageIcon> userAvatars = new HashMap<>();
    private static final String PLACEHOLDER = "Type here...";


    public ChatWindow(ChatClient client, UserData user) {
        this(user);
        this.client = client;
        this.client.attachUI(this); // <- keep this
    }

    public ChatWindow(UserData user) {
        this.user = user;
        setTitle("Chat - " + user.getUsername() + (user.getRole().equals("admin") ? " (admin)" : ""));
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                leaveApp(); // this calls client.disconnect(...), which sends LEAVE
            }
        });

        setLayout(new BorderLayout());

        boolean dark = ThemeManager.isDark();
        getContentPane().setBackground(ThemeManager.background());
// use ThemeManager.card()/foreground()/subtext()/etc. wherever you set colors

// Sidebar
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(210, getHeight()));
        sidebar.setBackground(ThemeManager.sidebarColor());
        sidebar.setBorder(new EmptyBorder(20, 0, 0, 0)); // move everything down ~14px

// Profile Avatar
        final int AV_SIZE  = 65;
        final int RING_PAD = 5;
        final int PANEL    = AV_SIZE + 2*RING_PAD + 44;

        avatarPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth()/2, cy = getHeight()/2;
                int drawSize = AV_SIZE;
                int imgX = cx - drawSize/2, imgY = cy - drawSize/2;

                // >>> LIVE fetch — no frozen 'me' variable <<<
                Image meNow = getAvatarForUser(user.getUsername(), AV_SIZE);

                Shape old = g2.getClip();
                g2.setClip(new Ellipse2D.Float(imgX, imgY, drawSize, drawSize));
                g2.drawImage(meNow, imgX, imgY, this);
                g2.setClip(old);

                int baseR = drawSize/2 + RING_PAD;
                AnimationEngine.drawPulsingStatus(g2, cx, cy, baseR, userStatus, System.currentTimeMillis());
                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setLayout(null);
        avatarPanel.setPreferredSize(new Dimension(PANEL, PANEL));
        avatarPanel.setMaximumSize(new Dimension(PANEL, PANEL));
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(avatarPanel);

// animate
        new Timer(33, e -> avatarPanel.repaint()).start();

// username
        usernameLabel = new JLabel("@" + user.getUsername());
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));

// Room Code
        String rawCode = user.getRoomCode();
        String formattedCode = (rawCode != null && rawCode.length() == 6)
                ? "Room Code: " + rawCode.substring(0, 3) + " " + rawCode.substring(3)
                : "Room Code: unknown";

        roomCodeLabel = new JLabel(formattedCode);
        // right after you build roomCodeLabel and add to sidebar:
        roomCodeLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        roomCodeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateRoomCode(user.getRoomCode());


// Buttons
        JButton addUserBtn = new JButton("+");
        addUserBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addUserBtn.setPreferredSize(new Dimension(45, 30));
       // addUserBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        addUserBtn.setFont(new Font("Dialog", Font.PLAIN, 16));


        settingsButton = new JButton("⚙");
       // settingsButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsButton.setPreferredSize(new Dimension(45, 30));
        settingsButton.setFont(new Font("Dialog", Font.PLAIN, 16));




// Layout order → Room Code goes near bottom
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(usernameLabel);
        sidebar.add(Box.createVerticalGlue());   // push next items down
        sidebar.add(roomCodeLabel);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(addUserBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(settingsButton);
        sidebar.add(Box.createVerticalStrut(10));

        add(sidebar, BorderLayout.WEST);

// Emoji Button
        JButton emojiButton = new JButton("😊");
        emojiButton.setPreferredSize(new Dimension(50, 50));
        emojiButton.setFocusable(false);
        emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        ///emojiButton.setFont(new Font("Dialog", Font.PLAIN, 16));


// was: input dialog
        emojiButton.addActionListener(e -> showEmojiPicker(emojiButton)); //

// Tabs
        tabbedPane = new JTabbedPane();
        ThemeManager.applyTabbedPaneColors(tabbedPane);

        ChatBubblePanel groupPanel = new ChatBubblePanel();
        groupPanel.setSelf(user.getUsername(), user.getStatus());
        this.groupScroll = buildScroll(groupPanel);

        tabbedPane.addTab("Group", this.groupScroll);
        chatPanels.put("Group", groupPanel);
        makeClosable("Group");

// INPUT CHAT: wrap chat in its own center panel

        chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(ThemeManager.sidebarColor());
    //    chatArea.add(tabbedPane, BorderLayout.CENTER);
        JPanel tabWrap = new JPanel(new BorderLayout());
        tabWrap.setBackground(ThemeManager.sidebarColor()); //same color as sideBar
// LEFT, RIGHT, BOTTOM = 1px using your theme-aware border color

        tabWrap.add(tabbedPane, BorderLayout.CENTER);
        chatArea.add(tabWrap, BorderLayout.CENTER);
        add(chatArea, BorderLayout.CENTER);

// Input Field (placeholder)
     //   private static final String PLACEHOLDER = "Type here...";

        inputField = new JTextField();
        inputField.setFont(ThemeManager.firstAvailableFont(13));
              //  Font.ROMAN_BASELINE, 14, "Segoe UI Emoji","Apple Color Emoji","Noto Color Emoji","Cascadia Mono", "Consolas", "Liberation Mono", "Courier New", "monospace"));
        showPlaceholder();                       // put the ghost text in once

// Clear placeholder on focus
        inputField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (isPlaceholder()) clearPlaceholder();
            }
            @Override public void focusLost(FocusEvent e) {
                if (inputField.getText().isEmpty()) showPlaceholder();
            }
        });

// Also clear on first click (covers cases where focus was already there)
        inputField.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (isPlaceholder()) clearPlaceholder();
            }
        });

// Also clear on first keystroke (belt + suspenders)
        inputField.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (isPlaceholder()) clearPlaceholder();
            }
        });

// Send Button
     //   sendButton = new JButton("➤");
      //  sendButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        //sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        //sendButton.setFont(new Font("Dialog", Font.PLAIN, 16));
        sendButton = new JButton("Send ➤");
        sendButton.setFont(new Font("Dialog", Font.PLAIN, 13));
        sendButton.setEnabled(false);

// Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(emojiButton, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
      //  add(inputPanel, BorderLayout.SOUTH);
        chatArea.add(inputPanel, BorderLayout.SOUTH);
        inputPanel.setBackground(ThemeManager.background());
        inputPanel.setPreferredSize(new Dimension(0, 78)); // consistent height

// Enable/disable Send Button (exclude placeholder)
        final Color disabledBg = ThemeManager.isDark() ? new Color(55,55,55) : new Color(235,235,235);
        final Color disabledFg = ThemeManager.isDark() ? new Color(150,150,150) : new Color(150,150,150);

        inputField.getDocument().addDocumentListener(
                LoginManager.createDocumentListener(() -> {
                    String t = inputField.getText();
                    boolean ok = t != null && !t.trim().isEmpty() && !isPlaceholder();
                    sendButton.setEnabled(ok);
                    if (ok) {
                        sendButton.setBackground(ThemeManager.UI_BLUE);
                        sendButton.setForeground(Color.BLACK);
                    } else {
                        sendButton.setBackground(disabledBg);
                        sendButton.setForeground(disabledFg);
                    }
                })
        );
/*
        try {
            this.groupName = (user.getRoomCode() != null) ? "Room: " + user.getRoomCode() : "unknown";
            client = new ChatClient("localhost", 8000, user.getUsername(), user, this.groupName);
            client.setChatWindow(this); // ky e bën handleIncoming të thirret në këtë ChatWindow
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection failed.");
            System.exit(1);
        }
*/
        this.groupName = (user.getRoomCode() != null) ? ("Room " + user.getRoomCode()) : "unknown"; //no socket

        sendButton.addActionListener(e -> send());
        inputField.addActionListener(e -> sendButton.doClick());

        addUserBtn.addActionListener(e -> ProfileSidebar.showAddMenu(this, addUserBtn));
        settingsButton.addActionListener(e -> ProfileSidebar.showSettingsMenu(this, settingsButton));

        // after creating inputField and sendButton
        // after creating inputField and sendButton
        inputField.getDocument().addDocumentListener(
                LoginManager.createDocumentListener(() -> {
                    String t = inputField.getText();
                    boolean ok = t != null && !t.trim().isEmpty() && !isPlaceholder();
                    sendButton.setEnabled(ok);
                })
        ); //Made the button to ignore the placeholder and whitespace.
        // a very subtle 1px line (white-ish in dark, dark-ish in light)
        Color line = ThemeManager.isDark() ? new Color(255,255,255,40) : new Color(0,0,0,50);

        //add hover efektin
        Color normal = ThemeManager.UI_BLUE;        // #BBD4E5
        Color hover  = ThemeManager.UI_BLUE_HOVER;  // #9ABED5

// ---- Sidebar button style (applies to both Add & Settings) ----
        Dimension SIDEBAR_BTN = new Dimension(40, 34); // square size

        JButton[] sidebarBtns = { addUserBtn, settingsButton };
        for (JButton b : sidebarBtns) {
            b.setPreferredSize(SIDEBAR_BTN);
            b.setMaximumSize(SIDEBAR_BTN);
            b.setMinimumSize(SIDEBAR_BTN);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);

            b.setUI(new BasicButtonUI());
            b.setRolloverEnabled(false);
            b.setFocusable(false);
            b.setContentAreaFilled(true);
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setFocusPainted(false);

            b.setBackground(normal);
            b.setBorder(new LineBorder(ThemeManager.borderColor(), 2, true));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            b.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e){ b.setBackground(hover); }
                @Override public void mouseExited (MouseEvent e){ b.setBackground(normal); }
            });
        }
// --- Send ---
        sendButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        sendButton.setRolloverEnabled(false);
        sendButton.setFocusable(false);
        sendButton.setContentAreaFilled(true);
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setBackground(disabledBg);          // start disabled
        sendButton.setForeground(disabledFg);

// hover only when enabled
        sendButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e){
                if (sendButton.isEnabled()) sendButton.setBackground(ThemeManager.UI_BLUE_HOVER);
            }
            @Override public void mouseExited (MouseEvent e){
                if (sendButton.isEnabled()) sendButton.setBackground(ThemeManager.UI_BLUE);
            }
        });

// --- Emoji ---
        emojiButton.setUI(new BasicButtonUI());
        emojiButton.setRolloverEnabled(false);
        emojiButton.setOpaque(true);
        emojiButton.setContentAreaFilled(true);
        emojiButton.setBorderPainted(false);
        emojiButton.setFocusPainted(false);
        emojiButton.setBackground(normal);
        emojiButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e){ emojiButton.setBackground(hover); }
            @Override public void mouseExited (MouseEvent e){ emojiButton.setBackground(normal); }
        });

        tabWrap.setBorder(BorderFactory.createMatteBorder(0,1,1,1, ThemeManager.borderColor()));
        inputPanel.setBorder(BorderFactory.createMatteBorder(1,1,1,1, ThemeManager.borderColor()));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.borderColor()),
                BorderFactory.createEmptyBorder(8,12,8,12)
        ));
        sendButton.setBorder(BorderFactory.createLineBorder(ThemeManager.borderColor(), 1, true));
        emojiButton.setBorder(BorderFactory.createLineBorder(ThemeManager.borderColor(), 1, true));

        updateTheme();
    }


    public void handleIncoming(String msg) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> handleIncoming(msg));
            return;
        }
        if (msg == null || msg.isEmpty()) return;
        // 1) ROOM_CODE
        if (msg.startsWith("ROOM_CODE|")) {
            String code = msg.substring("ROOM_CODE|".length()).trim();
            updateRoomCode(code);
            ensureGroupTabAfterHandshake(code);  // <-- make/rename the tab immediately
            return;
        }

        if (msg.startsWith("JOIN_ACCEPTED|")) {
            String[] parts = msg.split("\\|", 3);
            String code = parts.length > 1 ? parts[1] : "";
            String name = parts.length > 2 ? parts[2] : "";

            // Finalize the group tab title and mapping
            String cleaned = code == null ? "" : code.replaceAll("\\s+","");
            String pretty  = cleaned.length()==6 ? cleaned.substring(0,3)+" "+cleaned.substring(3) : code;
            String label   = (name == null || name.isBlank()) ? ("Room " + pretty) : name;
            String finalTitle = "Group: " + label;

            int idx = findGroupTabIndex();
            if (idx < 0) {
                // No group tab yet (rare): create it now
                ChatBubblePanel p = new ChatBubblePanel();
                wirePanelDefaults(p);
                JScrollPane sp = buildScroll(p);
                tabbedPane.addTab(finalTitle, sp);
                chatPanels.put(finalTitle, p);
                makeClosable(finalTitle);
                idx = tabbedPane.getTabCount() - 1;
            } else {
                // Rename existing tab and fix the map key
                String oldTitle = tabbedPane.getTitleAt(idx);
                JScrollPane sp  = (JScrollPane) tabbedPane.getComponentAt(idx);
                ChatBubblePanel p = (ChatBubblePanel) sp.getViewport().getView();

                chatPanels.remove(oldTitle);
                chatPanels.put(finalTitle, p);
                tabbedPane.setTitleAt(idx, finalTitle);
                makeClosable(finalTitle);
            }

            tabbedPane.setSelectedIndex(idx);
            clearGroupChatView(finalTitle);              // optional: fresh timeline
            if (client != null) client.setRoomName(label);  // so sendGroupMessage() uses correct name
            LoginManager.showSystemNotification(this, "Joined " + label + " (" + pretty + ")");
            return;
        }


// LEGACY: some server paths send ROOM_JOINED|<user>
// Treat it as "you are now in the room" and create/rename tab
        if (msg.startsWith("ROOM_JOINED|")) {
            // We don't need the user; use our current room code/name
            String code = (user != null) ? user.getRoomCode() : "";
            String name = (groupName != null && !groupName.isBlank())
                    ? groupName
                    : (code != null && code.replaceAll("\\s+","").length()==6
                    ? "Room " + code.replaceAll("\\s+","").substring(0,3) + " " + code.replaceAll("\\s+","").substring(3)
                    : "Group");
            onGroupJoined(code, name);
            return;
        }


        // 2) SYSTEM TOAST
        if (msg.startsWith("SYSTEM:")) {
            LoginManager.showSystemNotification(this, msg.substring(7).trim());
            return;
        }

        // 3) SIMPLE NOTIFY
        if (msg.startsWith("🔔")) {
            LoginManager.showSystemNotification(this, msg.substring(1).trim());
            return;
        }

        // 4) JOIN REQUEST UI (incoming approval prompt)
        if (msg.startsWith("JOIN_REQUEST:")) {
            String joinerUsername = msg.substring("JOIN_REQUEST:".length()).trim();

            Image av = getAvatarForUser(joinerUsername);
            Icon icon = (av != null)
                    ? new ImageIcon(av.getScaledInstance(48, 48, Image.SCALE_SMOOTH))
                    : UIManager.getIcon("OptionPane.questionIcon");

            int opt = JOptionPane.showConfirmDialog(
                    this,
                    "@" + joinerUsername + " wants to join the room. Approve?",
                    "Join Request",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    icon
            );
            if (opt == JOptionPane.YES_OPTION) client.sendJoinApproved(joinerUsername);
            else                               client.sendJoinDeclined(joinerUsername);
            return;
        }

        if (msg.startsWith("JOIN_DECLINED|")) {
            String who = msg.split("\\|", 2)[1];
            LoginManager.showSystemNotification(this, "Join request declined by @" + who);
            return;
        }
        if (msg.startsWith("GROUP_RENAMED|")) {
            String n = msg.split("\\|",2)[1];
            setGroupName(n);
            return;
        }

        // 6) PRIVATE ENDED (other side closed)
        if (msg.startsWith("PRIVATE_ENDED|")) {
            String other = msg.split("\\|", 2)[1];
            int idx = indexOfTabByBaseName(other);
            if (idx >= 0) tabbedPane.removeTabAt(idx);
            LoginManager.showSystemNotification(this, "@" + other + " left the private chat.");
            return;
        }

        // 7) PRIVATE END CONFIRMATION (server ack for my leave)
        if (msg.startsWith("PRIVATE_END_CONFIRMATION|")) {
            String other = msg.split("\\|", 2)[1];
            // tab was already closed locally in leavePrivate()
            LoginManager.showSystemNotification(this, "You left the chat with @" + other + ".");
            return;
        }

        // 8) PRIVATE MESSAGE
        if (msg.startsWith("PRIVATE|")) {
            String[] parts = msg.split("\\|", 4);
            if (parts.length < 4) return;
            String sender   = parts[1];
            String receiver = parts[2];
            String text     = parts[3];

            if (sender.equals(receiver)) {
                if (receiver.equals(user.getUsername())) {
                    JOptionPane.showMessageDialog(this, "You cannot send a private message to yourself.");
                }
                return;
            }
            if (receiver.equals(user.getUsername())) {
                ChatBubblePanel panel = ensurePrivatePanel(sender);
                Image avatar = getAvatarForUser(sender);
                panel.addMessage(sender, text, false, avatar);
                scrollTabToBottom(sender);
            }
            return;
        }

        // 9) PRIVATE REQUEST (incoming DM request)
        if (msg.startsWith("PRIVATE_REQUEST|")) {
            String sender = msg.split("\\|", 2)[1];
            int option = JOptionPane.showConfirmDialog(
                    this, sender + " wants to chat privately. Accept?",
                    "Private Chat", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                client.sendPrivateApproved(sender);
                createPrivateTab(sender);
            } else {
                client.sendPrivateDeclined(sender);
            }
            return;
        }

        // 10) PRIVATE APPROVED / DECLINED
        if (msg.startsWith("PRIVATE_APPROVED|")) {
            String target = msg.split("\\|", 2)[1];
            if (client != null) client.onPrivateApproved(target);

            int idx = indexOfTabByBaseName(target);
            if (idx >= 0) {
                tabbedPane.setTitleAt(idx, target);
                makeClosable(target);
            } else {
                createPrivateTab(target);
            }
            LoginManager.showSystemNotification(this, "@" + target + " joined the chat.");
            return;
        }
        if (msg.startsWith("PRIVATE_DECLINED|")) {
            String target = msg.split("\\|", 2)[1];
            int idx = indexOfTabByBaseName(target);
            if (idx >= 0) tabbedPane.removeTabAt(idx);
            LoginManager.showSystemNotification(this, "@" + target + " declined your private chat.");
            return;
        }

        // 11) INVITE
        if (msg.startsWith("INVITE|")) {
            String[] parts = msg.split("\\|", 3);
            if (parts.length < 2) return;
            String rawCode = parts[1] != null ? parts[1].trim() : "";
            String inviter = parts.length > 2 ? parts[2] : "unknown";

            String cleaned = rawCode.replaceAll("\\s+", "");
            String pretty  = (cleaned.matches("\\d{6}"))
                    ? cleaned.substring(0,3) + " " + cleaned.substring(3)
                    : rawCode;

            int opt = JOptionPane.showConfirmDialog(
                    this,
                    "@" + inviter + " invited you to join room " + pretty + ". Accept?",
                    "Room Invite",
                    JOptionPane.YES_NO_OPTION
            );

            if (opt == JOptionPane.YES_OPTION && client != null) client.requestJoinGroup(cleaned);
            return;
        }

        // 12) GROUP fallback: "sender: text"
        String[] p = msg.split(":", 2);
        if (p.length == 2) {
            String sender = p[0].trim();
            String text   = p[1].trim();
            if (!sender.equals(user.getUsername())) {
                ChatBubblePanel group = ensureGroupPanel();
                Image avatar = getAvatarForUser(sender);
                group.addMessage(sender, text, false, avatar);
                scrollGroupToBottom();
            }
        }
    }
// Build a configured scroll for any ChatBubblePanel
    private JScrollPane buildScroll(ChatBubblePanel panel) {
        JScrollPane sp = new JScrollPane(
                panel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
      //  ThemeManager.styleScrollPane(sp);   //blue border
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.borderColor()));

        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                panel.revalidate();
                panel.repaint();
            }
        });
        return sp;
    }


    public void setStatus(String status) {
        if (status == null || status.isBlank()) status = "offline";
        status = status.toLowerCase().trim();
        if (!status.equals("online") && !status.equals("away") && !status.equals("busy") && !status.equals("offline")) {
            status = "offline";
        }

        // 1) local UI state
        this.userStatus = status;
        if (this.user != null) this.user.setStatus(status);

        // 2) refresh UI bits
        for (ChatBubblePanel p : chatPanels.values()) p.setSelf(user.getUsername(), status);
        if (avatarPanel != null) avatarPanel.repaint();

        // 3) persist to DB (one line)
        boolean ok = UserManager.setStatus(user.getUsername(), status);
        if (!ok) {
            LoginManager.showSystemNotification(this, "Couldn't save status.");
        } else {
            LoginManager.showSystemNotification(this, "Status set to " + status + ".");
        }
    }


    //addin x-is
// thirre SA HERË që shton një tab privat

    private void wirePanelDefaults(ChatBubblePanel p) {
        p.setTheme(ThemeManager.background(), ThemeManager.foreground());
        if (user != null) p.setSelf(user.getUsername(), user.getStatus());
    }

    private void createPrivateTab(String username) {
        if (!chatPanels.containsKey(username)) {
            ChatBubblePanel panel = new ChatBubblePanel();
            wirePanelDefaults(panel);                    // <-- add
            JScrollPane sp = buildScroll(panel);
            tabbedPane.addTab(username, sp);
            chatPanels.put(username, panel);
            makeClosable(username);
        }
    }
    private ChatBubblePanel ensurePrivatePanel(String username) {
        ChatBubblePanel p = chatPanels.get(username);
        if (p == null) {
            p = new ChatBubblePanel();
            wirePanelDefaults(p);
            JScrollPane sp = buildScroll(p);
            tabbedPane.addTab(username, sp);
            chatPanels.put(username, p);
            makeClosable(username);
        }
        return p;
    }

    private void ensurePrivateTabPending(String username) {
        if (chatPanels.containsKey(username)) return;

        ChatBubblePanel panel = new ChatBubblePanel();
        wirePanelDefaults(panel);
        JScrollPane sp = buildScroll(panel);

        String title = username + " (pending)";
        tabbedPane.addTab(title, sp);
        chatPanels.put(username, panel);
        makeClosable(title);

        // auto-select the new tab
        tabbedPane.setSelectedComponent(sp);
    }

    private ChatBubblePanel ensureGroupPanel() {
        int idx = findGroupTabIndex();
        if (idx >= 0) {
            JScrollPane sp = (JScrollPane) tabbedPane.getComponentAt(idx);
            return (ChatBubblePanel) sp.getViewport().getView();
        }
        // krijo bazën "Group" nëse s'ekziston
        ChatBubblePanel p = new ChatBubblePanel();
        wirePanelDefaults(p);
        JScrollPane sc = buildScroll(p);
        tabbedPane.addTab("Group", sc);
        chatPanels.put("Group", p);
        makeClosable("Group");
        return p;
    }

    // ensure Group tab exists and return its panel
    private ChatBubblePanel ensureGroupTab(String groupName) {
        String newTitle = "Group: " + (groupName == null || groupName.isBlank() ? "Group" : groupName.trim());

        // if a Group* tab exists, reuse it (and fix the map key)
        int idx = findGroupTabIndex();
        if (idx >= 0) {
            JScrollPane sp = (JScrollPane) tabbedPane.getComponentAt(idx);
            ChatBubblePanel p = (ChatBubblePanel) sp.getViewport().getView();

            String oldTitle = tabbedPane.getTitleAt(idx);
            if (!newTitle.equals(oldTitle)) {
                chatPanels.remove(oldTitle);
                chatPanels.put(newTitle, p);
                tabbedPane.setTitleAt(idx, newTitle);
                makeClosable(newTitle);
            }
            tabbedPane.setSelectedIndex(idx);
            return p;
        }

        // otherwise create it
        ChatBubblePanel p = new ChatBubblePanel();
        wirePanelDefaults(p);
        JScrollPane sc = buildScroll(p);
        tabbedPane.addTab(newTitle, sc);
        chatPanels.put(newTitle, p);
        makeClosable(newTitle);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        return p;
    }

    // ChatWindow.java
// Create or rename/select a "Group: <something>" tab right after the handshake.
// Safe to call multiple times.
    private void ensureGroupTabAfterHandshake(String something) {
        String cleaned = (something == null) ? "" : something.replaceAll("\\s+", "");
        String preferred =
                (client != null && client.getRoomName() != null && !client.getRoomName().isBlank())
                        ? "Group: " + client.getRoomName()
                        : (cleaned.isEmpty() ? "Group" : "Group: " + cleaned);

        // Do we already have ANY tab that starts with "Group"?
        int idx = findGroupTabIndex();
        if (idx < 0) {
            // Create new group tab
            ChatBubblePanel p = new ChatBubblePanel();
            wirePanelDefaults(p);
            JScrollPane sp = buildScroll(p);
            tabbedPane.addTab(preferred, sp);
            chatPanels.put(preferred, p);
            makeClosable(preferred);
            tabbedPane.setSelectedComponent(sp);
            return;
        }

        // If the existing is exactly "Group", rename once to a better label.
        String oldTitle = tabbedPane.getTitleAt(idx);
        if ("Group".equals(oldTitle) && !oldTitle.equals(preferred)) {
            JScrollPane sp = (JScrollPane) tabbedPane.getComponentAt(idx);
            ChatBubblePanel p = (ChatBubblePanel) sp.getViewport().getView();

            chatPanels.remove(oldTitle);
            chatPanels.put(preferred, p);
            tabbedPane.setTitleAt(idx, preferred);
            makeClosable(preferred);
        }

        tabbedPane.setSelectedIndex(idx);
    }

    public void onGroupJoined(String code, String name) {
        SwingUtilities.invokeLater(() -> {
            updateRoomCode(code);

            String cleaned = code == null ? "" : code.replaceAll("\\s+","");
            String pretty  = cleaned.length()==6 ? cleaned.substring(0,3)+" "+cleaned.substring(3) : String.valueOf(code);
            String label   = (name == null || name.isBlank()) ? ("Room " + pretty) : name;
            String newTitle = "Group: " + label;

            int idx = findGroupTabIndex(); // çdo tab që fillon me "Group"
            if (idx < 0) {
                // s’ka fare tab -> krijoje
                ChatBubblePanel p = new ChatBubblePanel();
                wirePanelDefaults(p);
                JScrollPane sp = buildScroll(p);
                tabbedPane.addTab(newTitle, sp);
                chatPanels.put(newTitle, p);
                makeClosable(newTitle);
                idx = tabbedPane.getTabCount() - 1;
            } else {
                // riemëro ekzistuesin dhe rregullo key në map
                String oldTitle = tabbedPane.getTitleAt(idx);
                JScrollPane sp  = (JScrollPane) tabbedPane.getComponentAt(idx);
                ChatBubblePanel p = (ChatBubblePanel) sp.getViewport().getView();

                chatPanels.remove(oldTitle);
                chatPanels.put(newTitle, p);

                tabbedPane.setTitleAt(idx, newTitle);
                makeClosable(newTitle);
            }

            tabbedPane.setSelectedIndex(idx);
            clearGroupChatView(newTitle);       // opsionale: boshatis feed-in
            if (client != null) client.setRoomName(label); // që sendGroupMessage të ketë emrin e saktë
            LoginManager.showSystemNotification(this, "Joined " + label + " (" + pretty + ")");
        });
    }

    // ChatWindow.java
    private void createOrSelectGroupTab(String title){
        ChatBubblePanel p = chatPanels.get(title);
        if (p == null) {
            p = new ChatBubblePanel();
            wirePanelDefaults(p);
            JScrollPane sp = buildScroll(p);
            tabbedPane.addTab(title, sp);
            chatPanels.put(title, p);
            makeClosable(title);
        }
        int idx = tabbedPane.indexOfTab(title);
        if (idx >= 0) tabbedPane.setSelectedIndex(idx);
    }

    // I ri: pastron tab-in sipas titullit (p.sh. "Group: Alpha")
    private void clearGroupChatView(String tabTitle) {
        ChatBubblePanel panel = chatPanels.get(tabTitle);
        if (panel != null) {
            panel.removeAll();
            panel.revalidate();
            panel.repaint();
        }
    }

    private void scrollTabToBottom(String title) {
        int idx = tabbedPane.indexOfTab(title);
        if (idx < 0) return;
        Component c = tabbedPane.getComponentAt(idx);
        if (c instanceof JScrollPane sp) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar v = sp.getVerticalScrollBar();
                v.setValue(v.getMaximum());
            });
        }
    }

    private void scrollGroupToBottom() {
        int idx = findGroupTabIndex();
        if (idx < 0) return;
        Component c = tabbedPane.getComponentAt(idx);
        if (c instanceof JScrollPane sp) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar v = sp.getVerticalScrollBar();
                v.setValue(v.getMaximum());
            });
        }
    }
    // helper minimal
    private void makeClosable(String title) {
        int idx = tabbedPane.indexOfTab(title);
        if (idx < 0) return;

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        header.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setForeground(ThemeManager.foreground()); // keep text readable on both themes
        header.add(lbl);

        JButton x = new JButton("\u2715"); // ✕
        x.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // kill LAF background/gradient
        x.setFocusable(false);
        x.setMargin(new Insets(0, 0, 0, 0));
        x.setBorder(BorderFactory.createEmptyBorder());
        x.setContentAreaFilled(false);   // no fill
        x.setBorderPainted(false);       // no border
        x.setFocusPainted(false);        // no focus ring
        x.setOpaque(false);              // transparent
        x.setPreferredSize(new Dimension(18, 16));

        // subtle hover color
        Color normal = ThemeManager.isDark() ? new Color(210,210,210) : new Color(90,90,90);
        Color hover  = new Color(220,70,70);
        x.setForeground(normal);
        x.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { x.setForeground(hover); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { x.setForeground(normal); }
        });

        x.addActionListener(e -> {
            if (title != null && title.startsWith("Group")) {
                leaveGroupTab();
            } else {
                String other = normalizeUserTitle(title);
                leavePrivate(other);
            }
        });

        header.add(x);
        tabbedPane.setTabComponentAt(idx, header);
    }


    public void onPrivateEnded(String otherUser) {
        int idx = indexOfTabByBaseName(otherUser);   // <— was: indexOfTab(otherUser)
        if (idx >= 0) tabbedPane.removeTabAt(idx);
        LoginManager.showSystemNotification(this, "Private chat with @" + otherUser + " ended.");
    }

    // If the tab is "alice (pending)", return "alice"
    private String normalizeUserTitle(String title){
        return title != null && title.endsWith(" (pending)")
                ? title.substring(0, title.length() - " (pending)".length())
                : title;
    }

    // Find either "bob" or "bob (pending)"
    private int indexOfTabByBaseName(String base){
        for (int i = 0; i < tabbedPane.getTabCount(); i++){
            String t = tabbedPane.getTitleAt(i);
            if (t.equals(base) || t.equals(base + " (pending)")) return i;
        }
        return -1;
    }


    //avatar
// Emoji picker
    private void showEmojiPicker(Component anchor) {
        String[] EMOJIS = {
                "😀","😁","😂","🤣","😊","😍","😘","😎","🤩","😉",
                "🙂","🙃","😇","🥰","😋","😜","🤪","🤗","🤔","🤨",
                "😐","😑","😶","😏","😴","🤤","🤯","😬","😮‍💨","😮",
                "😭","😢","😤","😡","🤬","😱","😳","🥵","🥶","🤒",
                "👍","👎","👌","✌️","🙏","👏","🙌","💪","✅","❌",
                "❤️","🧡","💛","💚","💙","💜","🖤","🤍","💖","💔",
                "🎉","🎂","🎁","✨","🔥","⭐","⚠️","❓","📎","📷"
        };

        JPopupMenu popup = new JPopupMenu();
        popup.setOpaque(true);
        popup.setBackground(ThemeManager.sidebarColor());
        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.isDark() ? new Color(70,70,70) : new Color(200,200,200)),
                BorderFactory.createEmptyBorder(8,8,8,8)
        ));

        JPanel grid = new JPanel(new GridLayout(6, 10, 6, 6));
        grid.setOpaque(true);
        grid.setBackground(ThemeManager.sidebarColor());

        for (String emoji : EMOJIS) {
            JButton b = new JButton(emoji);
            b.setFocusable(false);
            b.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            b.setFont(ThemeManager.firstAvailableFont(17));
            //Font.PLAIN, 18, "Segoe UI Emoji","Apple Color Emoji","Noto Color Emoji","Segoe UI","Arial Unicode MS"));
            b.setContentAreaFilled(false);
            b.setOpaque(false);

            b.addActionListener(ev -> {
                if (isPlaceholder()) clearPlaceholder();   // <-- important
                insertAtCaret(inputField, emoji);
                popup.setVisible(false);
                inputField.requestFocusInWindow();
            });

            grid.add(b);
        }

        popup.add(grid);
        popup.show(anchor, 0, anchor.getHeight());
    }

    private static void insertAtCaret(JTextField field, String s) {
        int pos = field.getCaretPosition();
        String t = field.getText();
        field.setText(t.substring(0, pos) + s + t.substring(pos));
        field.setCaretPosition(pos + s.length());
    }
    // cache per-size
    private final java.util.Map<String, Image> avatarCache = new java.util.HashMap<>();

    // lexim nga resources: fillimisht avatar specifik, pastaj default
// in ChatWindow
    private BufferedImage loadAvatarHD(String username) {
        try {
            if (user != null && username.equalsIgnoreCase(user.getUsername())) {
                String p = user.getAvatarPath();
                if (p != null && !p.isBlank()) {
                    var u = getClass().getClassLoader().getResource(p);
                    if (u != null) return ImageIO.read(u);
                }
            }
        } catch (Exception ignore) {}

        String[] candidates = {
                "avatars_hd/" + username + ".png",
                "avatars_hd/default.png",
                "Avatar/1.png"
        };
        for (String path : candidates) {
            try {
                var url = getClass().getClassLoader().getResource(path);
                if (url != null) return ImageIO.read(url);
            } catch (Exception ignore) {}
        }
        return new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
    }


    /*Fixed*/

    // put near other fields/methods
    private boolean ensureConnected() {
        if (client == null) {
            LoginManager.showSystemNotification(this, "Not connected to server.");
            return false;
        }
        return true;
    }

    private void leavePrivate(String otherUser) {
        int ok = JOptionPane.showConfirmDialog(this,
                "Leave the private chat with " + otherUser + "?", "Confirm",
                JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        if (ensureConnected()) {
            client.leavePrivateChat(otherUser, user.getUsername());
        }

        // CLOSE THE TAB regardless of "(pending)" suffix
        int idx = indexOfTabByBaseName(otherUser);   // <— was: tabbedPane.indexOfTab(otherUser)
        if (idx >= 0) tabbedPane.removeTabAt(idx);

        LoginManager.showSystemNotification(this, "You left the chat with @" + otherUser + ".");
    }


    private void leaveGroupTab() {
        int ok = JOptionPane.showConfirmDialog(this, "Leave the group chat?", "Confirm",
                JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        if (ensureConnected()) {
            client.sendMessage("LEAVE_GROUP");   // authoritative command, no new method needed
        }
/*
if (ensureConnected()) {
    client.sendMessage("SYSTEM: " + user.getUsername() + " left the group");
}

* */
        //  int idx = tabbedPane.indexOfTab("Group");
        int idx = findGroupTabIndex();
        if (idx >= 0) tabbedPane.removeTabAt(idx);
        if (tabbedPane.getTabCount() > 0) tabbedPane.setSelectedIndex(0);
        LoginManager.showSystemNotification(this, "You left the group.");
    }

    public void leaveApp(boolean force) {
        if (!force) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Leave the program and disconnect?", "Confirm",
                    JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
        }
        if (ensureConnected()) {
            client.disconnect(true, null, user.getUsername());
        }
        LoginManager.showSystemNotification(this, "Disconnected from server.");
        dispose();
    }

    public void leaveApp() {
        leaveApp(false);
    }

    private void send() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || isPlaceholder()) return;
        if (!ensureConnected()) return;

        int sel = tabbedPane.getSelectedIndex();
        if (sel < 0) return;

        String currentTab = tabbedPane.getTitleAt(sel);
        boolean isGroup = currentTab != null && currentTab.startsWith("Group");
        Image avatar = getAvatarForUser(user.getUsername());

        if (isGroup) {
            // ensure we have a group tab + panel to write into
            JScrollPane sp = null;
            ChatBubblePanel gp = null;

            Component comp = tabbedPane.getComponentAt(sel);
            if (comp instanceof JScrollPane s) {
                sp = s;
                gp = (ChatBubblePanel) s.getViewport().getView();
            } else {
                // build or find the proper "Group: <label>" tab
                String label = (client != null && client.getRoomName() != null && !client.getRoomName().isBlank())
                        ? client.getRoomName()
                        : ((groupName != null && !groupName.isBlank()) ? groupName : "Group");
                String title = label.startsWith("Group") ? label : ("Group: " + label);

                gp = chatPanels.get(title);
                if (gp == null) {
                    gp = new ChatBubblePanel();
                    wirePanelDefaults(gp);
                    sp = buildScroll(gp);
                    tabbedPane.addTab(title, sp);
                    chatPanels.put(title, gp);
                    makeClosable(title);
                    tabbedPane.setSelectedComponent(sp);
                } else {
                    int idx = tabbedPane.indexOfTab(title);
                    if (idx >= 0) {
                        sp = (JScrollPane) tabbedPane.getComponentAt(idx);
                        tabbedPane.setSelectedIndex(idx);
                    }
                }
            }

            if (sp == null || gp == null) return;

            client.sendGroupMessage(text);
            gp.addMessage(user.getUsername(), text, true, avatar);

            final JScrollPane spRef = sp; // effectively final for lambda
            SwingUtilities.invokeLater(() -> {
                JScrollBar v = spRef.getVerticalScrollBar();
                v.setValue(v.getMaximum());
            });
        } else {
            // Private: create tab if missing (as before)
            String other = normalizeUserTitle(currentTab);
            ChatBubblePanel pp = chatPanels.get(other);
            if (pp == null) {
                pp = new ChatBubblePanel();
                wirePanelDefaults(pp);
                JScrollPane sc = new JScrollPane(pp,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                tabbedPane.addTab(other, sc);
                chatPanels.put(other, pp);
                makeClosable(other);
            }

            if (client.canSendPrivate(other)) {
                client.sendPrivateMessage(other, text);
                pp.addMessage(user.getUsername(), text, true, avatar);
            } else {
                pp.addMessage(user.getUsername(), "⏳ " + text, true, avatar);
            }

            JScrollPane sc = (JScrollPane) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            SwingUtilities.invokeLater(() -> {
                JScrollBar v = sc.getVerticalScrollBar();
                v.setValue(v.getMaximum());
            });
        }

        // clear input for both cases
        inputField.setText("");
        showPlaceholder();
        sendButton.setEnabled(false);
    }

    // downscale me cilësi të lartë (multi-step) -> shmang “mario pixels”
    // merre avatarin në madhësinë që kërkon sidebar-i (p.sh. 65px), me cache
// default-size convenience overload
    private Image getAvatarForUser(String username) {
        return getAvatarForUser(username, 48);
    }

    private Image getAvatarForUser(String username, int size) {
        // ⇩ live for current user (no cache)
        if (user != null && username.equalsIgnoreCase(user.getUsername())) {
            BufferedImage src = loadAvatarHD(username);   // must read from user.getAvatarPath()
            return scaleDownHQ(src, size);
        }

        String key = username + "#" + size;
        Image cached = avatarCache.get(key);
        if (cached != null) return cached;

        BufferedImage src = loadAvatarHD(username);
        BufferedImage dst = scaleDownHQ(src, size);
        avatarCache.put(key, dst);
        return dst;
    }

    // high‑quality downscale used by getAvatarForUser(...)
    private static BufferedImage scaleDownHQ(BufferedImage src, int size) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage img = src;

        // downscale in halves until close to target
        while (w / 2 >= size && h / 2 >= size) {
            w /= 2; h /= 2;
            BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = tmp.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(img, 0, 0, w, h, null);
            g.dispose();
            img = tmp;
        }

        // final pass to exact target size
        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(img, 0, 0, size, size, null);
        g.dispose();
        return dst;
    }

    private int allowedUsers;
    private String[] allowedIPs;

    public void setRoomAccessControl(int allowedUsers, String[] allowedIPs, String roomName) {
        this.allowedUsers = allowedUsers;
        this.allowedIPs   = allowedIPs;
        setGroupName(roomName); // vetëm tab-i ndryshon
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
        setGroupTabTitleFromName(groupName);
    }

    // gjen indeksin e tab-it që fillon me "Group"
    private int findGroupTabIndex() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            String t = tabbedPane.getTitleAt(i);
            if (t != null && t.startsWith("Group")) return i;
        }
        return -1;
    }

    private void setGroupTabTitleFromName(String name) {
        int idx = findGroupTabIndex();
        if (idx >= 0) {
            String title = (name == null || name.isBlank()) ? "Group" : "Group: " + name;
            tabbedPane.setTitleAt(idx, title);
            makeClosable(title); // rindërton header-in me X me titullin e ri
        }
    }

    public void updateTheme() {
        Color bg = ThemeManager.background();
        Color fg = ThemeManager.foreground();
        Color border = ThemeManager.isDark() ? new Color(255,255,255,40) : new Color(0,0,0,50);

        getContentPane().setBackground(bg);
        sidebar.setBackground(ThemeManager.sidebarColor());
        if (chatArea != null) chatArea.setBackground(bg);

        if (usernameLabel != null) usernameLabel.setForeground(fg);
        if (roomCodeLabel != null)
            roomCodeLabel.setForeground(ThemeManager.isDark() ? new Color(200,200,200) : new Color(120,120,120));

        inputField.setBackground(bg);
        inputField.setForeground(fg);
        inputField.setCaretColor(fg);
        if (isPlaceholder()) inputField.setForeground(placeholderColor());

        tabbedPane.setOpaque(true);
        tabbedPane.setBackground(bg);
        tabbedPane.setForeground(null);
        tabbedPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, border)); // subtle line under tabs
        ThemeManager.applyTabbedPaneColors(tabbedPane);
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component c = tabbedPane.getComponentAt(i);
            if (c instanceof JScrollPane sp) ThemeManager.styleScrollPane(sp);
        }
        if (groupScroll != null) ThemeManager.styleScrollPane(groupScroll);

        // bubble panels
        for (ChatBubblePanel p : chatPanels.values()) p.setTheme(bg, fg);

        revalidate();
        repaint();
    }

    private static String formatRoomCode(String raw) {
        if (raw == null || raw.isEmpty()) return "Room Code: unknown";
        String cleaned = raw.replaceAll("\\s+", "");
        return cleaned.length() == 6
                ? "Room Code: " + cleaned.substring(0,3) + " " + cleaned.substring(3)
                : "Room Code: " + raw;
    }

    public void updateRoomCode(String code) {
        if (roomCodeLabel != null) {
            roomCodeLabel.setText(formatRoomCode(code));
            roomCodeLabel.revalidate();
            roomCodeLabel.repaint();
        }
        if (user != null) user.setRoomCode(code == null ? null : code.replaceAll("\\s+", ""));
    }

    // inside ChatWindow class (anywhere among methods)
    public UserData getUser() {
        return user;
    }
    private Color placeholderColor() {
        return ThemeManager.isDark() ? new Color(160,160,160) : Color.GRAY;
    }
    private boolean isPlaceholder() {
        return PLACEHOLDER.equals(inputField.getText());
    }
    private void showPlaceholder() {
        inputField.setForeground(placeholderColor());
        inputField.setText(PLACEHOLDER);
    }
    private void clearPlaceholder() {
        inputField.setForeground(ThemeManager.foreground());
        inputField.setText("");
    }
}
