import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * BASIC HANDLER (current protocol):
 * Handshake (3 lines):
 *   1) <username>
 *   2) <role>                  (can be ignored)
 *   3) <roomName or label>     (clients in same name are grouped together)
 *
 * Server can optionally reply:
 *   ROOM_CODE|<displayCode>
 *
 * Group messages are line-based:
 *   GROUP|<text>
 *
 * Notes:
 * - AUTH probe is a short, separate flow: "AUTH|username|password" then close.
 * - Broadcasting is done via ChatServer.addToRoom/removeFromRoom/broadcast(room,...).
 */
public class ChatHandler implements Runnable {
    final Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // session fields (used across run/finally)
    String username = null;
    String roomName = null; // package-private që ChatServer me mujt me e ndryshu

    public ChatHandler(Socket socket) {
        this.socket = socket; // streams initialized in run()
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // -------- 1) AUTH GATE (short connection) --------
            String first = in.readLine();
            if (first == null) { close(); return; }

            if (first.startsWith("AUTH|")) {
                // AUTH|username|password
                String[] p  = first.split("\\|", 3);
                String u    = p.length > 1 ? p[1] : "";
                String pass = p.length > 2 ? p[2] : "";

                boolean ok;
                try { ok = UserManager.login(u, pass); } catch (Exception e) { ok = false; }

                out.println(ok ? "AUTH_OK" : "AUTH_FAIL");
                out.flush();
                close();                  // auth probe ends here
                return;
            }

            // -------- 2) NORMAL CHAT HANDSHAKE (3 lines) --------
            username = first;                      // line 1 (username)
            String roleIgnored = in.readLine();    // line 2 (role) – not used now
            roomName           = in.readLine();    // line 3 (room name/label)

            if (username == null || username.isBlank()) { close(); return; }

            // nëse s'ka room, ose vjen "Not defined" nga ChatApp -> gjenero kod 6-shifror
            if (roomName == null || roomName.isBlank()) {
                roomName = generateRoomCode();
            } else if ("Not defined".equalsIgnoreCase(roomName.trim())) {
                roomName = generateRoomCode();
            }

            // register këtë handler në room (roomName tani zakonisht është kodi 6-shifror)
            ChatServer.addToRoom(roomName, this);
            System.out.println("[Server] " + username + " joined room '" + roomName + "'");

            // ktheja klientit ROOM_CODE që ta shfaqë
            out.println("ROOM_CODE|" + roomName);

            // -------- 3) MAIN LOOP --------
            String line;
            while ((line = in.readLine()) != null) {

                // group message: "GROUP|text"
                if (line.startsWith("GROUP|")) {
                    String text = line.substring("GROUP|".length());
                    ChatServer.broadcast(roomName, username + ": " + text, this);
                    continue;
                }

                // join via code / label
                if (line.startsWith("JOIN_REQUEST|")) {
                    String code = line.split("\\|", 2)[1].trim().replaceAll("\\s+","");
                    ChatServer.requestJoin(username, code);
                    continue;
                }
                if (line.startsWith("JOIN_GROUP|")) {  // legacy alias për JOIN_REQUEST
                    String code = line.split("\\|", 2)[1].trim().replaceAll("\\s+","");
                    ChatServer.requestJoin(username, code);
                    continue;
                }

                // join approvals (toleron "|" ose ":")
                if (line.startsWith("JOIN_APPROVED")) {
                    String target = line.contains("|")
                            ? line.split("\\|",2)[1]
                            : line.split(":",2)[1];
                    ChatServer.approveJoin(username, target);
                    continue;
                }
                if (line.startsWith("JOIN_DECLINED")) {
                    String target = line.contains("|")
                            ? line.split("\\|",2)[1]
                            : line.split(":",2)[1];
                    ChatServer.declineJoin(username, target);
                    continue;
                }

                // group invites
                if (line.startsWith("INVITE|")) {
                    String[] parts = line.split("\\|", 3);
                    String code;
                    String targetUser;

                    if (parts.length == 2) {
                        // INVITE|target  -> infer room nga ky handler
                        targetUser = parts[1].trim();
                        code = roomName;
                    } else {
                        // INVITE|code|target
                        code = parts[1].trim().replaceAll("\\s+","");
                        targetUser = parts[2].trim();
                    }

                    ChatHandler target = ChatServer.findUserInServer(targetUser);
                    if (target != null) {
                        target.sendLine("INVITE|" + code + "|" + username);
                    } else {
                        sendLine("SYSTEM: @" + targetUser + " is not online.");
                    }
                    continue;
                }

                // private protocol (routing i plotë mund të shtohet më vonë)
                if (line.startsWith("PRIVATE_REQUEST|")
                        || line.startsWith("PRIVATE_APPROVED|")
                        || line.startsWith("PRIVATE_DECLINED|")
                        || line.startsWith("PRIVATE|")
                        || line.startsWith("LEAVE_PRIVATE|")) {
                    System.out.println("[Server] PM signal from @" + username + ": " + line);
                    continue;
                }

                // history fetch – për momentin injorohet (pa DB këtu)
                if (line.startsWith("HISTORY_REQUEST|")) {
                    // hook për DB nëse do më vonë
                    continue;
                }

                // leave
                if ("LEAVE".equalsIgnoreCase(line) || "EXIT".equalsIgnoreCase(line)) {
                    break;
                }

                // fallback: ignore unknown commands
            }

        } catch (IOException e) {
            System.err.println("[Server] IO error: " + e.getMessage());
        } finally {
            try {
                if (roomName != null && username != null) {
                    ChatServer.removeFromRoom(roomName, this);
                    System.out.println("[Server] " + username + " left room '" + roomName + "'");
                    ChatServer.broadcast(roomName, "SYSTEM: @" + username + " left the chat.", this);
                }
            } catch (Throwable ignore) {}
            close();
        }
    }

    // --- helpers ---
    public void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    void sendLine(String s) {
        if (out != null) {
            out.println(s);
            out.flush();
        }
    }

    private String generateRoomCode() {
        int code = 100000 + (int)(Math.random() * 900000);
        return String.valueOf(code);
    }

    private void close() {
        try { if (out != null) out.flush(); } catch (Exception ignore) {}
        try { if (socket != null) socket.close(); } catch (Exception ignore) {}
    }
}
