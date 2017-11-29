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


public class ServerServlet extends HttpServlet {
    ConnectDB connectDB;
    private InputStream inImage;
    @Override
    public void init() throws ServletException {
        connectDB = new ConnectDB();
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
        String operation = req.getParameter("operation");
        if(operation.equals("register"))
            requestPicture(req, resp);
    }

    private void requestPicture(HttpServletRequest req,HttpServletResponse resp) throws IOException {
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
    }

    private void registerOperation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter wr = resp.getWriter();
        String login = req.getParameter("login");
        String password = req.getParameter("password");
        String name = req.getParameter("name");
        String surname = req.getParameter("surname");
        try {
            Statement statement = connectDB.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM users where login = '" + login +"'");
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
            PreparedStatement ps = connectDB.getConnection().prepareStatement("INSERT INTO users (name,surname,login,password,picture) " +
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
