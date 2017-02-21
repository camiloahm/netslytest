package biz.netcentric.slightly;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.script.ScriptException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by camiloh on 2/21/17.
 */
@WebServlet("/")
public class SlightlyServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        response.setBufferSize(8192);
        PrintWriter out = response.getWriter();
        URL url = getServletContext().getResource("/app/index.html");
        String html="";

        //Get the file contents
        try (Stream<String> stream = Files.lines(Paths.get(url.getPath()))) {
            html = stream.reduce("", (a, b) -> a + b);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Create instance, parse and write result to OutputStream
        try {
            out.println(new SlightlySoup().parse(html, request));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        //Close OutputStream
        out.close();

    }

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}