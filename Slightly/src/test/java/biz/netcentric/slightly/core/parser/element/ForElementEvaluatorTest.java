package biz.netcentric.slightly.core.parser.element;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by dev-camiloh on 2/25/17.
 */
public class ForElementEvaluatorTest {

    private ScriptEngine engine;
    private static final String NASHRON_ENGINE = "nashorn";
    private static final String NASHRON_MOZILLA = "load(\"nashorn:mozilla_compat.js\");";
    private ElementEvaluator forElementEvaluator;

    @Before
    public void init() throws ScriptException {
        engine = new ScriptEngineManager().getEngineByName(NASHRON_ENGINE);
        engine.eval(NASHRON_MOZILLA);
        engine.eval("importClass(Packages.biz.netcentric.app.Person);\n" +
                "var id=\"2\";\n" +
                "var person=Person.lookup(id);");
        forElementEvaluator = new ForElementEvaluator();
    }


    @Test
    public void evalNoneForElement() {
        Document doc = Jsoup.parseBodyFragment("\"<h1 tittle=\\\"tittle\\\"></h1>\";");
        String htmlBeforeEval = doc.html();
        Elements elements = doc.getAllElements();
        elements.forEach(x -> forElementEvaluator.evalElement(engine, x, new ArrayList<>()));
        assertThat(htmlBeforeEval, equalTo(doc.html()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void evalInvalidFor() {
        Document doc = Jsoup.parseBodyFragment("<div data-for-child=\"wrong.children\">Child: ${child}</div>");
        Elements elements = doc.getAllElements();
        elements.forEach(x -> forElementEvaluator.evalElement(engine, x, new ArrayList<>()));
    }

    @Test
    public void evalValidForElement() {
        Document doc = Jsoup.parseBodyFragment("<div data-for-child=\"person.children\">Child: ${child}</div>");
        Elements elements = doc.getAllElements();
        elements.forEach(x -> forElementEvaluator.evalElement(engine, x, new ArrayList<>()));
        assertThat(doc.html(), equalTo("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
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
                "  </div>\n" +
                " </body>\n" +
                "</html>"));
    }


}
