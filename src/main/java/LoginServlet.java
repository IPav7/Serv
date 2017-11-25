import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Igor Pavinich on 24.11.2017.
 */
@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    ConnectDB connectDB;
    @Override
    public void init() throws ServletException {
        connectDB = new ConnectDB();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String password = req.getParameter("password");
        try {
            Statement statement = connectDB.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM user where name = '" + name + "' and password" +
                    " = '" + password + "'");
            resp.setContentType("text/html");
            PrintWriter writer = resp.getWriter();
            if(resultSet.next() == true) {
                writer.write("OK");
            }
            else writer.write("NO");

        }catch (SQLException e){
            System.out.println("err");
        }
    }
}
