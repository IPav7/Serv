import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Igor Pavinich on 29.11.2017.
 */
public class ServerServlet extends HttpServlet {
    ConnectDB connectDB;
    @Override
    public void init() throws ServletException {
        connectDB = new ConnectDB();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String operation = req.getParameter("operation");
        if(operation.equals("login"))
            loginOperation(req, resp);
        else if(operation.equals("messages"))
            messagesOperation(req, resp);
     /*   try {
            Statement statement = connectDB.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM test where login = '" + name + "' and password" +
                    " = '" + password + "'");
            resp.setContentType("text/html");
            PrintWriter writer = resp.getWriter();
            if(resultSet.next()) {
                writer.write(String.valueOf(resultSet.getInt(1)));
                System.out.println(resultSet.getInt(1));
            }
            else writer.write("0");

        }catch (SQLException e){
            System.out.println("err");
        }*/
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    private void messagesOperation(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.getWriter().write("signin ok");
        }
        catch (IOException e){
            System.out.println("xcvbn");
        }
    }

    private void loginOperation(HttpServletRequest req, HttpServletResponse resp) {
        String login = req.getParameter("login");
        String password = req.getParameter("password");
        User user = new User(login, password);

        try {
            Statement statement = connectDB.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM test where login = '" + user.getLogin() + "' and password" +
                    " = '" + user.getPassword() + "'");
            resp.setContentType("text/html");
            PrintWriter writer = resp.getWriter();
            if(resultSet.next()) {
                Cookie cookie = new Cookie("login", login);
                cookie.setMaxAge(1800);
                MyFilter.names.add(login);
                resp.addCookie(cookie);
                writer.write("ok");
            }
            else writer.write("0");

        }catch (SQLException e){
            System.out.println("err");
        }
        catch (IOException ex){
            System.out.println("erergd");
        }

        System.out.println(user);
    }
}
