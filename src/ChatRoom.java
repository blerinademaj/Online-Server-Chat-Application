import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * BASIC ROOM:
 * - Admin (creator) + members
 * - Broadcast helper
 */
public class ChatRoom {
    private final String roomName;
    private final String roomCode;
    private final ChatHandler admin;

    private final List<ChatHandler> members = new CopyOnWriteArrayList<>();

    public ChatRoom(ChatHandler admin, String roomName, String roomCode) {
        this.admin = admin;
        this.roomName = (roomName == null || roomName.isBlank()) ? "Group" : roomName;
        this.roomCode = roomCode;
        this.members.add(admin);
    }

    public String getRoomName() { return roomName; }
    public String getRoomCode() { return roomCode; }
    public ChatHandler getAdmin() { return admin; }

    public void addMember(ChatHandler h) { if (!members.contains(h)) members.add(h); }
    public void removeMember(ChatHandler h) { members.remove(h); }

    public List<ChatHandler> getMembers() { return members; }

    public void broadcast(String line) {
        for (ChatHandler h : members) {
            try { h.sendLine(line); } catch (Exception ignore) {}
        }
    }
}
