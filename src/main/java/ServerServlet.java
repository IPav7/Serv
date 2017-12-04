import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        else if(operation.equals("dialogs"))
            dialogsOperation(req, resp);
        else if(operation.equals("profile"))
            profileOperation(req, resp);
        else if(operation.equals("search"))
            searchOperation(req, resp);
        else if(operation.equals("messages"))
            messagesOperation(req, resp);
    }

    private void messagesOperation(HttpServletRequest req, HttpServletResponse resp) {
        ArrayList<Message> messages = userActions.getMessages(req.getCookies()[0].getName(),
                req.getParameter("receiver"));
        if(messages!=null) {
            try{
                Gson gson = new Gson();
                String json = gson.toJson(messages);
                resp.setCharacterEncoding("windows-1251");
                resp.getWriter().write(json);
                resp.setStatus(HttpServletResponse.SC_OK);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private void dialogsOperation(HttpServletRequest req, HttpServletResponse resp) {
            ArrayList<Dialog> dialogs = userActions.getDialogs(req.getCookies()[0].getName());
            if(dialogs!=null) {
                try{
                    dialogs.sort(new Comparator<Dialog>() {
                        @Override
                        public int compare(Dialog o1, Dialog o2) {
                            return new Date(o2.getDate()).compareTo(new Date(o1.getDate()));
                        }
                    });
                    Gson gson = new Gson();
                    String json = gson.toJson(dialogs);
                    resp.setCharacterEncoding("windows-1251");
                    resp.getWriter().write(json);
                    resp.setStatus(HttpServletResponse.SC_OK);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private void searchOperation(HttpServletRequest req, HttpServletResponse resp) {
        ArrayList<User> users = userActions.getUsersList();
        if(users!=null){
            try{
                Gson gson = new Gson();
                String json = gson.toJson(users);
                resp.setCharacterEncoding("windows-1251");
                resp.getWriter().write(json);
                resp.setStatus(HttpServletResponse.SC_OK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private void profileOperation(HttpServletRequest req, HttpServletResponse resp) {
        if(req.getParameter("type").equals("info"))
            getProfileInfo(req, resp);
        else getProfileImage(req, resp);
    }

    private void getProfileImage(HttpServletRequest req, HttpServletResponse resp) {
        String login = req.getParameter("login");
        if(login == null)
            login = req.getCookies()[0].getName();
        InputStream inputStream = userActions.getUserImageByLogin(login);
        if(inputStream != null)
        {
            try {
                resp.setContentLength(inputStream.available());
                resp.addHeader("Cache-Control", "no-cache");
                ServletOutputStream out = resp.getOutputStream();
                resp.setContentType("image/jpg");
                int length;
                byte[] buf = new byte[1024];
                while ((length = inputStream.read(buf)) != -1){
                    out.write(buf, 0, length);
                }
                inputStream.close();
                out.flush();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private void getProfileInfo(HttpServletRequest req, HttpServletResponse resp) {
        String login = req.getParameter("login");
        if(login == null)
            login = req.getCookies()[0].getName();
        User user = userActions.getUserInfoByLogin(login);
        if(user != null)
        {
            try {
                Gson gson = new Gson();
                String json = gson.toJson(user);
                resp.setCharacterEncoding("windows-1251");
                resp.getWriter().write(json);
            }catch (IOException e)
            {
                e.printStackTrace();
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
        if(operation.equals("sendmessage"))
            getMessageOperation(req, resp);
    }

    private void getMessageOperation(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.setCharacterEncoding("windows-1251");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    req.getInputStream()));
            Message message = new Gson().fromJson(in.readLine(), Message.class);
            in.close();
            message.setDate(System.currentTimeMillis());
            userActions.addMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPicture(HttpServletRequest req, HttpServletResponse resp) {
        try {
            int len = req.getContentLength();
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

    private void loginOperation(HttpServletRequest req, HttpServletResponse resp) {
        resp.setCharacterEncoding("windows-1251");
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
    }
}
