import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


public class UserActions {
    Connection connection;

    public UserActions(Connection connection) {
        this.connection = connection;
    }

    public boolean userExist(User user){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM user where login = '" + user.getLogin() + "' and password" +
                    " = '" + user.getPassword() + "'");
            if(resultSet.next()) {
                return true;
            }
        }catch (SQLException e){
            System.out.println("err");
        }
        return false;
    }

    public boolean registerUser(User user){
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM user where login = '" + user.getLogin() +"'");
            if(rs.next() == true){
                return false;
            }

            PreparedStatement ps = connection.prepareStatement("INSERT INTO user (name,surname,login,password,picture) " +
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
}