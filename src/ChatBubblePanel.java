import javax.swing.*;
        import java.awt.*;
        import java.awt.geom.Ellipse2D;
        import java.awt.image.BufferedImage;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

public class ChatBubblePanel extends JPanel {

    // ---- tune these if needed ----
    private static final int MIN_BUBBLE_W = 250;
    private static final int SIDE_PADDING = 240;
    private static final int V_SPACING = 20;
    private static final int RADIUS = 20;
    private static final int AVATAR_SIZE = 40;
    private static final int AVATAR_MARGIN = 20;
    private static final int BUBBLE_GAP = 15;

    // --------------------------------

    private static class Message {
        String sender, text, time;
        boolean isSelf;
        Image avatar;

        Message(String sender, String text, boolean isSelf, Image avatar) {
            this.sender = sender;
            this.text = text;
            this.isSelf = isSelf;
            this.time = java.time.LocalTime.now().withSecond(0).withNano(0).toString();
            this.avatar = avatar;
        }
    }

    private final List<Message> messages = new ArrayList<>();
    private Color bgColor = ThemeManager.background();
    private Color fgColor = ThemeManager.foreground();

    // --- NEW: status cache (no DB in paint) ---
    private final Map<String, String> statusByUser = new HashMap<>();
    private String selfUsername = null;
    private String selfStatus = "online";

    public ChatBubblePanel() {
        setOpaque(true);
        setBackground(bgColor);
        new javax.swing.Timer(33, e -> repaint()).start();
    }

    // wire from ChatWindow
    public void setSelf(String username, String status) {
        this.selfUsername = username;
        this.selfStatus = (status == null ? "offline" : status);
        statusByUser.put(username, this.selfStatus);
        repaint();
    }

    // allow ChatWindow to push presence updates (optional, future-proof)
    public void setUserStatus(String username, String status) {
        if (username == null) return;
        if (status == null) status = "offline";
        statusByUser.put(username, status);
        // repaint only if that user is on screen (cheap enough to repaint anyway)
        repaint();
    }

    public void addMessage(String sender, String text, boolean isSelf, Image avatar) {
        messages.add(new Message(sender, text, isSelf, avatar));
        // ensure sender at least exists in the cache
        statusByUser.putIfAbsent(sender, isSelf ? selfStatus : statusByUser.getOrDefault(sender, "offline"));
        revalidate();
        repaint();
    }

    public void setTheme(Color bg, Color fg) {
        this.bgColor = bg;
        this.fgColor = fg;
        setBackground(bgColor);
        repaint();
    }

    private int getViewportWidth() {
        Component p = getParent();
        if (p instanceof JViewport vp) {
            Container sp = vp.getParent();
            if (sp instanceof JScrollPane jsp) {
                int w = jsp.getWidth();
                Insets ins = jsp.getInsets();
                int inset = (ins == null ? 0 : ins.left + ins.right);
                return Math.max(0, w - inset - 12); // small safety margin from the scrollbar
            }
            return vp.getWidth(); // fallback
        }
        int w = getWidth();
        return (w > 0) ? w : 800;
    }

    @Override
    public Dimension getPreferredSize() {
        int panelW = getViewportWidth();
        int target = (int) Math.round(panelW * 0.62);
        int bubbleW = Math.max(MIN_BUBBLE_W, Math.min(target, panelW - SIDE_PADDING));

        int y = 20;

        Font textFont   = ThemeManager.firstAvailableFont(14);
             //   Font.PLAIN, 14, "Segoe UI Emoji","Apple Color Emoji","Noto Color Emoji","Cascadia Mono", "Consolas", "Liberation Mono", "Courier New", "monospace","Dialog","Arial Unicode MS", "JetBrains Mono");
        Font timeFont = new Font("Courier Prime", Font.PLAIN, 10);
        FontMetrics fmText = getFontMetrics(textFont);
        FontMetrics fmTime = getFontMetrics(timeFont);
        int lineHeight = fmText.getHeight();

        for (Message msg : messages) {
            int timeWidth = fmTime.stringWidth(msg.time);
            int gap = 16;
            int textAreaWidth = Math.max(120, bubbleW - 20 - timeWidth - gap);
            List<String> lines = wrapLines(msg.text, fmText, textAreaWidth);
            int textHeight = Math.max(lineHeight, lines.size() * lineHeight);
            int extraAbove = 15, extraBelow = 5;
            int bubbleH = textHeight + 30 + extraAbove + extraBelow;
            y += bubbleH + V_SPACING;
        }
        return new Dimension(panelW, Math.max(y, 300));
    }

