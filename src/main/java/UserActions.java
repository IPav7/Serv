import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    public boolean isOnline(String login){
            if(MyFilter.names.keySet().contains(login)) {
                long lastSeen = MyFilter.names.get(login);
                return ((System.currentTimeMillis() - lastSeen) < 300000);
            }else return false;
    }

    public boolean registerUser(User user){
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users where login = '" + user.getLogin() +"'");
            if(rs.next()) {
                return false;
            }
            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (name,surname,login,password,full,small) " +
                    "VALUES(?,?,?,?,?,?)");
            ps.setString(1,user.getName());
            ps.setString(2,user.getSurname());
            ps.setString(3,user.getLogin());
            ps.setString(4,user.getPassword());
            if(user.getPicture()!=null) {
                InputStream[] copies = copyInputStream(user.getPicture());
                ps.setBlob(5, copies[0]);
                ps.setBlob(6, copies[1]);
                user.getPicture().close();
            }else {
                ps.setNull(5, Types.NULL);
                ps.setNull(6, Types.NULL);
            }
            ps.executeUpdate();
            ps.close();
           // statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE friends_" + user.getLogin() + " (" +
                    "friend VARCHAR(20) NOT NULL);");
            statement.executeUpdate("CREATE TABLE dialogs_" + user.getLogin() + " (" +
                    "second VARCHAR(20) NOT NULL, " +
                    " unread boolean NOT NULL);");
           // statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE messages_" + user.getLogin() + " (" +
                    "sender VARCHAR(20) NOT NULL, " +
                    " receiver VARCHAR(20) NOT NULL, " +
                    " message VARCHAR(200) NOT NULL DEFAULT '', " +
                    " date DATETIME NOT NULL, " +
                    " kind varchar(10) NOT NULL, " +
                    " sound MEDIUMBLOB);");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public InputStream getUserImageByLogin(String login, String size) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs;
            rs = statement.executeQuery("SELECT " + size + " FROM users where login = '" + login + "'");
            if (rs.next()) {
                    Blob blob = rs.getBlob(1);
                    if(blob!=null) return blob.getBinaryStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage createResizedCopy(java.awt.Image originalImage,
                                                  int scaledWidth, int scaledHeight,
                                                  boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    public User getUserInfoByLogin(String first, String login){
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users where login = '" + login +"'");
            if(rs.next())
            {
                User user = new User();
                user.setId(rs.getInt(1));
                user.setName(rs.getString(2));
                user.setSurname(rs.getString(3));
                user.setOnline(isOnline(login));
                user.setFriend(isFriend(first, login));
                user.setLogin(login);
                return user;
            }
        }catch (SQLException e){
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<User> getUsersList(String query) {
        ArrayList<User> users = new ArrayList<User>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * from users WHERE CONCAT_WS('', name, surname, login) LIKE" +
                    " '%" + query + "%';");
            while (rs.next())
            {
                User user = new User();
                user.setName(rs.getString(2));
                user.setSurname(rs.getString(3));
                user.setLogin(rs.getString(4));
                user.setOnline(isOnline(user.getLogin()));
                users.add(user);
            }
        }catch (SQLException e){
            e.printStackTrace();
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
                dialog.setName(getUserInfoByLogin(login, second).getSurname() + " " + getUserInfoByLogin(login, second).getName());
                dialog.setUnread(rs.getBoolean(2));
                dialog.setType(getLastMessageDate(login, second).getType());
                dialog.setOnline(isOnline(second));
                dialogs.add(dialog);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return dialogs;
    }

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
                dialog.setDate(timestamp.getTime());
                dialog.setType(rs.getString(5));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return dialog;
    }

    public ArrayList<Message> getMessages(String name, String receiver, String all) {
        String time;
        if(all.equals("true"))
            time = "1970-01-01 03:00:00";
        else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            time = dateFormat.format(new Date(MyFilter.names.get(name)));
        }
        ArrayList<Message> messages = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM messages_" + name +
                    " where ((sender = '" + name + "' and receiver = '" + receiver + "')" +
                    "or (sender = '" + receiver + "' and receiver = '" + name + "'))" +
                    " and date > '" + time + "' order by date");
            MyFilter.names.put(name, System.currentTimeMillis());
            while (rs.next()){
                Message message = new Message();
                message.setSender(rs.getString(1));
                message.setReceiver(rs.getString(2));
                message.setMessage(rs.getString(3));
                message.setDate(rs.getTimestamp(4).getTime());
                message.setType(rs.getString(5));
                messages.add(message);
            }
            statement.executeUpdate("update dialogs_" + name + " set unread = FALSE where second = '" + receiver + "';");
        }catch (Exception e){
            e.printStackTrace();
        }
