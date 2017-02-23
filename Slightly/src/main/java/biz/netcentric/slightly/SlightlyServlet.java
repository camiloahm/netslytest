package biz.netcentric.slightly;

import biz.netcentric.reader.Resource;
import biz.netcentric.reader.ResourceReader;
import biz.netcentric.reader.ResourceReaderDefaultImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
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
        String resourcePath = request.getServletPath();

        try (PrintWriter out = response.getWriter()) {
            URI uri = getServletContext().getResource(resourcePath).toURI();
            ResourceReader resourceReader = new ResourceReaderDefaultImpl();
            Resource resource = resourceReader.getResource(uri);
            out.println(new SlightlySoup().parse(resource.getHtml(), request));
        } catch (URISyntaxException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, resourcePath + " is incorrect");
        } catch (ScriptException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}