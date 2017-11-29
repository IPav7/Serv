import jdk.net.SocketFlow;

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
    UserActions userActions;
    @Override
    public void init() throws ServletException {
        connectDB = new ConnectDB();
        userActions = new UserActions(connectDB.getConnection());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String operation = req.getParameter("operation");
        if(operation.equals("login"))
            loginOperation(req, resp);
        else if(operation.equals("messages"))
            messagesOperation(req, resp);
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
        if(userActions.userExist(user)) {
            Cookie cookie = new Cookie("login", login);
            MyFilter.names.add(login);
            resp.addCookie(cookie);
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        System.out.println(user);
    }
}
