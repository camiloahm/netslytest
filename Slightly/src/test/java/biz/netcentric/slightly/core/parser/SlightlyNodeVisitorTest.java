package biz.netcentric.slightly.core.parser;

import biz.netcentric.slightly.core.parser.element.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by dev-camiloh on 2/25/17.
 */
public class SlightlyNodeVisitorTest {

    private ScriptEngine engine;
    private static final String NASHRON_ENGINE = "nashorn";
    private static final String NASHRON_MOZILLA = "load(\"nashorn:mozilla_compat.js\");";


    @Before
    public void init() throws ScriptException {
        engine = new ScriptEngineManager().getEngineByName(NASHRON_ENGINE);
        engine.eval(NASHRON_MOZILLA);
    }


    @Test
    public void traverseDocument() {
        Document doc = Jsoup.parse("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<script type=\"server/javascript\">\n" +
                "importClass(Packages.biz.netcentric.app.Person);\n" +
                "var id=\"2\";\n" +
                "var person=Person.lookup(id);\n" +
                "</script>\n" +
                "<head>\n" +
                "\t<title>${person.name}</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1 title=\"${person.name}\">${person.name}</h1>\n" +
                "<h2 data-if=\"person.married\" title=\"${person.spouse}\">Spouse:\n" +
                "\t${person.spouse}</h2>\n" +
                "<div data-for-child=\"person.children\">Child: ${child}</div>\n" +
                "</body>\n" +
                "</html>");
        List<ElementEvaluator> elementEvaluators = new ArrayList<>();
        elementEvaluators.add(new ScriptElementEvaluator());
        elementEvaluators.add(new AttributeElementEvaluator());
        elementEvaluators.add(new IfElementEvaluator());
        elementEvaluators.add(new ForElementEvaluator());
        List<Node> trash = new ArrayList<>();
        doc.traverse(new SlightlyNodeVisitor(engine, elementEvaluators, trash));
        trash.forEach(x -> x.remove());
        assertThat(doc.html(), equalTo("<!doctype html>\n" +
                "<html>\n" +
                " <head>\n" +
                "  <script type=\"server/javascript\">\n" +
                "importClass(Packages.biz.netcentric.app.Person);\n" +
                "var id=\"2\";\n" +
                "var person=Person.lookup(id);\n" +
                "</script>  \n" +
                "  <title>Name 2</title> \n" +
                " </head> \n" +
                " <body> \n" +
                "  <h1 title=\"Name 2\">Name 2</h1> \n" +
                "  <h2 data-if=\"person.married\" title=\"Spouse 2\">Spouse: Spouse 2</h2> \n" +
                "  <div data-for-child=\"person.children\">\n" +
                "   <div>\n" +
                "    Child: Children 0\n" +
                "   </div>\n" +
                "   <div>\n" +
                "    Child: Children 1\n" +
                "   </div>\n" +
                "   <div>\n" +
                "    Child: Children 2\n" +
                "   </div>\n" +
                "  </div>  \n" +
                " </body>\n" +
                "</html>"));
    }

}
