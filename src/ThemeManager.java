import javax.swing.*;
import java.awt.*;

/** Simple theme utils — no custom UI painting. */
public class ThemeManager {
    private static boolean darkMode = false;

    // ---- STATE ----
    public static void setDarkMode(boolean dark) {
        darkMode = dark;
        UIManager.put("app.dark", dark); // persist for new windows
    }
    public static boolean isDark() {
        Object v = UIManager.get("app.dark");
        return darkMode || (v instanceof Boolean && (Boolean) v);
    }
    public static void toggle() { setDarkMode(!isDark()); }

    // Base surfaces
    public static Color background()   { return darkMode ? new Color(30,30,30)   : new Color(0xFAFAFA); }
    public static Color foreground()   { return darkMode ? Color.WHITE : Color.BLACK; }
    public static Color sidebarColor() { return darkMode ? new Color(38, 38, 38): new Color(0xEAEAEA); }
 //   public static Color shadowColor() { return darkMode ? new Color(129,129,129,60) : new Color(0,0,0,140); };
    // Lines / shadows
    public static Color borderColor() {
        return darkMode ?  new Color(0xEAEAEA) : new Color(70,70,70);      // darker gray in light
    }
    public static Color shadowColor()  {
        return darkMode ?   new Color(0x42555D) : new Color(0x42555D);
    }

    // extra for Login/inputs/cards
    public static Color card()         { return isDark() ? new Color(28,32,38)     : Color.WHITE; }
    public static Color subtext()      { return isDark() ? new Color(160,168,180)  : new Color(104,112,118); }
    public static Color accent()       { return isDark() ?  new Color(0x42555D) : new Color(0x42555D);}
    public static Color inputBg()      { return isDark() ? new Color(36,41,48)     : new Color(252,253,255); }
    public static Color inputBorder()  { return isDark() ? new Color(60,68,80)     : new Color(208,215,222); }

  /*  public static Font firstAvailableFont(int style, int size, String... names) {
        java.util.List<String> avail = java.util.Arrays.asList(
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        );
        for (String n : names) if (avail.contains(n)) return new Font(n, style, size);
        return new Font(Font.SANS_SERIF, style, size); // fallback
    }*/

    public static Font firstAvailableFont(int fontSize) {
        String[] fonts = {
                "Segoe UI Emoji","Noto Color Emoji",
                "Cascadia Mono",
                "Consolas",
                "Liberation Mono",
                "Courier New",
                "monospace"
        };

        for (String f : fonts) {
            try {
                return new Font(f, Font.PLAIN, fontSize);
            } catch (Exception ignore) {}
        }

        return new Font("monospace", Font.PLAIN, fontSize); // fallback
    }



    // ===== Tabs (selected blue, idle soft gray) =====
    public static final Color UI_BLUE       = new Color(0xBBD4E5);
    public static final Color UI_BLUE_HOVER = new Color(0x9ABED5);

    public static void applyTabbedPaneColors(JTabbedPane tabs) {
        final Color selBg  = UI_BLUE;
        final Color selFg  = Color.BLACK;
        final Color idleBg = new Color(0xF5F7FA);
        final Color idleFg = Color.BLACK;

        tabs.addChangeListener(e -> recolor(tabs, selBg, selFg, idleBg, idleFg));
        recolor(tabs, selBg, selFg, idleBg, idleFg);
        tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.borderColor()));
    }


    private static void recolor(JTabbedPane tabs, Color selBg, Color selFg, Color idleBg, Color idleFg) {
        int sel = tabs.getSelectedIndex();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            boolean s = (i == sel);
            tabs.setBackgroundAt(i, s ? selBg : idleBg);
            tabs.setForegroundAt(i, s ? selFg : idleFg);

            Component hdr = tabs.getTabComponentAt(i);
            if (hdr instanceof JPanel p) {
                p.setOpaque(false);
                for (Component ch : p.getComponents()) {
                    if (ch instanceof JLabel l)  { l.setOpaque(false); l.setForeground(s ? selFg : idleFg); }
                    if (ch instanceof JButton b) {
                        b.setOpaque(false);
                        b.setContentAreaFilled(false);
                        b.setBorderPainted(false);
                        b.setFocusPainted(false);
                        b.setForeground(s ? Color.BLACK : new Color(0x6B7280)); // normal gray
                    }
                }
            }
        }
        tabs.repaint();
    }

    // ===== ScrollPane =====
    public static void styleScrollPane(JScrollPane sp) {
        Color bg = background();
        sp.setOpaque(true);
        sp.setBackground(bg);
        if (sp.getViewport() != null) {
            sp.getViewport().setOpaque(true);
            sp.getViewport().setBackground(bg);
        }
        sp.setBorder(BorderFactory.createEmptyBorder()); // no blue rim

        sp.setCorner(JScrollPane.UPPER_LEFT_CORNER,  themedCorner(bg));
        sp.setCorner(JScrollPane.UPPER_RIGHT_CORNER, themedCorner(bg));
        sp.setCorner(JScrollPane.LOWER_LEFT_CORNER,  themedCorner(bg));
        sp.setCorner(JScrollPane.LOWER_RIGHT_CORNER, themedCorner(bg));
    }

    private static Component themedCorner(Color bg) {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(bg);
        return p;
    }
}
