import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Igor Pavinich on 29.11.2017.
 */
public class ServerServlet extends HttpServlet {
    ConnectDB connectDB;
    private UserActions userActions;
    private InputStream inImage;
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
    }

    private void registerOperation(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        PrintWriter wr = resp.getWriter();
        String login = req.getParameter("login");
        String password = req.getParameter("password");
        String name = req.getParameter("name");
        String surname = req.getParameter("surname");
/*
        try {
            Statement statement = connectDB.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM user where login = '" + login +"'");
            if(rs.next() == true){
                wr.write("Exist");
                return;
            }else{
                System.out.println("Not Exist");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            PreparedStatement ps = connectDB.getConnection().prepareStatement("INSERT INTO user (name,surname,login,password,picture) " +
                    "VALUES(?,?,?,?,?)");
            ps.setString(1,name);
            ps.setString(2,surname);
            ps.setString(3,login);
            ps.setString(4,password);
            ps.setBlob(5,inImage);
            ps.executeUpdate();
            inImage.close();
            ps.close();
            wr.write("data upload");
        }catch (Exception e){

        }*/

        User user = new User(login,password,name,surname,inImage);
        if(userActions.registerUser(user)){
            Cookie cookie = new Cookie("login",login);
            MyFilter.names.add(login);
            resp.addCookie(cookie);
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        else
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String operation = req.getParameter("operation");
        if(operation.equals("register"))
            requestPicture(req,resp);
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
            /*BufferedImage bImageFromConvert = ImageIO.read(inImage);
            ImageIO.write(bImageFromConvert,"jpg",new File("1.jpg"));*/


            PrintWriter writer = resp.getWriter();
            writer.write("picture upload");
        } catch (IOException e) {
            try{
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print(e.getMessage());
                resp.getWriter().close();
            } catch (IOException ioe) {
            }
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
            MyFilter.names.add(login);
            resp.addCookie(cookie);
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        System.out.println(user);
    }
}
