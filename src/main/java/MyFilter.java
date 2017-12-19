import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Igor Pavinich on 29.11.2017.
 */
public class MyFilter implements Filter {

    private FilterConfig filterConfig;
    public static Map<String, Long> names;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        names = new HashMap<>();
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        boolean found = false;
        Cookie[] cookies = request.getCookies();
        Set keys = names.keySet();
        if (cookies!=null){
            for (Cookie cookie : cookies){
                if(keys.contains(cookie.getName())) {
                    found = true;
                    if(!request.getParameter("operation").equals("messages") && !request.getParameter("operation").equals("sendmessage")
                            && !request.getParameter("operation").equals("sendSound"))
                    names.put(cookie.getName(), System.currentTimeMillis());
                }
            }
        }
        if(found || request.getParameter("operation").equals("login")
                 || request.getParameter("operation").equals("register"))
        filterChain.doFilter(servletRequest, servletResponse);
        else ((HttpServletResponse)servletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    public void destroy() {
    }

}