    private static List<String> wrapLines(String text, FontMetrics fm, int maxW) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String w : text.split("\\s+")) {
            String test = (line.length() == 0) ? w : line + " " + w;
            if (fm.stringWidth(test) > maxW) {
                if (line.length() > 0) lines.add(line.toString());
                line.setLength(0);
                line.append(w);
            } else {
                line.setLength(0);
                line.append(test);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    private static Color statusColor(String s) {
        if (s == null) return new Color(200,200,200);
        switch (s.toLowerCase()) {
            case "online":  return new Color(0,172,88);
            case "busy":    return new Color(220,53,69);
            case "away":    return new Color(255,140,0);
            case "offline": return new Color(200,200,200);
            default:        return new Color(200,200,200);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelW = getViewportWidth();
        int target  = (int) Math.round(panelW * 0.62);
        int bubbleW = Math.max(MIN_BUBBLE_W, Math.min(target, panelW - SIDE_PADDING));
        int y = 20;
/*
        Font senderFont = ThemeManager.firstAvailableFont(13);
        //Font.BOLD, 14, "Segoe UI Emoji","Apple Color Emoji","Noto Color Emoji","Cascadia Mono", "Consolas", "Liberation Mono", "Courier New", "monospace","Dialog","Arial Unicode MS", "JetBrains Mono");
        Font textFont   = ThemeManager.firstAvailableFont(13);
        //Font.PLAIN, 14, "Segoe UI Emoji","Apple Color Emoji","Noto Color Emoji","Cascadia Mono", "Consolas", "Liberation Mono", "Courier New", "monospace","Dialog","Arial Unicode MS", "JetBrains Mono");

        Font timeFont   = new Font("Cascadia Mono", Font.PLAIN, 11); //Courier Prime
*/
        // 1) Pick ONE base font once
        Font base = ThemeManager.firstAvailableFont(13); // Cascadia/Consolas/... (no emoji here)
        //   inputField.setFont(base);

        Font senderFont = base.deriveFont(Font.BOLD, 13f);
        Font textFont   = base.deriveFont(Font.PLAIN, 13f);
        Font timeFont   = base.deriveFont(Font.PLAIN, 11f); // same family, smaller

        for (Message msg : messages) {
            int avatarX = msg.isSelf ? panelW - (AVATAR_SIZE + AVATAR_MARGIN) : AVATAR_MARGIN;
            int x = msg.isSelf ? avatarX - BUBBLE_GAP - bubbleW : avatarX + AVATAR_SIZE + BUBBLE_GAP;

            // keep your light/gray bubbles
            Color bubbleColor = msg.isSelf ? ThemeManager.UI_BLUE : new Color(240,240,240);
            //new Color(173, 216, 230)

            // --- measure ---
            g2.setFont(timeFont);
            FontMetrics fmTime = g2.getFontMetrics();
            int timeWidth = fmTime.stringWidth(msg.time);
            int gap = 16;

            g2.setFont(textFont);
            FontMetrics fmText = g2.getFontMetrics();
            int textAreaWidth = Math.max(120, bubbleW - 20 - timeWidth - gap);
            int lineHeight = fmText.getHeight();
            java.util.List<String> lines = wrapLines(msg.text, fmText, textAreaWidth);
            int textHeight = Math.max(lineHeight, lines.size() * lineHeight);

            int extraAbove = 15, extraBelow = 5;
            int bubbleH = textHeight + 30 + extraAbove + extraBelow;

            // shadow
            g2.setColor(ThemeManager.shadowColor());
            g2.fillRoundRect(x + 3, y + 3, bubbleW, bubbleH, RADIUS, RADIUS);

            // bubble
            g2.setColor(bubbleColor);
            g2.fillRoundRect(x, y, bubbleW, bubbleH, RADIUS, RADIUS);

            // sender
            g2.setFont(senderFont);
            g2.setColor(Color.BLACK);
            g2.drawString("@" + msg.sender + ":", x + 10, y + 20);

            // text
            g2.setFont(textFont);
            g2.setColor(Color.BLACK);
            int tx = x + 20, ty = y + 40;
            for (String ln : lines) {
                g2.drawString(ln, tx, ty);
                ty += lineHeight;
            }

            // time
            g2.setFont(timeFont);
            g2.setColor(ThemeManager.isDark() ? new Color(37, 33, 33, 255) : new Color(124, 124, 124, 255));
            int timeX = x + bubbleW - 10 - timeWidth;
            int timeY = y + textHeight + 25 + extraAbove;
            g2.drawString(msg.time, timeX, timeY);

            // avatar + status dot
            if (msg.avatar != null) {
                final int DOT = 10, PAD = 2;
                int avatarY = y + (bubbleH - AVATAR_SIZE) / 2;

                Shape old = g2.getClip();
                g2.setClip(new Ellipse2D.Float(avatarX, avatarY, AVATAR_SIZE, AVATAR_SIZE));
                g2.drawImage(msg.avatar, avatarX, avatarY, this);
                g2.setClip(old);

                String st = msg.isSelf ? selfStatus : statusByUser.getOrDefault(msg.sender, null);
                int dx = avatarX + AVATAR_SIZE - DOT - PAD;
                int dy = avatarY + AVATAR_SIZE - DOT - PAD;
                g2.setColor(statusColor(st));
                g2.fillOval(dx, dy, DOT, DOT);
            }

            y += bubbleH + V_SPACING;
        }

        g2.dispose();
    }
}
