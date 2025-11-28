import java.sql.*;
import java.util.*;

public class MessageManager {

    // Centralize DB config. Reuse these from other classes if needed.
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


    // --- Driver helper (added) ---
    private static void ensureDriver() {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new RuntimeException("MySQL JDBC driver not found", e); }
    }

    // ========================= PUBLIC API =========================

    /** Save message and create receipts (GROUP/PRIVATE). */
    public static void saveMessage(String senderUsername,
                                   String receiverUsernameOrNull,
                                   String content,
                                   boolean isPrivate,
                                   String roomCodeOrNull) {
        if (senderUsername == null || senderUsername.isBlank() || content == null) return;

        String sender = senderUsername.trim();
        if (sender.startsWith("@")) sender = sender.substring(1);
        String receiver = (receiverUsernameOrNull == null) ? null : receiverUsernameOrNull.trim();
        if (receiver != null && receiver.startsWith("@")) receiver = receiver.substring(1);

        Connection conn = null;
        try {
            ensureDriver(); // <-- ensure driver
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // <-- fixed name
            conn.setAutoCommit(false); // TX start

            int senderId = getUserId(conn, sender);
            if (senderId == 0) throw new SQLException("Sender not found: " + sender);

            if (isPrivate) {
                if (receiver == null || receiver.isBlank())
                    throw new SQLException("Private message requires receiverUsername.");
                int receiverId = getUserId(conn, receiver);
                if (receiverId == 0) throw new SQLException("Receiver not found: " + receiver);

                int dmId = getOrCreateDmId(conn, senderId, receiverId);

                // 1) insert message
                int messageId;
                String insMsg = "INSERT INTO OSCH_MESSAGES (sender_id, dm_id, content, msg_type, created_at) " +
                        "VALUES (?, ?, ?, 'PRIVATE', NOW())";
                try (PreparedStatement ps = conn.prepareStatement(insMsg, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, senderId);
                    ps.setInt(2, dmId);
                    ps.setString(3, content);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        if (!k.next()) throw new SQLException("No message id");
                        messageId = k.getInt(1);
                    }
                }

                // 2) insert receipt for the recipient
                String insRec = "INSERT INTO OSCH_MESSAGE_RECEIPTS (message_id, recipient_id, status) VALUES (?, ?, 'sent')";
                try (PreparedStatement rec = conn.prepareStatement(insRec)) {
                    rec.setInt(1, messageId);
                    rec.setInt(2, receiverId);
                    rec.executeUpdate();
                }

            } else {
                if (roomCodeOrNull == null) throw new SQLException("Group message requires roomCode.");
                String roomCode = roomCodeOrNull.trim().replaceAll("\\s+", "");

                // ensure room (idempotent)
                try (PreparedStatement r = conn.prepareStatement(
                        "INSERT INTO OSCH_ROOMS (room_code, name, created_by, created_at) " +
                                "VALUES (?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE name=name")) {
                    r.setString(1, roomCode);
                    r.setString(2, "Room " + roomCode);
                    r.setInt(3, senderId);
                    r.executeUpdate();
                }

                // ensure membership (idempotent; PK(user_id, room_code))
                try (PreparedStatement m = conn.prepareStatement(
                        "INSERT INTO OSCH_ROOM_MEMBERS (user_id, room_code, is_admin, status, joined_at) " +
                                "SELECT u.id, ?, CASE WHEN u.role='admin' THEN 1 ELSE 0 END, 'active', NOW() " +
                                "FROM OSCH_USERS u WHERE u.id=? " +
                                "ON DUPLICATE KEY UPDATE " +
                                "  is_admin = GREATEST(is_admin, VALUES(is_admin)), " +
                                "  status   = 'active'")) {
                    m.setString(1, roomCode);
                    m.setInt(2, senderId);
                    m.executeUpdate();
                }

                // insert message
                int messageId;
                String insMsg = "INSERT INTO OSCH_MESSAGES (sender_id, room_code, content, msg_type, created_at) " +
                        "VALUES (?, ?, ?, 'GROUP', NOW())";
                try (PreparedStatement ps = conn.prepareStatement(insMsg, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, senderId);
                    ps.setString(2, roomCode);
                    ps.setString(3, content);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        if (!k.next()) throw new SQLException("No message id");
                        messageId = k.getInt(1);
                    }
                }

                // receipts for all members except sender
                String membersSql = "SELECT user_id FROM OSCH_ROOM_MEMBERS WHERE room_code = ?";
                try (PreparedStatement ms = conn.prepareStatement(membersSql)) {
                    ms.setString(1, roomCode);
                    try (ResultSet rs = ms.executeQuery()) {
                        String insRec = "INSERT INTO OSCH_MESSAGE_RECEIPTS (message_id, recipient_id, status) VALUES (?, ?, 'sent')";
                        try (PreparedStatement rec = conn.prepareStatement(insRec)) {
                            while (rs.next()) {
                                int uid = rs.getInt(1);
                                if (uid == senderId) continue;
                                rec.setInt(1, messageId);
                                rec.setInt(2, uid);
                                rec.addBatch();
                            }
                            rec.executeBatch();
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignore) {}
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
        }
    }

    /** Same as saveMessage but returns the new message id. */
    public static int saveMessageReturnId(String senderUsername,
                                          String receiverUsernameOrNull,
                                          String content,
                                          boolean isPrivate,
                                          String roomCodeOrNull) throws SQLException {
        if (senderUsername == null || senderUsername.isBlank() || content == null)
            throw new SQLException("bad args");

        String sender = senderUsername.trim();
        if (sender.startsWith("@")) sender = sender.substring(1);
        String receiver = (receiverUsernameOrNull == null) ? null : receiverUsernameOrNull.trim();
        if (receiver != null && receiver.startsWith("@")) receiver = receiver.substring(1);

        ensureDriver(); // <-- ensure driver
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // <-- fixed name
        try {
            conn.setAutoCommit(false);
            int senderId = getUserId(conn, sender);
            if (senderId == 0) throw new SQLException("Sender not found: " + sender);

            int messageId;

            if (isPrivate) {
                if (receiver == null || receiver.isBlank())
                    throw new SQLException("Private message requires receiverUsername.");
                int receiverId = getUserId(conn, receiver);
                if (receiverId == 0) throw new SQLException("Receiver not found: " + receiver);

                int dmId = getOrCreateDmId(conn, senderId, receiverId);

                String sql = "INSERT INTO OSCH_MESSAGES (sender_id, dm_id, content, msg_type, created_at) " +
                        "VALUES (?, ?, ?, 'PRIVATE', NOW())";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, senderId);
                    ps.setInt(2, dmId);
                    ps.setString(3, content);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        if (!k.next()) throw new SQLException("No message id");
                        messageId = k.getInt(1);
                    }
                }

                try (PreparedStatement rec = conn.prepareStatement(
                        "INSERT INTO OSCH_MESSAGE_RECEIPTS (message_id, recipient_id, status) VALUES (?, ?, 'sent')")) {
                    rec.setInt(1, messageId);
                    rec.setInt(2, receiverId);
                    rec.executeUpdate();
                }

            } else {
                if (roomCodeOrNull == null) throw new SQLException("Group message requires roomCode.");
                String roomCode = roomCodeOrNull.trim().replaceAll("\\s+", "");

                try (PreparedStatement r = conn.prepareStatement(
                        "INSERT INTO OSCH_ROOMS (room_code, name, created_by, created_at) " +
                                "VALUES (?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE name=name")) {
                    r.setString(1, roomCode);
                    r.setString(2, "Room " + roomCode);
                    r.setInt(3, senderId);
                    r.executeUpdate();
                }
                try (PreparedStatement m = conn.prepareStatement(
                        "INSERT INTO OSCH_ROOM_MEMBERS (user_id, room_code, is_admin, status, joined_at) " +
                                "VALUES (?, ?, 0, 'active', NOW()) ON DUPLICATE KEY UPDATE status='active'")) {
                    m.setInt(1, senderId);
                    m.setString(2, roomCode);
                    m.executeUpdate();
                }

                String sql = "INSERT INTO OSCH_MESSAGES (sender_id, room_code, content, msg_type, created_at) " +
                        "VALUES (?, ?, ?, 'GROUP', NOW())";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, senderId);
                    ps.setString(2, roomCode);
                    ps.setString(3, content);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        if (!k.next()) throw new SQLException("No message id");
                        messageId = k.getInt(1);
                    }
                }

                String membersSql = "SELECT user_id FROM OSCH_ROOM_MEMBERS WHERE room_code = ?";
                try (PreparedStatement ms = conn.prepareStatement(membersSql)) {
                    ms.setString(1, roomCode);
                    try (ResultSet rs = ms.executeQuery()) {
                        String insRec = "INSERT INTO OSCH_MESSAGE_RECEIPTS (message_id, recipient_id, status) VALUES (?, ?, 'sent')";
                        try (PreparedStatement rec = conn.prepareStatement(insRec)) {
                            while (rs.next()) {
                                int uid = rs.getInt(1);
                                if (uid == senderId) continue;
                                rec.setInt(1, messageId);
                                rec.setInt(2, uid);
                                rec.addBatch();
                            }
                            rec.executeBatch();
                        }
                    }
                }
            }

            conn.commit();
            return messageId;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            throw e;
        } finally {
            try { conn.close(); } catch (SQLException ignore) {}
        }
    }

    /** Room history (oldest first). */
    public static List<MessageData> getMessagesByRoomCode(String roomCode) {
        List<MessageData> out = new ArrayList<>();
        String sql = """
            SELECT m.id, u.username AS sender, m.content, m.msg_type, m.created_at, m.room_code
            FROM OSCH_MESSAGES m
            JOIN OSCH_USERS u ON u.id = m.sender_id
            WHERE m.room_code = ?
            ORDER BY m.created_at ASC
        """;
        ensureDriver(); // <-- ensure driver
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new MessageData(
                            rs.getString("sender"),
                            null,
                            rs.getString("content"),
                            rs.getTimestamp("created_at"),
                            rs.getString("msg_type"),
                            rs.getString("room_code")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /** Private history A↔B (oldest first). */
    public static List<MessageData> getPrivateMessages(String userA, String userB) {
        List<MessageData> out = new ArrayList<>();
        ensureDriver(); // <-- ensure driver
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            int aId = getUserId(conn, userA), bId = getUserId(conn, userB);
            if (aId == 0 || bId == 0) return out;

            Integer dmId = getDmId(conn, aId, bId);
            if (dmId == null) return out;

            String sql = """
                SELECT m.id, su.username AS sender, m.content, m.msg_type, m.created_at
                FROM OSCH_MESSAGES m
                JOIN OSCH_USERS su ON su.id = m.sender_id
                WHERE m.dm_id = ?
                ORDER BY m.created_at ASC
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, dmId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sender = rs.getString("sender");
                        String receiver = sender.equals(userA) ? userB : userA;
                        out.add(new MessageData(
                                sender,
                                receiver,
                                rs.getString("content"),
                                rs.getTimestamp("created_at"),
                                rs.getString("msg_type"),
                                null
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    // ========================= HELPERS =========================

    /** MISSING earlier: needed by all methods that already call it. */
    private static int getUserId(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM OSCH_USERS WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Exposed for server code that only has username. */
    public static int getUserId(String username) throws SQLException {
        ensureDriver(); // <-- ensure driver
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return getUserId(c, username);
        }
    }

    // Return existing dm id for (a,b) pair (order-independent), or null
    private static Integer getDmId(Connection conn, int userAId, int userBId) throws SQLException {
        int x = Math.min(userAId, userBId);
        int y = Math.max(userAId, userBId);
        String sql = "SELECT id FROM OSCH_PRIVATE_DMS WHERE user_a_id = ? AND user_b_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, x);
            ps.setInt(2, y);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    // Get or create dm id for (a,b) pair (order-independent)
    private static int getOrCreateDmId(Connection conn, int userAId, int userBId) throws SQLException {
        Integer existing = getDmId(conn, userAId, userBId);
        if (existing != null) return existing;

        int x = Math.min(userAId, userBId);
        int y = Math.max(userAId, userBId);
        String insert = "INSERT INTO OSCH_PRIVATE_DMS (user_a_id, user_b_id, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, x);
            ps.setInt(2, y);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        // race insert: fetch again
        Integer after = getDmId(conn, userAId, userBId);
        if (after != null) return after;
        throw new SQLException("Failed to create or fetch DM id.");
    }

    public static void markDelivered(int messageId, int recipientId) {
        String sql = "UPDATE OSCH_MESSAGE_RECEIPTS SET status='delivered', delivered_at=NOW() " +
                "WHERE message_id=? AND recipient_id=?";
        ensureDriver(); // <-- ensure driver
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setInt(2, recipientId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void markSeen(int messageId, int recipientId) {
        String sql = "UPDATE OSCH_MESSAGE_RECEIPTS SET status='seen', seen_at=NOW() " +
                "WHERE message_id=? AND recipient_id=?";
        ensureDriver(); // <-- ensure driver
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setInt(2, recipientId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
