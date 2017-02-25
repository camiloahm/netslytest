package biz.netcentric.slightly;

import biz.netcentric.reader.Resource;
import biz.netcentric.reader.ResourceReader;
import biz.netcentric.reader.ResourceReaderDefaultImpl;
import biz.netcentric.slightly.core.SlightlyParser;
import biz.netcentric.slightly.core.SlightlyParserDefaultImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.enterprise.context.RequestScoped;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by camiloh on 2/21/17.
 */
@RequestScoped
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
            //out.println(new SlightlySoupCopy().evalElement(resource.getHtml(),request));
        } catch (URISyntaxException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, resourcePath + " is incorrect");
        } catch (IllegalArgumentException e) {
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