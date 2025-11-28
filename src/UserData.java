import java.sql.Timestamp;

public class UserData {
    private String username;
    private String firstName;
    private String lastName;
    private String avatarPath;
    private String role;
    private String status;
    private Timestamp joinTime;
    private String roomCode;

    public UserData(String username,
                    String avatarPath,
                    String role,
                    String status,
                    Timestamp joinTime,
                    String roomCode,
                    String firstName,
                    String lastName) {
        this.username = username;
        this.avatarPath = avatarPath;
        this.role = role;
        this.status = status;
        this.joinTime = joinTime;
        this.roomCode = roomCode;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // --- getters
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getAvatarPath(){ return avatarPath; }
    public String getRole()      { return role; }
    public String getStatus()    { return status; }
    public Timestamp getJoinTime(){ return joinTime; }
    public String getRoomCode()  { return roomCode; }

    // --- setters
    public void setUsername(String uname) { this.username = uname; }
    public void setFirstName(String v)    { this.firstName = v; }
    public void setLastName(String v)     { this.lastName = v; }
    public void setAvatarPath(String v)   { this.avatarPath = v; }
    public void setRole(String v)         { this.role = v; }
    public void setStatus(String v)       { this.status = v; }
    public void setJoinTime(Timestamp v)  { this.joinTime = v; }
    public void setRoomCode(String v)     { this.roomCode = v; }
}
