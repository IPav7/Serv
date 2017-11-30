import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by Igor Pavinich on 29.11.2017.
 */
public class MyFilter implements Filter {

    private FilterConfig filterConfig;
    public static ArrayList<String> names;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        names = new ArrayList<String>();
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        boolean found = false;
        Cookie[] cookies = request.getCookies();
        System.out.print("cookies: ");
        for (String s :
                names) {
            System.out.print(s + " ");
        }
        if (cookies!=null){
            for (Cookie cookie : cookies){
                System.out.println("\ncookie: " + cookie.getName());
                if(names.contains(cookie.getName())) {
                    found = true;
                    System.out.println("found");
                }
            }
        }
        if(found || request.getParameter("operation").equals("login")
                 || request.getParameter("operation").equals("register"))
        filterChain.doFilter(servletRequest, servletResponse);
        else servletResponse.getWriter().write("No access");
    }

    public void destroy() {
        System.out.println("Filter destroy");
    }

}
