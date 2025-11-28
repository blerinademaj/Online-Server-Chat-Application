import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {
    private static final int PORT = 8000;

    // roomKey (zakonisht kodi 6-shifror) -> handlers në atë room
    static final Map<String, List<ChatHandler>> rooms = new HashMap<>();
    // username -> roomCode që e ka kërku me u join
    private static final Map<String, String> pendingJoin = new HashMap<>();

    // --- Room membership ---
    public static synchronized void addToRoom(String room, ChatHandler handler) {
        rooms.computeIfAbsent(room, k -> new ArrayList<>()).add(handler);
    }

    public static synchronized void removeFromRoom(String room, ChatHandler handler) {
        List<ChatHandler> list = rooms.get(room);
        if (list != null) {
            list.remove(handler);
            if (list.isEmpty()) {
                rooms.remove(room);
            }
        }
    }

    public static synchronized void broadcast(String room, String msg, ChatHandler sender) {
        List<ChatHandler> list = rooms.get(room);
        if (list != null) {
            for (ChatHandler ch : list) {
                if (ch != sender) {
                    ch.sendMessage(msg);
                }
            }
        }
    }

    // gjej user-in në cilindo room
    public static synchronized ChatHandler findUserInServer(String username) {
        for (List<ChatHandler> list : rooms.values()) {
            for (ChatHandler ch : list) {
                if (ch.username != null && ch.username.equals(username)) {
                    return ch;
                }
            }
        }
        return null;
    }

    // --- Join / invite flows ---

    /** @username kërkon me u bashku në room me kod 'roomCode'. */
    public static synchronized void requestJoin(String username, String roomCode) {
        ChatHandler requester = findUserInServer(username);
        if (requester == null) return;

        List<ChatHandler> list = rooms.get(roomCode);
        if (list == null || list.isEmpty()) {
            requester.sendLine("SYSTEM: Room " + roomCode + " not found.");
            return;
        }

        ChatHandler admin = list.get(0); // i pari në listë = admin
        pendingJoin.put(username, roomCode);
        // ChatWindow pret "JOIN_REQUEST:<username>"
        admin.sendLine("JOIN_REQUEST:" + username);
    }

    /** Admin @adminUsername aprovon pending join për @targetUsername. */
    public static synchronized void approveJoin(String adminUsername, String targetUsername) {
        String code = pendingJoin.get(targetUsername);
        if (code == null) return;

        ChatHandler target = findUserInServer(targetUsername);
        if (target == null) {
            pendingJoin.remove(targetUsername);
            return;
        }

        // lëviz user-in prej room-it të vjetër në të riun
        if (target.roomName != null) {
            removeFromRoom(target.roomName, target);
        }
        target.roomName = code;
        addToRoom(code, target);

        // njofto target-in që u pranu
        target.sendLine("JOIN_ACCEPTED|" + code + "|Room " + code);
        // dhe update room code label-in
        target.sendLine("ROOM_CODE|" + code);

        pendingJoin.remove(targetUsername);
    }

    /** Admin @adminUsername refuzon pending join për @targetUsername. */
    public static synchronized void declineJoin(String adminUsername, String targetUsername) {
        ChatHandler target = findUserInServer(targetUsername);
        if (target != null) {
            // ChatWindow pret "JOIN_DECLINED|<admin>"
            target.sendLine("JOIN_DECLINED|" + adminUsername);
        }
        pendingJoin.remove(targetUsername);
    }

    // --- Server bootstrap ---
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Server] Listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ChatHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
