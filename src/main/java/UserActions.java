import java.io.IOException;
import java.sql.*;

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

    public int getIdByLogin(String login){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users where login = '" + login + "'");
            if(resultSet.next()) {
                return resultSet.getInt(1);
            }
        }catch (SQLException e){
            System.out.println("err");
        }
        return -1;
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
}
