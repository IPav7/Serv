import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;

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
            if(rs.next() == true){
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
}
