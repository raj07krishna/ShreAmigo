package scenica.raj.shreamigo;

/**
 * Created by DELL on 1/10/2017.
 */

public class UserData {

    private String Name;
    private String email;
    private String UID;
    private String TokenID;

    public UserData() {
    }

    public UserData(String name, String email, String UID, String tokenID) {
        Name = name;
        this.email = email;

        this.UID = UID;
        TokenID = tokenID;
    }

    public String getName() {
        return Name;
    }

    public UserData setName(String name) {
        Name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserData setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUID() {
        return UID;
    }

    public UserData setUID(String UID) {
        this.UID = UID;
        return this;
    }

    public String getTokenID() {
        return TokenID;
    }

    public UserData setTokenID(String tokenID) {
        TokenID = tokenID;
        return this;
    }
}
