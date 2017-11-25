import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by Igor Pavinich on 03.11.2017.
 */
public class ConnectDB {
    private final String URL =  "jdbc:mysql://localhost:3306/messengerdb?autoReconnect=true&useSSL=false";
    private final String USER = "root";
    private final String PASSWORD = "root";
    Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public ConnectDB() {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/messengerdb?user=root&password=root");
            if(!connection.isClosed())
                System.out.println("CONNECTED");
        }
        catch (Exception e){
            System.out.println("ERROR CONNECTION");
        }
    }
}
