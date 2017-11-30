import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by Igor Pavinich on 29.11.2017.
 */
public class ServerServlet extends HttpServlet {
    ConnectDB connectDB;
    UserActions userActions;
    private InputStream inImage = null;
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
        else if(operation.equals("register"))
            registerOperation(req,resp);
        else if(operation.equals("messages"))
            messagesOperation(req, resp);
        else if(operation.equals("profile")) {
            profileOperation(req, resp);
        }
    }

    private void profileOperation(HttpServletRequest req, HttpServletResponse resp) {
        System.out.println("1profileoperation");
        String login = req.getParameter("login");
        if(login == null)
        login = req.getCookies()[0].getName();
        User user = userActions.getUserByLogin(login);
        if(user != null)
        {
            System.out.println("3profileoperation");
            Gson gson = new Gson();
            String json = gson.toJson(user);
            System.out.println("4json " + json);
            try {
                resp.getWriter().write(json);
            }catch (IOException e)
            {
                System.out.println("err send");
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private void registerOperation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String login = req.getParameter("login");
        String password = req.getParameter("password");
        String name = req.getParameter("name");
        String surname = req.getParameter("surname");

        User user = new User(login,password,name,surname,inImage);
        if(userActions.registerUser(user)){
            Cookie cookie = new Cookie("login",login);
            if(!MyFilter.names.contains(login)) MyFilter.names.add(login);
            resp.addCookie(cookie);
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        else
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String operation = req.getParameter("operation");
        if(operation.equals("register")) {
            requestPicture(req, resp);
        }
    }

    private void requestPicture(HttpServletRequest req, HttpServletResponse resp) {
        try {
            int len = req.getContentLength();
            System.out.println(len);
            byte[] input = new byte[len];
            ServletInputStream sin = req.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            while((nRead = sin.read(input,0,input.length))!= -1){
                buffer.write(input,0,nRead);
            }
            buffer.flush();
            inImage = new ByteArrayInputStream(buffer.toByteArray());
            if(len>0) resp.setStatus(HttpServletResponse.SC_OK);
            else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
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
            if(!MyFilter.names.contains(login)) MyFilter.names.add(login);
            resp.addCookie(cookie);
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        System.out.println(user);
    }
}
