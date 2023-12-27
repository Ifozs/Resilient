package tech.resilientgym.resilient;

public class User {

    private int user_id;
    private String name;
    private String level;
    private String email;
    private int xp;

    public User(int user_id, String name, String level, String email, int xp) {
        this.user_id = user_id;
        this.name = name;
        this.level = level;
        this.email = email;
        this.xp = xp;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }
}

