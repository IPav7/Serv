import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by Igor Pavinich on 29.11.2017.
 */
public class UserActions {
    Connection connection;

    public UserActions(Connection connection) {
        this.connection = connection;
    }

    public boolean registerUser(User user){
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users where login = '" + user.getLogin() +"'");
            if(rs.next() == true) {
                return false;
            }
            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (name,surname,login,password,picture) " +
                    "VALUES(?,?,?,?,?)");
            ps.setString(1,user.getName());
            ps.setString(2,user.getSurname());
            ps.setString(3,user.getLogin());
            ps.setString(4,user.getPassword());
            ps.setBlob(5,user.getPicture());
            ps.executeUpdate();
            user.getPicture().close();
            ps.close();
            statement.executeQuery("CREATE TABLE dialogs_" + user.getLogin() + " (\n" +
                    "  `second` VARCHAR(20) NOT NULL,\n" +
                    "  `unread` boolean NOT NULL);");
            statement.executeQuery("CREATE TABLE messages_" + user.getLogin() + " (\n" +
                    "  `sender` VARCHAR(20) NOT NULL,\n" +
                    "  `receiver` VARCHAR(20) NOT NULL,\n" +
                    "  `message` VARCHAR(200) NOT NULL,\n" +
                    "  `date` DATETIME NOT NULL);");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public InputStream getUserImageByLogin(String login){
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users where login = '" + login +"'");
            if(rs.next())
            {
                return rs.getBlob(6).getBinaryStream();
            }
        }catch (SQLException e){
            System.out.println("err");
        }
        return null;
    }

    public User getUserInfoByLogin(String login){
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users where login = '" + login +"'");
            if(rs.next())
            {
                User user = new User();
                user.setId(rs.getInt(1));
                user.setName(rs.getString(2));
                user.setSurname(rs.getString(3));
                user.setLogin(rs.getString(4));
                return user;
            }
        }catch (SQLException e){
            System.out.println("err");
        }
        return null;
    }

    public boolean userExist(User user){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users where login = '" + user.getLogin() + "' and password" +
                    " = '" + user.getPassword() + "'");
            if(resultSet.next()) {
                return true;
            }
        }catch (SQLException e){
            System.out.println("err");
        }
        return false;
    }

    public ArrayList<User> getUsersList() {
        ArrayList<User> users = new ArrayList<User>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users");
            while (rs.next())
            {
                User user = new User();
                user.setName(rs.getString(2));
                user.setSurname(rs.getString(3));
                user.setLogin(rs.getString(4));
                users.add(user);
            }
        }catch (SQLException e){
            System.out.println("err");
        }
        return users;
    }

    public ArrayList<Dialog> getDialogs(String login) {
        ArrayList<Dialog> dialogs = new ArrayList<Dialog>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM dialogs_" + login);
            while (rs.next())
            {
                Dialog dialog = new Dialog();
                String second = rs.getString(1);
                dialog.setDate(getLastMessageDate(login, second).getDate());
                dialog.setLastMessage(getLastMessageDate(login, second).getLastMessage());
                dialog.setSecond(second);
                dialog.setName(getUserInfoByLogin(second).getSurname() + " " + getUserInfoByLogin(second).getName());
                dialog.setUnread(rs.getBoolean(2));
                dialogs.add(dialog);
            }
        }catch (SQLException e){
            System.out.println("err");
        }
        return dialogs;
    }

    //select * from messages_ipav7
    //where (sender = 'ipav7' and receiver = 'emk') or (sender = 'emk' and receiver = 'ipav7')
    // order by date desc limit 1;

    private Dialog getLastMessageDate(String sender, String receiver){
        Dialog dialog = new Dialog();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM messages_" + sender +
                    " where (sender = '" + sender + "' and receiver = '" + receiver + "')" +
                    "or (sender = '" + receiver + "' and receiver = '" + sender + "')" +
                    "order by date desc limit 1");
            if(rs.next()){
                dialog.setLastMessage(rs.getString(3));
                Timestamp timestamp = rs.getTimestamp(4);
                dialog.setDate(new Date(timestamp.getTime()));
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return dialog;
    }

    public ArrayList<Message> getMessages(String name, String receiver) {
        ArrayList<Message> messages = new ArrayList<Message>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM messages_" + name +
                    " where (sender = '" + name + "' and receiver = '" + receiver + "')" +
                    "or (sender = '" + receiver + "' and receiver = '" + name + "')" +
                    "order by date desc");
            while (rs.next()){
                Message message = new Message();
                message.setSender(rs.getString(1));
                message.setReceiver(rs.getString(2));
                message.setMessage(rs.getString(3));
                Timestamp timestamp = rs.getTimestamp(4);
                message.setDate(new Date(timestamp.getTime()));
                messages.add(message);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return messages;
    }
}
