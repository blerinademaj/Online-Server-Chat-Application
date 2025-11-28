import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Profile dialog + central menus (+ flows) for ChatWindow.
 * - View/edit avatar
 * - Settings menu (profile, status, theme, exit)
 * - Add menu (start private, create group, join group)
 *
 * Notes:
 * - Works even if ChatWindow keeps 'client' private (uses reflection).
 * - Tries to call optional helpers (ensurePrivateTabPending, clearGroupChatView) via reflection if present.
 * - Expects LoginManager, ThemeManager, UserManager, ChatClient, UserData, ChatWindow to exist.
 */
public class ProfileSidebar extends JDialog {
    private final ChatWindow ownerWin;
    private final UserData user;
    private JLabel avatarLabel;
    private ButtonGroup avatarGroup;
    // fields you already have / need:
    // Available avatar resources (classpath)
    private static final String[] AVATARS = {
            "Avatar/1.png","Avatar/2.png","Avatar/3.png","Avatar/4.png",
            "Avatar/5.png","Avatar/6.png","Avatar/7.png","Avatar/8.jpg","Avatar/9.png"
    };

    // ---------- Public static MENUS ----------

    public static void showSettingsMenu(ChatWindow owner, Component anchor) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem profile = new JMenuItem("View Profile");
        profile.addActionListener(e -> new ProfileSidebar(owner, owner.getUser()).setVisible(true));

        JMenuItem status = new JMenuItem("Change Status…");
        status.addActionListener(e -> {
            String[] statuses = {"online","busy","away","offline"};
            String current = (owner.getUser() != null && owner.getUser().getStatus() != null)
                    ? owner.getUser().getStatus() : "online";
            String choice = (String) JOptionPane.showInputDialog(
                    owner, "Set your status:", "Status",
                    JOptionPane.QUESTION_MESSAGE, null, statuses, current
            );
            if (choice != null && !choice.isBlank() && !choice.equalsIgnoreCase(current)) {
                owner.setStatus(choice); // ChatWindow updates ring + panels
                LoginManager.showSystemNotification(owner, "Status set to " + choice + ".");
            }
        });

        JMenuItem theme = new JMenuItem("Switch Theme");
        theme.addActionListener(e -> { ThemeManager.toggle(); owner.updateTheme(); });

        JMenuItem leave = new JMenuItem("Disconnect/Exit");
        leave.addActionListener(e -> owner.leaveApp(true));

