import java.sql.Timestamp;

public class MessageData {
        private int id;                // <-- add this
        private String senderUsername;
        private String receiverUsername;
        private String content;
        private Timestamp timestamp;
        private String type;
        private String roomCode;

        public MessageData(String sender, String receiver, String content,
                           Timestamp timestamp, String msgType, String roomCode) {
            this.senderUsername = sender;
            this.receiverUsername = receiver;
            this.content = content;
            this.timestamp = timestamp;
            this.type = msgType;
            this.roomCode = roomCode;
        }

        // --- add these two ---
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getRoomCode() {
        return roomCode;
    }

}
