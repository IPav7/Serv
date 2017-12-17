import java.io.InputStream;

/**
 * Created by Igor Pavinich on 28.11.2017.
 */
public class User {
    private int id;
    private String login;
    private String password;
    private String name;
    private String surname;
    private InputStream picture;
    private boolean online;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public User() {
    }

    public User(int id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public User(String login, String password, String name, String surname, InputStream picture) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.picture = picture;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setPicture(InputStream picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public InputStream getPicture() {
        return picture;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