        menu.add(profile);
        menu.add(status);
        menu.add(theme);
        menu.add(leave);
        menu.show(anchor, 0, anchor.getHeight());
    }

    public static void showAddMenu(ChatWindow owner, Component anchor) {
        String[] actions = {
                "Start private chat",
                "Start a new group",
                "Join group",
                "Invite to Chat Room"
        };

        String choice = (String) JOptionPane.showInputDialog(
                owner,                       // parent for centering
                "Choose what you want to do:",
                "Add",
                JOptionPane.QUESTION_MESSAGE,
                null,                        // no icon
                actions,                     // options (rows)
                actions[0]                   // default
        );

        if (choice == null) return;

        switch (choice) {
            case "Start private chat" -> startPrivateFlow(owner);
            case "Start a new group"  -> createNewGroupFlow(owner);
            case "Join group"         -> joinGroupFlow(owner);
            case "Invite to Chat Room"-> inviteToRoomFlow(owner);
        }
    }

    public static void startPrivateFlow(ChatWindow owner) {
        UserData u = owner.getUser();

        String target = JOptionPane.showInputDialog(owner, "Username:");
        if (target == null) return;                    // user canceled → silent return

        target = target.trim();
        if (target.isEmpty()) {                        // whitespace or empty
            JOptionPane.showMessageDialog(owner, "❌ Username can't be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (u != null && target.equalsIgnoreCase(u.getUsername())) {
            JOptionPane.showMessageDialog(owner, "You can’t DM yourself.");
            return;
        }
        if (!UserManager.userExists(target)) {
            JOptionPane.showMessageDialog(owner, "❌ User does not exist.");
            return;
        }

        ChatClient c = client(owner);
        if (!ensureConnected(owner, c)) return;

        if (c.canSendPrivate(target)) {
            JOptionPane.showMessageDialog(owner, "You already have an active private chat with @" + target + ".");
            return;
        }

        if (!c.sendPrivateRequest(target)) {
            JOptionPane.showMessageDialog(owner, "Request not sent.");
            return;
        }

        // Optional: mark pending tab if ChatWindow exposes it
        invokeIfExists(owner, "ensurePrivateTabPending", new Class[]{String.class}, new Object[]{target});

        LoginManager.showSystemNotification(owner, "Request sent to @" + target + ". Waiting for approval.");
    }

    public static void createNewGroupFlow(ChatWindow owner) {
        String name = JOptionPane.showInputDialog(owner, "Enter group name:", "New Group", JOptionPane.PLAIN_MESSAGE);
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "Group name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String code = String.format("%06d", (int)(Math.random() * 1_000_000));
        String pretty = code.substring(0,3) + " " + code.substring(3);

        int ok = JOptionPane.showConfirmDialog(
                owner,
                "Create group:\nName: " + name + "\nCode: " + pretty,
                "Confirm",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (ok != JOptionPane.OK_OPTION) return;

        ChatClient c = client(owner);
        if (!ensureConnected(owner, c)) return;

        c.createGroup(code, name);
        c.joinRoom(code); // make sure we're in

        // reflect UI immediately; server will also push ROOM_CODE later
        owner.updateRoomCode(code);
        owner.setGroupName(name);
        invokeIfExists(owner, "clearGroupChatView", new Class[]{}, new Object[]{});
        LoginManager.showSystemNotification(owner, "Group created: " + name + " (" + pretty + ")");
    }

    public static void joinGroupFlow(ChatWindow owner) {
        String input = JOptionPane.showInputDialog(owner, "Enter group code (e.g., 123 456 or 123456):");
        if (input == null) return;
        String code = input.replaceAll("\\s+", "");
        if (!code.matches("\\d{6}")) {
            JOptionPane.showMessageDialog(owner, "Invalid code. Use 6 digits.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ChatClient c = client(owner);
        if (!ensureConnected(owner, c)) return;

        c.requestJoinGroup(code);
        String pretty = code.substring(0,3) + " " + code.substring(3);
        LoginManager.showSystemNotification(owner, "Join request sent for room " + pretty + ".");
    }

    private static void inviteToRoomFlow(ChatWindow owner) {
        String code = currentRoomCode(owner);
        if (code == null) {
            JOptionPane.showMessageDialog(owner, "No active chat room to invite to.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String target = JOptionPane.showInputDialog(owner,
                "Invite username to this room (" + pretty(code) + "):");
        if (target == null) return;
        target = target.trim();
        if (target.isEmpty()) return;

        if (!UserManager.userExists(target)) {
            JOptionPane.showMessageDialog(owner, "❌ User does not exist.");
            return;
        }

        ChatClient c = client(owner);
        if (!ensureConnected(owner, c)) return;

        // If your ChatClient supports direct invites, call it.
        try {
            c.sendGroupInvite(target, code);
            LoginManager.showSystemNotification(owner,
                    "Invite sent to @" + target + " for room " + pretty(code) + ".");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(owner,
                    "Invite not supported on server. Share this code manually: " + pretty(code),
                    "Invite", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // helpers:
    private static String currentRoomCode(ChatWindow owner) {
        UserData u = owner.getUser();
        if (u == null) return null;
        String rc = u.getRoomCode();
        if (rc == null) return null;
        rc = rc.replaceAll("\\s+", "");
        return rc.matches("\\d{6}") ? rc : null;
    }
    private static String pretty(String code) {
        return (code != null && code.length() == 6) ? code.substring(0,3) + " " + code.substring(3) : String.valueOf(code);
    }


    // ---------- Dialog (view/edit profile) ----------

    public ProfileSidebar(ChatWindow parent, UserData user) {
        super(parent, "User Profile", true);
        this.ownerWin = parent;
        this.user = user;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel nameLabel = new JLabel("@" + user.getUsername());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(12));

        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.X_AXIS));
        avatarGroup = new ButtonGroup();

        for (String file : AVATARS) {
            ImageIcon base = safeIcon(file);
            Image scaled = base.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            JToggleButton btn = new JToggleButton(new ImageIcon(scaled));
            btn.setPreferredSize(new Dimension(54, 54));
            btn.setFocusable(false);
            if (file.equals(user.getAvatarPath())) btn.setSelected(true);
            btn.addActionListener(e -> {
                user.setAvatarPath(file);
                setAvatarIcon(file);
            });
            avatarGroup.add(btn);
            avatarPanel.add(btn);
            avatarPanel.add(Box.createHorizontalStrut(4));
        }

        JScrollPane avatarScroll = new JScrollPane(
                avatarPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        avatarScroll.setPreferredSize(new Dimension(300, 70));
        avatarScroll.setBorder(null);
        content.add(avatarScroll);

        avatarLabel = new JLabel();
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarLabel.setPreferredSize(new Dimension(80, 80));
        setAvatarIcon(user.getAvatarPath());
        content.add(Box.createVerticalStrut(8));
        content.add(avatarLabel);

        content.add(Box.createVerticalStrut(12));

        JLabel roleLabel = new JLabel("Role: " + nullSafe(user.getRole()));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(roleLabel);
        content.add(Box.createVerticalStrut(3));

        JLabel statusLabel = new JLabel("Status: " + nullSafe(user.getStatus()));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);

        content.add(Box.createVerticalGlue());

        JLabel joinedLabel = new JLabel("Joined: " + formatDate(user.getJoinTime()));
        joinedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roomLabel = new JLabel("Room: " + nullSafe(user.getRoomCode()));
        roomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(Box.createVerticalStrut(12));
        content.add(joinedLabel);
        content.add(roomLabel);

      //  JPanel changeBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton changeBtn = new JButton("Change Data");
        changeBtn.addActionListener(e -> {
            Object[] options = { "Change Username", "Change Password", "Cancel" };
            int choice = JOptionPane.showOptionDialog(
                    this, "Select what you want to change:", "Change Data",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]
            );
            switch (choice) {
                case 0 -> changeUsername(this);
                case 1 -> changePassword(this);
            }
        });

        JButton saveBtn = new JButton("Save");    // only avatar
        saveBtn.addActionListener(e -> onSave());

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(closeBtn);
        btnPanel.add(changeBtn);

        add(content, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

    }

    private static String nullSafe(String s) {
        return (s == null || s.isBlank()) ? "unknown" : s;
    }

    private ImageIcon safeIcon(String resourcePath) {
        java.net.URL url = getClass().getClassLoader().getResource(resourcePath);
        if (url == null) {
            // fallback to first avatar if missing
            url = getClass().getClassLoader().getResource(AVATARS[0]);
        }
        return new ImageIcon(url);
    }

    private void setAvatarIcon(String file) {
        ImageIcon icon = safeIcon(file == null ? AVATARS[0] : file);
        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        avatarLabel.setIcon(new ImageIcon(img));
    }
// Change Data button stays as you wrote (two options).

    private void changeUsername(Component owner) {
        // current -> proposed
        final String oldU = user.getUsername();
        String newU = JOptionPane.showInputDialog(owner, "Enter new username:", oldU);
        if (newU == null) return;                 // input canceled
        newU = newU.trim();
        if (newU.isEmpty() || newU.equalsIgnoreCase(oldU)) return;

        // confirm BEFORE any DB write
        int confirm = JOptionPane.showConfirmDialog(
                owner,
                "Changing username requires reconnect.\nProceed?",
                "Confirm",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) return;  // cancel = do nothing

        // write to DB now (atomic)
        boolean ok = UserManager.changeUsername(oldU, newU);
        if (!ok) {
            JOptionPane.showMessageDialog(owner, "Username already taken or DB error.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // success → notify + forced disconnect (no extra prompt)
        JOptionPane.showMessageDialog(owner, "Username updated to @" + newU, "Notification", JOptionPane.INFORMATION_MESSAGE);
        JOptionPane.showMessageDialog(owner,"You have been signed out due to username change.\nPlease log in again....","Reconnecting...",  JOptionPane.WARNING_MESSAGE);
        // IMPORTANT: do NOT mutate user.setUsername(...) here.
        ownerWin.leaveApp(true);
    }

    private void changePassword(Component owner) {
        JPanel p = new JPanel(new GridLayout(3, 2, 5, 5));
        JPasswordField cur = new JPasswordField();
        JPasswordField np  = new JPasswordField();
        JPasswordField cf  = new JPasswordField();
        p.add(new JLabel("Current:")); p.add(cur);
        p.add(new JLabel("New:"));     p.add(np);
        p.add(new JLabel("Confirm:")); p.add(cf);

        if (JOptionPane.showConfirmDialog(owner, p, "Change Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String c = new String(cur.getPassword());
        String n = new String(np.getPassword());
        String v = new String(cf.getPassword());
        if (c.isBlank() || n.isBlank() || v.isBlank()) { JOptionPane.showMessageDialog(owner, "Fill all fields."); return; }
        if (!n.equals(v)) { JOptionPane.showMessageDialog(owner, "New and confirm don’t match."); return; }

        boolean ok = UserManager.changePassword(user.getUsername(), c, n);
        JOptionPane.showMessageDialog(owner, ok ? "Password updated." : "Wrong current password / DB error.");
    }


    private void onSave() {
        boolean ok = UserManager.updateUser(user);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to save profile.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // keep ChatWindow's UserData in sync
        ownerWin.getUser().setAvatarPath(user.getAvatarPath());

        // nuke cached scaled avatars for this user (keys like "username#48", "username#65", …)
        try {
            var f = ownerWin.getClass().getDeclaredField("avatarCache");
            f.setAccessible(true);
            Object m = f.get(ownerWin);
            if (m instanceof java.util.Map<?,?> map) {
                String prefix = user.getUsername() + "#";
                java.util.List<Object> drop = new java.util.ArrayList<>();
                for (Object k : map.keySet()) {
                    if (k instanceof String s && s.startsWith(prefix)) drop.add(k);
                }
                for (Object k : drop) ((java.util.Map) map).remove(k);
            }
        } catch (Exception ignore) {}
// drop the simple sidebar cache too (if present)
        try {
            var fUA = ownerWin.getClass().getDeclaredField("userAvatars");
            fUA.setAccessible(true);
            Object m2 = fUA.get(ownerWin);
            if (m2 instanceof java.util.Map<?,?> map2) {
                map2.remove(user.getUsername());
            }
        } catch (Exception ignore) {}

// repaint the left avatar ring if present
        try {
            var fPanel = ownerWin.getClass().getDeclaredField("avatarPanel");
            fPanel.setAccessible(true);
            Object p = fPanel.get(ownerWin);
            if (p instanceof java.awt.Component c) c.repaint();
        } catch (Exception ignore) {}

        // refresh avatars inside existing bubbles (Message.avatar) without new APIs
        try {
            // a) get all chat panels
            var fPanels = ownerWin.getClass().getDeclaredField("chatPanels");
            fPanels.setAccessible(true);
            Object panelsObj = fPanels.get(ownerWin);

            if (panelsObj instanceof java.util.Map<?,?> map) {
                // b) prepare a fresh image using existing getAvatarForUser(String,int)
                java.awt.Image fresh40 = null;
                try {
                    var mGet = ownerWin.getClass().getDeclaredMethod("getAvatarForUser", String.class, int.class);
                    mGet.setAccessible(true);
                    fresh40 = (java.awt.Image) mGet.invoke(ownerWin, user.getUsername(), 40);
                } catch (Exception ignore) {}

                for (Object panel : map.values()) {
                    if (panel == null) continue;

                    try {
                        // c) access private List<Message> and update avatar field for my messages
                        var fMsgs = panel.getClass().getDeclaredField("messages");
                        fMsgs.setAccessible(true);
                        Object msgsObj = fMsgs.get(panel);
                        if (msgsObj instanceof java.util.List<?> msgs && fresh40 != null) {
                            for (Object msg : msgs) {
                                try {
                                    var fSender = msg.getClass().getDeclaredField("sender");
                                    fSender.setAccessible(true);
                                    Object sender = fSender.get(msg);
                                    if (sender instanceof String s && s.equalsIgnoreCase(user.getUsername())) {
                                        var fAv = msg.getClass().getDeclaredField("avatar");
                                        fAv.setAccessible(true);
                                        fAv.set(msg, fresh40);
                                    }
                                } catch (Exception ignore) {}
                            }
                        }
                    } catch (Exception ignore) {}

                    if (panel instanceof java.awt.Component c) {
                        c.revalidate();
                        c.repaint();
                    }
                }
            }
        } catch (Exception ignore) {}

        // repaint the left avatar ring if present
        try {
            var fPanel = ownerWin.getClass().getDeclaredField("avatarPanel");
            fPanel.setAccessible(true);
            Object p = fPanel.get(ownerWin);
            if (p instanceof java.awt.Component c) c.repaint();
        } catch (Exception ignore) {}

        ownerWin.repaint();
        dispose();
    }

    private String formatDate(java.sql.Timestamp ts) {
        if (ts == null) return "unknown";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(ts);
    }

    // ---------- Internal helpers ----------

    private static ChatClient client(ChatWindow owner) {
        // 1) prefer a public getter if present
        try {
            Method m = owner.getClass().getMethod("getClient");
            Object c = m.invoke(owner);
            if (c instanceof ChatClient) return (ChatClient) c;
        } catch (Exception ignore) {}

        // 2) fallback: access private field "client"
        try {
            Field f = owner.getClass().getDeclaredField("client");
            f.setAccessible(true);
            Object c = f.get(owner);
            if (c instanceof ChatClient) return (ChatClient) c;
        } catch (Exception ignore) {}

        return null;
    }

    private static boolean ensureConnected(ChatWindow owner, ChatClient c) {
        if (c == null) {
            LoginManager.showSystemNotification(owner, "Not connected to server.");
            return false;
        }
        return true;
    }

    /** Invoke owner.methodName(...) if it exists; otherwise no-op. */
    private static void invokeIfExists(ChatWindow owner, String methodName, Class<?>[] sig, Object[] args) {
        try {
            Method m = owner.getClass().getDeclaredMethod(methodName, sig);
            m.setAccessible(true);
            m.invoke(owner, args);
        } catch (Exception ignore) {}
    }
}
