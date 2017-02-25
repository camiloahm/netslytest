package biz.netcentric.slightly;

import biz.netcentric.slightly.core.reader.Resource;
import biz.netcentric.slightly.core.reader.ResourceReader;
import biz.netcentric.slightly.core.reader.ResourceReaderDefaultImpl;
import biz.netcentric.slightly.core.parser.SlightlyParser;
import biz.netcentric.slightly.core.parser.SlightlyParserDefaultImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

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

    private SlightlyParser slightlyParser;
    private ResourceReader resourceReader;

    public SlightlyServlet() {
        slightlyParser = new SlightlyParserDefaultImpl();
        resourceReader = new ResourceReaderDefaultImpl();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        String resourcePath = request.getServletPath();

        try (PrintWriter out = response.getWriter()) {
            URI uri = getServletContext().getResource(resourcePath).toURI();
            Resource resource = resourceReader.getResource(uri);
            out.println(slightlyParser.parse(request, resource.getHtml()));

        } catch (URISyntaxException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, resourcePath + " is incorrect");
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Resource cannot be parsed " + e.getMessage());
        }

    }

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}