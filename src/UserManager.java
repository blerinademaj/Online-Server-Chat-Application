import javax.swing.*;
import java.sql.*;

public class UserManager {

    // Centralize DB config. Use SAME host as MessageManager.
    public static final String DB_HOST =
            System.getProperty("osch.db.host", "127.0.0.1"); // server’s LAN IP (NOT 127.0.0.1)
    public static final String DB_NAME = "online_server_chat";

    public static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":3306/" + DB_NAME
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC"
            + "&connectTimeout=4000"
            + "&socketTimeout=8000"
            + "&tcpKeepAlive=true";

    public static final String DB_USER = System.getProperty("osch.db.user", "root");
    public static final String DB_PASSWORD = System.getProperty("osch.db.pass", "");

    /* ----------------------------- Helpers ----------------------------- */

    private static void ensureDriver() {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new RuntimeException("MySQL JDBC driver not found", e); }
    }

    private static String safe(String v) { return (v == null || v.isBlank()) ? null : v; }
    private static String trimOrEmpty(String s) { return s == null ? "" : s.trim(); }

    /* ----------------------------- Register/Login/Status ----------------------------- */

    // REGISTER: default status online
    public static boolean register(String username, String password, String firstName, String lastName, String secretCode) {
        System.out.println("[REGISTER] Trying: " + username);
        ensureDriver();
        try (Connection conn = DriverManager.getConnection(UserManager.DB_URL, UserManager.DB_USER, UserManager.DB_PASSWORD)) {

            // unique username
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT 1 FROM OSCH_USERS WHERE username=?")) {
                check.setString(1, username);
                if (check.executeQuery().next()) {
                    System.out.println("[REGISTER] Username exists.");
                    return false;
                }
            }

            // insert (status online)
            String sql = "INSERT INTO OSCH_USERS " +
                    "(username, password, first_name, last_name, avatar_path, role, status, join_time, ip_address, secret_code) " +
                    "VALUES (?, ?, ?, ?, 'avatars/default.png', 'user', 'online', CURRENT_TIMESTAMP, NULL, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password); // NOTE: hash later
                ps.setString(3, firstName);
                ps.setString(4, lastName);
                ps.setString(5, secretCode);
                int rows = ps.executeUpdate();
                System.out.println("[REGISTER] " + (rows == 1 ? "OK" : "FAIL"));
                return rows == 1;
            }
        } catch (Exception e) {
            System.out.println("[REGISTER] Error:");
            e.printStackTrace();
            return false;
        }
    }

    // LOGIN: set status online në DB nëse sukses
    public static boolean login(String username, String password) {
        System.out.println("[LOGIN] Trying: " + username);
        final String u = (username == null ? "" : username.trim());
        if (u.isEmpty() || password == null) return false;

        ensureDriver();

        final String sql = "SELECT 1 FROM OSCH_USERS WHERE username=? AND password=? LIMIT 1";
        boolean ok = false;

        try (Connection conn = DriverManager.getConnection(UserManager.DB_URL, UserManager.DB_USER, UserManager.DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                ok = rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("[LOGIN] " + (ok ? "SUCCESS" : "FAIL"));
        if (ok) setStatus(u, "online");
        return ok;
    }

    // PUBLIC: ndrysho statusin (UI → DB)
    public static boolean setStatus(String username, String status) {
        if (username == null || username.isBlank()) return false;

        // normalize + clamp
        String s = (status == null ? "offline" : status.trim().toLowerCase());
        if (!s.equals("online") && !s.equals("away") && !s.equals("busy") && !s.equals("offline")) {
            s = "offline";
        }

        final String sql = "UPDATE OSCH_USERS SET status=? " +
                "WHERE username=? AND (status IS NULL OR status<>?) " +
                "LIMIT 1";
        ensureDriver();
        try (Connection conn = DriverManager.getConnection(UserManager.DB_URL, UserManager.DB_USER, UserManager.DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s);
            ps.setString(2, username.trim());
            ps.setString(3, s);
            ps.executeUpdate(); // idempotent if same value
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ----------------------------- Reads & Updates ----------------------------- */

    public static UserData getUserByUsername(String username) {
        System.out.println("[GET USER] " + username);
        ensureDriver();
        final String sel =
                "SELECT username, first_name, last_name, avatar_path, role, status, join_time, ip_address " +
                        "FROM OSCH_USERS WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName  = rs.getString("last_name");
                    String avatar    = rs.getString("avatar_path");
                    String role      = rs.getString("role");
                    String status    = rs.getString("status");
                    Timestamp joined = rs.getTimestamp("join_time");
                    return new UserData(username, avatar, role, status, joined, null, firstName, lastName);
                }
            }
        } catch (Exception e) {
            System.out.println("[GET USER] Error:");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean userExists(String username) {
        ensureDriver();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM OSCH_USERS WHERE username=?")) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePassword(String username, String newPassword) {
        ensureDriver();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement("UPDATE OSCH_USERS SET password=? WHERE username=?")) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUser(UserData user) {
        String sql = "UPDATE OSCH_USERS SET avatar_path=?, role=?, status=? WHERE username=?";
        ensureDriver();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safe(user.getAvatarPath()));
            ps.setString(2, safe(user.getRole()));
            ps.setString(3, safe(user.getStatus()));
            ps.setString(4, user.getUsername());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUserIP(String username, String ipAddress) {
        String sql = "UPDATE OSCH_USERS SET ip_address=? WHERE username=?";
        ensureDriver();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ipAddress);
            ps.setString(2, username);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ----------------------------- Sessions ----------------------------- */

    public static boolean enterSession(String username, String ip) {
        String sqlInsert = "INSERT INTO OSCH_SESSIONS (user_id, ip, started_at, last_seen_at) " +
                "SELECT id, ?, NOW(), NOW() FROM OSCH_USERS WHERE username=?";

        String sqlUpdate = "UPDATE OSCH_USERS SET status='online' WHERE username=?";
        ensureDriver();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlInsert);
                 PreparedStatement ps2 = conn.prepareStatement(sqlUpdate)) {

                ps1.setString(1, ip);
                ps1.setString(2, username);
                ps1.executeUpdate();

                ps2.setString(1, username);
                ps2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean leaveSession(String username) {
        // End any open session and set offline in one hit
        String sqlEnd = "UPDATE OSCH_SESSIONS s " +
                "JOIN OSCH_USERS u ON u.id=s.user_id " +
                "SET s.ended_at=NOW(), u.status='offline' " +
                "WHERE u.username=? AND s.ended_at IS NULL";
        ensureDriver();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sqlEnd)) {
            ps.setString(1, username);
            int affected = ps.executeUpdate();

            // If no active sessions row matched, still force offline to be safe
            if (affected == 0) {
                try (PreparedStatement ps2 =
                             conn.prepareStatement("UPDATE OSCH_USERS SET status='offline' WHERE username=?")) {
                    ps2.setString(1, username);
                    ps2.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ----------------------------- Username/Password changes ----------------------------- */

    public static boolean changeUsername(String currentUsername, String newUsername) {
        if (currentUsername == null || newUsername == null) return false;
        final String upd = "UPDATE OSCH_USERS SET username=? WHERE username=?";
        ensureDriver();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = c.prepareStatement(upd)) {
            ps.setString(1, newUsername.trim());
            ps.setString(2, currentUsername.trim());
            int rows = ps.executeUpdate();
            return rows == 1; // unique constraint will throw if duplicate
        } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
            // newUsername already taken
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean changePassword(String username, String currentPwd, String newPwd) {
        if (username == null || currentPwd == null || newPwd == null) return false;
        final String check = "SELECT 1 FROM OSCH_USERS WHERE username=? AND password=? LIMIT 1";
        final String upd   = "UPDATE OSCH_USERS SET password=? WHERE username=?";
        ensureDriver();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            c.setAutoCommit(true);
            try (PreparedStatement ps = c.prepareStatement(check)) {
                ps.setString(1, username.trim());
                ps.setString(2, currentPwd);
                try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) return false; }
            }
            try (PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, newPwd);
                ps.setString(2, username.trim());
                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