//        System.out.println("----------------\n" + name + " send: " + time);
//        for (Message message :
//                messages) {
//            System.out.print(message.getMessage() + " ");
//        }
//        System.out.println("\n-----------------");
        return messages;
    }

    public void addTextMessage(Message message) {
        try{
            PreparedStatement ps = connection.prepareStatement("INSERT INTO messages_" + message.getSender() +
                    " (sender, receiver, message, date, kind) VALUES(?,?,?,?,?)");
            ps.setString(1,message.getSender());
            ps.setString(2,message.getReceiver());
            ps.setString(3,message.getMessage());
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setString(5, message.getType());
            ps.executeUpdate();
            ps.close();
            Statement statement = connection.createStatement();
            ResultSet set = statement.executeQuery("SELECT * FROM dialogs_" + message.getSender() + " WHERE second = '" + message.getReceiver() + "'");
            if(!set.next()){
                statement.executeUpdate("INSERT INTO dialogs_" + message.getSender() + " VALUES ('" + message.getReceiver() + "', FALSE)");
            }
            set = statement.executeQuery("SELECT * FROM dialogs_" + message.getReceiver() + " WHERE second = '" + message.getSender() + "'");
            if(!set.next()){
                statement.executeUpdate("INSERT INTO dialogs_" + message.getReceiver() + " VALUES ('" + message.getSender() + "', TRUE )");
            }else statement.executeUpdate("update dialogs_" + message.getReceiver() + " set unread = true where second = '" + message.getSender() + "';");
            PreparedStatement ps2 = connection.prepareStatement("INSERT INTO messages_" + message.getReceiver() +
                    " (sender, receiver, message, date, kind) VALUES(?,?,?,?,?)");
            ps2.setString(1,message.getSender());
            ps2.setString(2,message.getReceiver());
            ps2.setString(3,message.getMessage());
            ps2.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps2.setString(5, message.getType());
            ps2.executeUpdate();
            ps2.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addSoundMessage(Message message) {
        try{
            PreparedStatement ps = connection.prepareStatement("INSERT INTO messages_" + message.getSender() +
                    " (sender, receiver, sound, date, kind) VALUES(?,?,?,?,?)");
            ps.setString(1,message.getSender());
            ps.setString(2,message.getReceiver());
            ps.setBlob(3,message.getSound());
            long buf = System.currentTimeMillis();
            if(buf%(buf/1000)>=500) buf = buf-500;
            ps.setTimestamp(4, new Timestamp(buf));
            ps.setString(5, message.getType());
            ps.executeUpdate();
            ps.close();
            Statement statement = connection.createStatement();
            ResultSet set = statement.executeQuery("SELECT * FROM dialogs_" + message.getSender() + " WHERE second = '" + message.getReceiver() + "'");
            if(!set.next()){
                statement.executeUpdate("INSERT INTO dialogs_" + message.getSender() + " VALUES ('" + message.getReceiver() + "', FALSE)");
            }
            set = statement.executeQuery("SELECT * FROM dialogs_" + message.getReceiver() + " WHERE second = '" + message.getSender() + "'");
            if(!set.next()){
                statement.executeUpdate("INSERT INTO dialogs_" + message.getReceiver() + " VALUES ('" + message.getSender() + "', TRUE )");
            }else statement.executeUpdate("update dialogs_" + message.getReceiver() + " set unread = true where second = '" + message.getSender() + "';");
            PreparedStatement ps2 = connection.prepareStatement("INSERT INTO messages_" + message.getReceiver() +
                    " (sender, receiver, sound, date, kind) VALUES(?,?,?,?,?)");
            ps2.setString(1,message.getSender());
            ps2.setString(2,message.getReceiver());
            ps2.setBlob(3,message.getSound());
            ps2.setTimestamp(4, new Timestamp(buf));
            ps2.setString(5, message.getType());
            ps2.executeUpdate();
            ps2.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public InputStream getMessageSound(String sender, String receiver, long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateFormat.format(new Date(date));
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT sound FROM messages_" + sender +
                    " where (sender = '" + sender + "' and receiver = '" + receiver + "' and date = '" + time + "') " +
                    "or (sender = '" + receiver + "' and receiver = '" + sender + "'" + " and date = '" + time + "')");
            if(rs.next())
                return rs.getBlob("sound").getBinaryStream();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean editProfile(String login, String name, String surname) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("update users set name = '" + name + "' where login = '" + login + "';");
            statement.executeUpdate("update users set surname = '" + surname + "' where login = '" + login + "';");
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean editProfileImage(String login, InputStream stream) {
        try {
            InputStream[] copies = copyInputStream(stream);
            PreparedStatement ps = connection.prepareStatement("update users set full = ? where login = '" + login + "';");
            ps.setBlob(1,copies[0]);
            ps.executeUpdate();
            ps.close();
            PreparedStatement ps2 = connection.prepareStatement("update users set small = ? where login = '" + login + "';");
            ps2.setBlob(1,copies[1]);
            ps2.executeUpdate();
            ps2.close();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private InputStream[] copyInputStream(InputStream input){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            BufferedImage bufferedImage = createResizedCopy(ImageIO.read(new ByteArrayInputStream(baos.toByteArray())),
                    100, 100, true);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", os);
            return new InputStream[]{new ByteArrayInputStream(baos.toByteArray()), new ByteArrayInputStream(os.toByteArray())};
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<User> getUserFriends(String login) {
        ArrayList<User> users = new ArrayList<User>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * from friends_" + login + ";");
            while (rs.next())
            {
                users.add(getUserInfoByLogin(login, rs.getString("friend")));
            }
            rs.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return users;
    }

    private boolean isFriend(String login, String second){
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * from friends_" + login + " WHERE friend = '" + second + "';");
            if (rs.next())
            return true;
            else return false;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public void addFriend(String name, String second, String add) {
        try {
            Statement statement = connection.createStatement();
            if(add.equals("true"))
            statement.executeUpdate("INSERT INTO friends_" + name + " (friend) VALUES ('" + second + "')");
            else
                statement.executeUpdate("DELETE from friends_" + name + " where friend='" + second + "'");
                 }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
