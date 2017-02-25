package biz.netcentric.slightly.core.parser.element;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
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
public class IfElementEvaluatorTest {

    private ScriptEngine engine;
    private static final String NASHRON_ENGINE = "nashorn";
    private static final String NASHRON_MOZILLA = "load(\"nashorn:mozilla_compat.js\");";
    private ElementEvaluator ifElementEvaluator;

    @Before
    public void init() throws ScriptException {
        engine = new ScriptEngineManager().getEngineByName(NASHRON_ENGINE);
        engine.eval(NASHRON_MOZILLA);
        engine.eval("importClass(Packages.biz.netcentric.app.Person);\n" +
                "var id=\"1\";\n" +
                "var person=Person.lookup(id);");
        ifElementEvaluator = new IfElementEvaluator();
    }


    @Test
    public void evalNoneIfElement() {
        Document doc = Jsoup.parseBodyFragment("\"<h1 tittle=\\\"tittle\\\"></h1>\";");
        String htmlBeforeEval = doc.html();
        Elements elements = doc.getAllElements();
        elements.forEach(x -> ifElementEvaluator.evalElement(engine, x, new ArrayList<>()));
        assertThat(htmlBeforeEval, equalTo(doc.html()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void evalInvalidIfElement() {
        Document doc = Jsoup.parseBodyFragment("<h2 data-if=\"person.name\" title=\"${person.spouse}\">Spouse:\n" +
                "\t${person.spouse}</h2>");
        Elements elements = doc.getAllElements();
        elements.forEach(x -> ifElementEvaluator.evalElement(engine, x, new ArrayList<>()));
    }

    @Test
    public void evalValidFalseIfElement() {
        Document doc = Jsoup.parseBodyFragment("<h2 data-if=\"person.married\" title=\"${person.spouse}\">Spouse:\n" +
                "\t${person.spouse}</h2>");
        Elements elements = doc.getAllElements();
        List<Node> trash = new ArrayList<>();
        elements.forEach(x -> ifElementEvaluator.evalElement(engine, x, trash));
        trash.forEach(x -> x.remove());
        assertThat(doc.html(), equalTo("<html>\n" +
                " <head></head>\n" +
                " <body></body>\n" +
                "</html>"));
    }

    @Test
    public void evalValidTrueIfElement() throws ScriptException {
        ScriptEngine tempEngine = new ScriptEngineManager().getEngineByName(NASHRON_ENGINE);
        tempEngine.eval(NASHRON_MOZILLA);
        tempEngine.eval("importClass(Packages.biz.netcentric.app.Person);\n" +
                "var id=\"2\";\n" +
                "var person=Person.lookup(id);");
        Document doc = Jsoup.parseBodyFragment("<h2 data-if=\"person.married\" title=\"${person.spouse}\">Spouse:\n" +
                "\t${person.spouse}</h2>");
        Elements elements = doc.getAllElements();
        List<Node> trash = new ArrayList<>();
        elements.forEach(x -> ifElementEvaluator.evalElement(tempEngine, x, trash));
        trash.forEach(x -> x.remove());
        assertThat(doc.html(), equalTo("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <h2 data-if=\"person.married\" title=\"${person.spouse}\">Spouse: ${person.spouse}</h2>\n" +
                " </body>\n" +
                "</html>"));
    }


}
