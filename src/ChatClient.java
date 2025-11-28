import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// --- ChatClient.java ---
public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private UserData user;

    private ChatWindow chatWindow;
    private String roomName;
    private String lastRoomCode = "";

    private final Set<String> activePMs = new HashSet<>();
    private final Map<String, Long> nextRequestAt = new HashMap<>();
    private static final long REQUEST_COOLDOWN_MS = 1500;
    private final Map<String, Boolean> pendingJoins = new ConcurrentHashMap<>();

    public ChatClient(String serverIP, int port, String username, UserData user, String roomName) throws IOException {
        String host = resolveHost(serverIP);
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 4000); // 4s connect timeout
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);

            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            this.username = username;
            this.user = user;
            this.roomName = roomName;

            // Exact server handshake: 3 lines
            out.println(username);                                                   // username
            out.println(user != null && user.getRole() != null ? user.getRole() : "user"); // role
            out.println(roomName != null ? roomName : "Not defined");                // initial room label

            start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to connect to server at " + host + ":" + port +
                            "\n• Start ChatServer first\n• Use 127.0.0.1 if server is on this PC\n• Otherwise use the server's LAN IP",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            throw e;
        }
    }

    private static String resolveHost(String ip) {
        if (ip == null || ip.isBlank() || "localhost".equalsIgnoreCase(ip) || "0.0.0.0".equals(ip)) {
            return "127.0.0.1";
        }
        return ip.trim();
    }

    // store a back-reference if you want to notify UI
    public void setChatWindow(ChatWindow w) { this.chatWindow = w; }  // optional if you use attachUI

    public String getRoomName() { return roomName; }
    public void setRoomName(String name) { this.roomName = name; }

    // group message: wrap protocol
    public void sendGroupMessage(String text) {
        if (text == null || text.isBlank()) return;
        out.println("GROUP|" + text);
    }

    // private message: wrap protocol (payload routing ende s'është implementu në server)
    public void sendPrivateMessage(String otherUser, String text) {
        if (otherUser == null || otherUser.isBlank() || text == null || text.isBlank()) return;
        out.println("PRIVATE|" + otherUser + ":" + text);
    }

    // join group request (sidebar / join by code)
    public boolean requestJoinGroup(String label) {
        if (label == null || label.isBlank()) return false;
        out.println("JOIN_REQUEST|" + label.trim());
        out.flush();
        return true;
    }

    public void attachUI(ChatWindow window) { this.chatWindow = window; }

    public void start() {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {

                    // 1) Room code handshake / updates
                    if (line.startsWith("ROOM_CODE|")) {
                        String code = line.substring("ROOM_CODE|".length()).trim();
                        lastRoomCode = code;
                        if (user != null) user.setRoomCode(code);
                        if (chatWindow != null) {
                            final String c = code;
                            SwingUtilities.invokeLater(() -> chatWindow.updateRoomCode(c));
                        }
                        continue;
                    }

                    // 2) PM approved/declined
                    if (line.startsWith("PRIVATE_APPROVED|")) {
                        String other = line.split("\\|",2)[1];
                        onPrivateApproved(other);
                        if (chatWindow != null) {
                            final String msgCopy = "SYSTEM: Private chat opened with @" + other;
                            SwingUtilities.invokeLater(() -> chatWindow.handleIncoming(msgCopy));
                        }
                        continue;
                    }

                    if (line.startsWith("PRIVATE_DECLINED|")) {
                        String other = line.split("\\|",2)[1];
                        onPrivateClosed(other);
                        if (chatWindow != null) {
                            final String msgCopy = "SYSTEM: @" + other + " declined the private chat.";
                            SwingUtilities.invokeLater(() -> chatWindow.handleIncoming(msgCopy));
                        }
                        continue;
                    }

                    // 3) PM ended by other/server
                    if (line.startsWith("PRIVATE_ENDED|")) {
                        String other = line.split("\\|",2)[1];
                        onPrivateClosed(other);
                        if (chatWindow != null) {
                            final String o = other;
                            SwingUtilities.invokeLater(() -> chatWindow.onPrivateEnded(o));
                        }
                        continue;
                    }

                    // 4) JOIN_ACCEPTED -> kërko history + lejo ChatWindow me bo rename tab-it
                    if (line.startsWith("JOIN_ACCEPTED|")) {
                        String[] p = line.split("\\|", 3);
                        String code = p.length > 1 ? p[1] : lastRoomCode;

                        out.println("HISTORY_REQUEST|" + code + "|50");
                        out.flush();

                        if (chatWindow != null) {
                            final String joinLine  = line; // JOIN_ACCEPTED|code|Room ...
                            final String uiMsg     = "SYSTEM: Joined " + (p.length > 2 ? p[2] : "Room " + code);

                            SwingUtilities.invokeLater(() -> {
                                // e dërgojmë JOIN_ACCEPTED te ChatWindow që të punojë logjika ekzistuese
                                chatWindow.handleIncoming(joinLine);
                                // plus një mesazh sistemik i thjeshtë
                                chatWindow.handleIncoming(uiMsg);
                            });
                        }
                        continue;
                    }

                    // 5) History stream (placeholder – nuk po ndryshojmë logjikën ekzistuese)
                    if (line.startsWith("HISTORY|")) {
                        continue;
                    }
                    if (line.startsWith("HISTORY_ITEM|")) {
                        if (chatWindow != null) {
                            final String msgCopy = line;
                            SwingUtilities.invokeLater(() -> chatWindow.handleIncoming(msgCopy));
                        }
                        continue;
                    }

                    // 6) Generic inbound (SYSTEM, GROUP, PRIVATE payloads, INVITE, JOIN_REQUEST, JOIN_DECLINED, ...)
                    if (chatWindow != null) {
                        final String msgCopy = line;
                        SwingUtilities.invokeLater(() -> chatWindow.handleIncoming(msgCopy));
                    }
                }
            } catch (IOException ignored) {
            } finally {
                try { socket.close(); } catch (IOException ignore) {}
            }
        }, "client-read-" + username).start();
    }

    // --- client-side send helpers ---
    public boolean requestPrivate(String toUser) {
        long now = System.currentTimeMillis();
        Long next = nextRequestAt.get(toUser);
        if (next != null && now < next) return false;
        out.println("PRIVATE_REQUEST|" + toUser);
        out.flush();
        nextRequestAt.put(toUser, now + REQUEST_COOLDOWN_MS);
        return true;
    }

    /** Return true if you are allowed to START a private with 'otherUser'. */
    public boolean canSendPrivate(String otherUser) {
        if (otherUser == null || otherUser.isBlank()) return false;
        // Allowed when NOT already active:
        return !activePMs.contains(otherUser.toLowerCase());
    }

    /** Admin approves/declines a join — përdor "|" sipas protokollit. */
    public void sendJoinApproved(String username) {
        if (username == null || username.isBlank()) return;
        out.println("JOIN_APPROVED|" + username.trim());
        out.flush();
    }
    public void sendJoinDeclined(String username) {
        if (username == null || username.isBlank()) return;
        out.println("JOIN_DECLINED|" + username.trim());
        out.flush();
    }

    /** Join a room by code (legacy) – server e mapon në JOIN_REQUEST. */
    public void joinRoom(String code) {
        if (code == null || code.isBlank()) return;
        String c = code.trim().replaceAll("\\s+","");
        out.println("JOIN_GROUP|" + c);
        out.flush();
    }

    public void sendPrivateApproved(String sender) { out.println("PRIVATE_APPROVED|" + sender); }
    public void sendPrivateDeclined(String sender) { out.println("PRIVATE_DECLINED|" + sender); }

    public void sendMessage(String raw) {
        if (raw == null || raw.trim().isEmpty()) return;
        out.println(raw.trim());
    }

    // Leaving a private chat
    public void leavePrivateChat(String otherUser, String username) {
        sendMessage("SYSTEM: " + username + " left the private chat with " + otherUser);
        out.println("LEAVE_PRIVATE|" + otherUser);
        out.flush();
        onPrivateClosed(otherUser);
    }

    // Group helpers
    public void createGroup(String code, String name) {
        out.println("CREATE_GROUP|" + code + "|" + name);
        out.flush();
    }

    /** Send a PRIVATE chat request handshake to the server. */
    public boolean sendPrivateRequest(String targetUsername) {
        if (targetUsername == null || targetUsername.isBlank()) return false;
        String u = targetUsername.trim();
        try {
            out.println("PRIVATE_REQUEST|" + u);
            out.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // --- Group Invites ---
    /** Send a group INVITE to 'targetUsername' për një room code të caktuar (6 shifra). */
    public void sendGroupInvite(String targetUsername, String roomCode) {
        if (targetUsername == null || targetUsername.isBlank()) return;
        String u = targetUsername.trim();
        String code = (roomCode == null) ? "" : roomCode.trim().replaceAll("\\s+", "");
        if (!code.isEmpty()) {
            out.println("INVITE|" + code + "|" + u);
        } else {
            out.println("INVITE|" + u);
        }
        out.flush();
    }

    /** Optional: invite që e lejon serverin me infer current room nga session-i. */
    public void sendGroupInvite(String targetUsername) {
        if (targetUsername == null || targetUsername.isBlank()) return;
        out.println("INVITE|" + targetUsername.trim());
        out.flush();
    }

    /** Mark a private chat as approved (call this from your reader/handler when server approves). */
    public void onPrivateApproved(String otherUser) {
        if (otherUser == null) return;
        activePMs.add(otherUser.toLowerCase());
    }

    /** Mark a private chat as closed (call when server or client ends it). */
    public void onPrivateClosed(String otherUser) {
        if (otherUser == null) return;
        activePMs.remove(otherUser.toLowerCase());
    }

    // Disconnect / Leave app
    public void disconnect(boolean isGroup, String chatName, String username) {
        try {
            if (isGroup) {
                out.println("LEAVE");
                out.flush();
                sendMessage("SYSTEM: " + username + " left the group");
            } else {
                // handled via leavePrivateChat(...)
            }
        } catch (Exception ignored) {}
    }

    public String getLastRoomCode() { return lastRoomCode; }
}
