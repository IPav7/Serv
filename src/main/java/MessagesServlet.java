import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by Igor Pavinich on 27.11.2017.
 */
@WebServlet(urlPatterns = "/messages")
public class MessagesServlet extends HttpServlet {
    ConnectDB connectDB;

    @Override
    public void init() throws ServletException {
        connectDB = new ConnectDB();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sender = req.getParameter("sender");
        System.out.println(sender);
        try {
            Statement statement = connectDB.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM dialogs where sender = " + sender);
            ArrayList<Dialog> dialogs = new ArrayList();
            while (resultSet.next()) {
                dialogs.add(new Dialog(resultSet.getInt(1), resultSet.getInt(2),
                        resultSet.getString(3)));
            }
                Gson gson = new Gson();
            resp.setContentType("text/html");
            resp.getWriter().write(gson.toJson(dialogs));
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
