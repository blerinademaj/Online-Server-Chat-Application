import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnimationEngine {

    /** Solid status ring. NOTE: last param is DIAMETER (not radius). */

    // ---- Pulse API ----
    private static Color statusColor(String status) {
        switch (status == null ? "" : status.toLowerCase()) {
            case "online":
                return new Color(0x27AE60); // green
            case "busy":
                return new Color(0xE74C3C); // red
            case "away":
                return new Color(0xF39C12); // amber
            case "offline":
                return new Color(0xBDC3C7); // light gray
            case "deleted":
                return new Color(0xC2110864); // dark blue

            default:
                return new Color(0xBDC3C7); // fallback
        }
    }

    public static void drawPulsingStatus(Graphics2D g2d, int cx, int cy, int baseR, String status, long nowMs) {
        if (baseR <= 0) return;

        Object aa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color c = statusColor(status);

        // Easing for smooth expand/fade
        float t = (nowMs % 1200) / 1200f;                  // 0..1
        float ease = (float)(1 - Math.pow(1 - t, 4));      // ease-out

        // 1) Inner solid ring (thicker, full circle)
        Stroke old = g2d.getStroke();
        g2d.setColor(c);
        g2d.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int d = baseR * 2;
        g2d.drawOval(cx - baseR, cy - baseR, d, d);

        // 2) Outer pulsing ring (thick, full 360°)
        int pulseR = baseR + (int)(12 * ease);             // expand more
        float alpha = 0.55f * (1f - ease);                 // stronger, then fade
        Composite oc = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // main pulse ring
        g2d.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int pd = pulseR * 2;
        g2d.drawOval(cx - pulseR, cy - pulseR, pd, pd);

        // subtle outer glow for fullness
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.25f));
        g2d.setStroke(new BasicStroke(20f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawOval(cx - pulseR+4, cy - pulseR+4, pd, pd);

        // restore
        g2d.setComposite(oc);
        g2d.setStroke(old);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }

    // ---- Window shake micro-animation (used on invalid login) ----
    public static void shake(Window w) {
        if (w == null) return;
        final int originalX = w.getX(), originalY = w.getY();
        final int[] dx = {0, 12, -10, 8, -6, 4, -2, 0};

        Timer t = new Timer(14, null);
        t.addActionListener(new ActionListener() {
            int i = 0;
            @Override public void actionPerformed(ActionEvent e) {
                w.setLocation(originalX + dx[i], originalY);
                if (++i >= dx.length) {
                    t.stop();
                    w.setLocation(originalX, originalY);
                }
            }
        });
        t.start();
    }

}
