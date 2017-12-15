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

    public boolean registerUser(User user) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users where login = '" + user.getLogin() + "'");
            if (rs.next()) {
                return false;
            }

            InputStream picture = user.getPicture();

            System.out.println("register");
            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (name,surname,login,password,picture) " +
                    "VALUES(?,?,?,?,?)");
            ps.setString(1, user.getName());
            ps.setString(2, user.getSurname());
            ps.setString(3, user.getLogin());
            ps.setString(4, user.getPassword());
            ps.setBlob(5, picture);
            ps.executeUpdate();
            ps.close();
            user.getPicture().close();
            // is.close();
            // statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE dialogs_" + user.getLogin() + " (" +
                    "second VARCHAR(20) NOT NULL, " +
                    " unread boolean NOT NULL);");
            // statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE messages_" + user.getLogin() + " (" +
                    "sender VARCHAR(20) NOT NULL, " +
                    " receiver VARCHAR(20) NOT NULL, " +
                    " message VARCHAR(200) NOT NULL, " +
                    " date DATETIME NOT NULL);");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public InputStream getUserImageByLogin(String login, String size) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users where login = '" + login + "'");
            if (rs.next()) {
                if (size.equals("full")) {
                    System.out.println("full");
                    return rs.getBlob(6).getBinaryStream();
                } else if (size.equals("small")) {
                    System.out.println("small");
                    BufferedImage bufferedImage = createResizedCopy(ImageIO.read(rs.getBlob(6).getBinaryStream()),
                            60, 60, true);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", os);
                    return new ByteArrayInputStream(os.toByteArray());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserInfoByLogin(String login) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users where login = '" + login + "'");
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt(1));
                user.setName(rs.getString(2));
                user.setSurname(rs.getString(3));
                user.setLogin(rs.getString(4));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean userExist(User user) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users where login = '" + user.getLogin() + "' and password" +
                    " = '" + user.getPassword() + "'");
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<User> getUsersList() {
        ArrayList<User> users = new ArrayList<User>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users ");
            while (rs.next()) {
                User user = new User();
                user.setName(rs.getString(2));
                user.setSurname(rs.getString(3));
                user.setLogin(rs.getString(4));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public ArrayList<Dialog> getDialogs(String login) {
        ArrayList<Dialog> dialogs = new ArrayList<Dialog>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM dialogs_" + login);
            while (rs.next()) {
                Dialog dialog = new Dialog();
                String second = rs.getString(1);
                dialog.setDate(getLastMessageDate(login, second).getDate());
                dialog.setLastMessage(getLastMessageDate(login, second).getLastMessage());
                dialog.setSecond(second);
                dialog.setName(getUserInfoByLogin(second).getSurname() + " " + getUserInfoByLogin(second).getName());
                dialog.setUnread(rs.getBoolean(2));
                dialogs.add(dialog);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dialogs;
    }

    private Dialog getLastMessageDate(String sender, String receiver) {
        Dialog dialog = new Dialog();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM messages_" + sender +
                    " where (sender = '" + sender + "' and receiver = '" + receiver + "')" +
                    "or (sender = '" + receiver + "' and receiver = '" + sender + "')" +
                    "order by date desc limit 1");
            if (rs.next()) {
                dialog.setLastMessage(rs.getString(3));
                Timestamp timestamp = rs.getTimestamp(4);
                dialog.setDate(timestamp.getTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dialog;
    }

    public ArrayList<Message> getMessages(String name, String receiver) {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM messages_" + name +
                    " where (sender = '" + name + "' and receiver = '" + receiver + "')" +
                    "or (sender = '" + receiver + "' and receiver = '" + name + "')" +
                    "order by date");
            while (rs.next()) {
                Message message = new Message();
                message.setSender(rs.getString(1));
                message.setReceiver(rs.getString(2));
                message.setMessage(rs.getString(3));
                message.setDate(rs.getTimestamp(4).getTime());
                messages.add(message);
            }
            statement.executeUpdate("update dialogs_" + name + " set unread = FALSE where second = '" + receiver + "';");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }

    public void addMessage(Message message) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO messages_" + message.getSender() +
                    " VALUES(?,?,?,?)");
            ps.setString(1, message.getSender());
            ps.setString(2, message.getReceiver());
            ps.setString(3, message.getMessage());
            ps.setTimestamp(4, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            ps.executeUpdate();
            ps.close();
            Statement statement = connection.createStatement();
            ResultSet set = statement.executeQuery("SELECT * FROM dialogs_" + message.getSender() + " WHERE second = '" + message.getReceiver() + "'");
            if (!set.next()) {
                statement.executeUpdate("INSERT INTO dialogs_" + message.getSender() + " VALUES ('" + message.getReceiver() + "', FALSE)");
            }
            set = statement.executeQuery("SELECT * FROM dialogs_" + message.getReceiver() + " WHERE second = '" + message.getSender() + "'");
            if (!set.next()) {
                statement.executeUpdate("INSERT INTO dialogs_" + message.getReceiver() + " VALUES ('" + message.getSender() + "', TRUE )");
            } else
                statement.executeUpdate("update dialogs_" + message.getReceiver() + " set unread = true where second = '" + message.getSender() + "';");
            PreparedStatement ps2 = connection.prepareStatement("INSERT INTO messages_" + message.getReceiver() +
                    " VALUES(?,?,?,?)");
            ps2.setString(1, message.getSender());
            ps2.setString(2, message.getReceiver());
            ps2.setString(3, message.getMessage());
            ps2.setTimestamp(4, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            ps2.executeUpdate();
            ps2.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage createResizedCopy(java.awt.Image originalImage,
                                                  int scaledWidth, int scaledHeight,
                                                  boolean preserveAlpha) {
        System.out.println("resizing...");
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
}
