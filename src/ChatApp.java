import javax.swing.*;

public class ChatApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatApp app = new ChatApp();
            new LoginFrame(app);
        });
    }

    public void continueAfterLogin(UserData user, boolean isAdmin, String serverIP) {
        try {
            String roomName = "Not defined";
            user.setRole(isAdmin ? "admin" : "user");

            String host = (serverIP == null || serverIP.isBlank()
                    || "localhost".equalsIgnoreCase(serverIP)
                    || "0.0.0.0".equals(serverIP)) ? "127.0.0.1" : serverIP.trim();

            ChatClient client = new ChatClient(host, 8000, user.getUsername(), user, roomName);
            ChatWindow chat = new ChatWindow(client, user);
            client.attachUI(chat);

            chat.setGroupName(roomName);
            chat.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to start chat at " + serverIP + ":8000\n" +
                            "• Start ChatServer first\n" +
                            "• Use 127.0.0.1 if server is on this PC\n" +
                            "• Otherwise use the server's LAN IP",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // keep the old signature for existing callers (defaults to localhost)
    public void continueAfterLogin(UserData user, boolean isAdmin) {
        continueAfterLogin(user, isAdmin, "127.0.0.1");
    }
}
